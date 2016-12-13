/*
 * Copyright (c) 2016. Pedro Diaz <igoticecream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alucas.snorlax.module.feature.encounter;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.alucas.snorlax.common.rx.RxBus;
import com.alucas.snorlax.common.rx.RxFuncitons;
import com.alucas.snorlax.module.feature.Feature;
import com.alucas.snorlax.module.feature.capture.CaptureEvent;
import com.alucas.snorlax.module.feature.mitm.MitmRelay;
import com.alucas.snorlax.module.feature.mitm.MitmUtil;
import com.alucas.snorlax.module.pokemon.Pokemon;
import com.alucas.snorlax.module.pokemon.PokemonFactory;
import com.alucas.snorlax.module.pokemon.probability.PokemonProbability;
import com.alucas.snorlax.module.pokemon.probability.PokemonProbabilityFactory;
import com.alucas.snorlax.module.util.Log;

import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus;
import rx.Subscription;

import static POGOProtos.Data.Capture.CaptureProbabilityOuterClass.CaptureProbability;
import static POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import static POGOProtos.Networking.Requests.RequestTypeOuterClass.RequestType;
import static POGOProtos.Networking.Responses.DiskEncounterResponseOuterClass.DiskEncounterResponse;
import static POGOProtos.Networking.Responses.EncounterResponseOuterClass.EncounterResponse;
import static POGOProtos.Networking.Responses.IncenseEncounterResponseOuterClass.IncenseEncounterResponse;

@Singleton
public final class Encounter implements Feature {
	private static final String LOG_PREFIX = "[" + Encounter.class.getCanonicalName() + "]";

	private final MitmRelay mMitmRelay;
	private final PokemonFactory mPokemonFactory;
	private final PokemonProbabilityFactory mPokemonProbabilityFactory;
	private final EncounterPreferences mEncounterPreferences;
	private final EncounterNotification mEncounterNotification;
	private final RxBus mRxBus;

	private Subscription mSubscription;
	private Subscription mRxBusSubscription;

	@Inject
	Encounter(MitmRelay mitmRelay, PokemonFactory pokemonFactory, PokemonProbabilityFactory pokemonProbabilityFactory, EncounterPreferences encounterPreferences, EncounterNotification encounterNotification, RxBus rxBus) {
		mMitmRelay = mitmRelay;
		mPokemonFactory = pokemonFactory;
		mPokemonProbabilityFactory = pokemonProbabilityFactory;
		mEncounterPreferences = encounterPreferences;
		mEncounterNotification = encounterNotification;
		mRxBus = rxBus;
	}

	@Override
	public void subscribe() {
		unsubscribe();

		mSubscription = mMitmRelay
			.getObservable()
			.compose(mEncounterPreferences.isEnabled())
			.flatMap(MitmUtil.filterResponse(RequestType.ENCOUNTER, RequestType.DISK_ENCOUNTER, RequestType.INCENSE_ENCOUNTER))
			.subscribe(messages -> {
				switch (messages.requestType) {
					case ENCOUNTER:
						onWildEncounter(messages.response);
						break;
					case DISK_ENCOUNTER:
						onDiskEncounter(messages.response);
						break;
					case INCENSE_ENCOUNTER:
						onIncenseEncounter(messages.response);
						break;
					default:
						break;
				}
			}, Log::e);

		mRxBusSubscription = mRxBus
			.receive(CaptureEvent.class)
			.map(CaptureEvent::getCatchStatus)
			.filter(status -> status.equals(CatchStatus.CATCH_FLEE) || status.equals(CatchStatus.CATCH_SUCCESS))
			.compose(mEncounterPreferences.isDismissEnabled())
			.subscribe(dismiss -> mEncounterNotification.cancel());
	}

	private void onWildEncounter(ByteString bytes) {
		try {
			EncounterResponse response = EncounterResponse.parseFrom(bytes);
			onEncounter(RequestType.ENCOUNTER, response.getWildPokemon().getPokemonData(), response.getCaptureProbability());
		} catch (InvalidProtocolBufferException | NullPointerException e) {
			Log.d("EncounterResponse failed: %s", e.getMessage());
			Log.e(e);
		} catch (IllegalArgumentException e) {
			Log.d("Cannot process WildEncounterResponse: %s", e.getMessage());
			Log.e(e);
		}
	}

	private void onDiskEncounter(ByteString bytes) {
		try {
			DiskEncounterResponse response = DiskEncounterResponse.parseFrom(bytes);
			onEncounter(RequestType.DISK_ENCOUNTER, response.getPokemonData(), response.getCaptureProbability());
		} catch (InvalidProtocolBufferException | NullPointerException e) {
			Log.d("DiskEncounterResponse failed: %s", e.getMessage());
			Log.e(e);
		} catch (IllegalArgumentException e) {
			Log.d("Cannot process DiskEncounterResponse: %s", e.getMessage());
			Log.e(e);
		}
	}

	private void onIncenseEncounter(ByteString bytes) {
		try {
			IncenseEncounterResponse response = IncenseEncounterResponse.parseFrom(bytes);
			onEncounter(RequestType.INCENSE_ENCOUNTER, response.getPokemonData(), response.getCaptureProbability());
		} catch (InvalidProtocolBufferException | NullPointerException e) {
			Log.d("IncenseEncounterResponse failed: %s", e.getMessage());
			Log.e(e);
		} catch (IllegalArgumentException e) {
			Log.d("Cannot process IncenseEncounterResponse: %s", e.getMessage());
			Log.e(e);
		}
	}

	private void onEncounter(RequestType encounterType, PokemonData pokemonData, CaptureProbability captureProbability) {
		Pokemon pokemon = mPokemonFactory.with(pokemonData);
		if (pokemon == null) {
			Log.d(LOG_PREFIX + "Failed to create Pokemon from PokemonData");
			return;
		}

		PokemonProbability probability = mPokemonProbabilityFactory.with(captureProbability);

		mEncounterNotification.show(
			encounterType,
			pokemon,
			probability.getPokeball(),
			probability.getGreatball(),
			probability.getUltraball()
		);
	}

	@Override
	public void unsubscribe() {
		RxFuncitons.unsubscribe(mSubscription);
		RxFuncitons.unsubscribe(mRxBusSubscription);
	}
}
