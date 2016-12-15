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

package com.alucas.snorlax.module.pokemon;

import java.util.List;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonRarityOuterClass.PokemonRarity;
import POGOProtos.Enums.PokemonTypeOuterClass.PokemonType;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;
import POGOProtos.Settings.Master.PokemonSettingsOuterClass.PokemonSettings;

import static POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import static POGOProtos.Enums.PokemonIdOuterClass.PokemonId;

@SuppressWarnings({"unused", "FieldCanBeLocal", "WeakerAccess"})
public final class Pokemon {
	public static final int ERROR_LAST_EVOL_CP_NO_EVOL = -1;
	public static final int ERROR_LAST_EVOL_CP_MULTI = -2;

	private final String[] mNames;
	private final PokemonData mPokemonData;

	Pokemon(PokemonData pokemonData, String[] names) {
		mPokemonData = pokemonData;
		mNames = names;
	}

	public float getLevel() {
		if (mPokemonData.getIsEgg()) {
			return 0;
		}

		float level = 1;
		float cpMultiplier = mPokemonData.getCpMultiplier() + mPokemonData.getAdditionalCpMultiplier();

		for (double currentCpM : PokemonCp.CpM) {
			if (Math.abs(cpMultiplier - currentCpM) < 0.0001) {
				return level;
			}
			level += 0.5;
		}
		return level;
	}

	public double getIv() {
		return ((double) (getIVAttack() + getIVDefense() + getIVStamina())) / 45.0;
	}

	public int getBaseAttack() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getStats().getBaseAttack();
	}

	public int getIVAttack() {
		return mPokemonData.getIndividualAttack();
	}

	public int getBaseDefence() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getStats().getBaseDefense();
	}

	public int getIVDefense() {
		return mPokemonData.getIndividualDefense();
	}

	public int getBaseStamina() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getStats().getBaseStamina();
	}

	public int getIVStamina() {
		return mPokemonData.getIndividualStamina();
	}

	public int getCp() {
		return mPokemonData.getCp();
	}

	public int getFamilyID()
	{
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getFamilyId().getNumber();
	}

	public int getLastEvolutionCp() {
		final PokemonId currentPokemonId = mPokemonData.getPokemonId();
		PokemonId evolutionId = currentPokemonId;
		while (true) {
			final PokemonSettings pokemonSettings = PokemonSettingsRegistry.getSettings(evolutionId);
			final List<PokemonId> evolutionsId = pokemonSettings.getEvolutionIdsList();
			if (evolutionsId == null || evolutionsId.isEmpty()) {
				break;
			}

			if (evolutionsId.size() > 1) {
				return ERROR_LAST_EVOL_CP_MULTI;
			}

			evolutionId = evolutionsId.get(0);
		}

		if (evolutionId == currentPokemonId) {
			return ERROR_LAST_EVOL_CP_NO_EVOL;
		}

		final PokemonSettings pokemonSettings = PokemonSettingsRegistry.getSettings(evolutionId);
		return PokemonCp.computeCP(
			pokemonSettings.getStats().getBaseAttack() + getIVAttack(),
			pokemonSettings.getStats().getBaseDefense() + getIVDefense(),
			pokemonSettings.getStats().getBaseStamina() + getIVStamina(),
			mPokemonData.getCpMultiplier() + mPokemonData.getAdditionalCpMultiplier()
		);
	}

	public int getHp() {
		return mPokemonData.getStaminaMax();
	}

	public String getName() {
		return mNames[getNumber() - 1];
	}

	public String getNickname() {
		return mPokemonData.getNickname();
	}

	public int getNumber() {
		return mPokemonData.getPokemonId().getNumber();
	}

	public MoveSettings getMoveFast() {
		return MoveSettingsRegistry.getMeta(mPokemonData.getMove1());
	}

	public MoveSettings getMoveCharge() {
		return MoveSettingsRegistry.getMeta(mPokemonData.getMove2());
	}

	public PokemonType getType1() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getType();
	}

	public PokemonType getType2() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getType2();
	}

	public PokemonRarity getPokemonClass() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getRarity();
	}

	public double getBaseWeight() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getPokedexWeightKg();
	}

	public double getWeight() {
		return mPokemonData.getWeightKg();
	}

	public double getBaseHeight() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getPokedexHeightM();
	}

	public double getHeight() {
		return mPokemonData.getHeightM();
	}

	public double getFleeRate() {
		return PokemonSettingsRegistry.getSettings(mPokemonData.getPokemonId()).getEncounter().getBaseFleeRate();
	}
}
