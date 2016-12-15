package com.alucas.snorlax.module.feature.gym;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LongSparseArray;
import android.util.Pair;

import com.alucas.snorlax.common.rx.RxFuncitons;
import com.alucas.snorlax.module.context.snorlax.Snorlax;
import com.alucas.snorlax.module.feature.Feature;
import com.alucas.snorlax.module.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import rx.Subscription;

@Singleton
public class GymPersistence implements Feature {
	private static final String LOG_PREFIX = "[" + GymPersistence.class.getSimpleName() + "] ";

	private static final String PREF_POKEMON_IN_GYM = "pokemonInGym";

	private final Context mContext;
	private final Gson mGson;
	private final Gym mGym;
	private final GymManager mGymManager;

	private Subscription mSubscription;

	@Inject
	public GymPersistence(@Snorlax final Context context, final Gson gson, final Gym gym, final GymManager gymManager) {
		this.mContext = context;
		this.mGson = gson;
		this.mGym = gym;
		this.mGymManager = gymManager;

		this.mGymManager.initPokemonInGym(loadPokemonInGym(mContext, mGson));
	}

	@Override
	public void subscribe() {
		mSubscription = mGym
			.getObservable()
			.subscribe(pair -> {
				final Pair<PokemonData, GymData> pokemonInfo = pair.second;
				final PokemonData pokemon = pokemonInfo.first;
				final GymData gymData = pokemonInfo.second;
				switch (pair.first) {
					case POKEMON_ADD:
						savePokemonInGym(mContext, pokemon.getId(), gymData);
						break;
					case POKEMON_REMOVE:
						removePokemonInGym(mContext, pokemon.getId());
						break;
					default:
						break;
				}
			});
	}

	@Override
	public void unsubscribe() {
		RxFuncitons.unsubscribe(mSubscription);
	}

	public static LongSparseArray<GymData> loadPokemonInGym(final Context context, final Gson gson) {
		final SharedPreferences settings = context.getSharedPreferences(PREF_POKEMON_IN_GYM, 0);
		final LongSparseArray<GymData> pokemonsInGym = new LongSparseArray<>();
		final Map<String, ?> pokemonsInGymRaw = settings.getAll();
		for (final Map.Entry<String, ?> pokemonEntry : pokemonsInGymRaw.entrySet()) {
			final String gymIdString = pokemonEntry.getKey();
			Long gymId;
			try {
				gymId = Long.decode(gymIdString);
			} catch (JsonSyntaxException | NumberFormatException e) {
				Log.e(e);
				continue;
			}

			GymData gymData = null;
			try {
				gymData = gson.fromJson((String) pokemonEntry.getValue(), GymData.class);
			} catch (JsonSyntaxException e) {
				Log.e(e);
				gymData = new GymData(pokemonEntry.getKey());
			}

			pokemonsInGym.put(gymId, gymData);

			Log.d(LOG_PREFIX + "Load : " + gymData.id + ", " + gymData.name);
		}

		return pokemonsInGym;
	}

	@SuppressWarnings("unused")
	private void savePokemonInGym(final Context context, final long pokemonUID, final GymData gymData) {
		Log.d(LOG_PREFIX + "savePokemonInGym : " + pokemonUID + ", '" + gymData + "'");

		final SharedPreferences settings = context.getSharedPreferences(PREF_POKEMON_IN_GYM, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString(String.valueOf(pokemonUID), mGson.toJson(gymData));
		editor.apply();

		mGymManager.savePokemonInGym(pokemonUID, gymData);
	}

	@SuppressWarnings("unused")
	private void removePokemonInGym(final Context context, final long pokemonUID) {
		Log.d(LOG_PREFIX + "removePokemonInGym : " + pokemonUID);

		final SharedPreferences settings = context.getSharedPreferences(PREF_POKEMON_IN_GYM, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.remove(String.valueOf(pokemonUID));
		editor.apply();

		mGymManager.removePokemonInGym(pokemonUID);
	}
}
