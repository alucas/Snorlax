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

package com.alucas.snorlax.module.feature.rename;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alucas.snorlax.common.Decimals;
import com.alucas.snorlax.common.Strings;
import com.alucas.snorlax.module.pokemon.Pokemon;
import com.alucas.snorlax.module.pokemon.PokemonFactory;
import com.alucas.snorlax.module.pokemon.PokemonFormat;
import com.alucas.snorlax.module.util.Log;

import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;
import POGOProtos.Enums.PokemonTypeOuterClass.PokemonType;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;

import static POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import static java.lang.Integer.parseInt;

@Singleton
final class RenameFormat {
	private static final List<Character> DELIMITERS = Arrays.asList('%', '℅');

	private static final String BASE_NAME = "NAME";
	private static final String BASE_NICK = "NICK";
	private static final String BASE_LVL = "LVL";
	private static final String BASE_IV = "IV";
	private static final String BASE_ATT = "ATT";
	private static final String BASE_DEF = "DEF";
	private static final String BASE_STA = "STA";
	private static final String BASE_MV1 = "MV1";
	private static final String BASE_MV2 = "MV2";
	private static final String BASE_MVT1 = "MVT1";
	private static final String BASE_MVT2 = "MVT2";
	private static final String BASE_MVP1 = "MVP1";
	private static final String BASE_MVP2 = "MVP2";
	private static final String BASE_TYP1 = "TYP1";
	private static final String BASE_TYP2 = "TYP2";
	private static final String BASE_CP = "CP";
	private static final String BASE_CPL = "CPL";
	private static final String BASE_CAND = "CAND";

	private final PokemonFactory mPokemonFactory;
	private final RenamePreferences mRenamePreferences;

	@Inject
	RenameFormat(PokemonFactory pokemonFactory, RenamePreferences renamePreferences) {
		mPokemonFactory = pokemonFactory;
		mRenamePreferences = renamePreferences;
	}

	@NonNull
	String format(PokemonData pokemonData, HashMap<Integer, Integer> candyList) {
		final Pokemon pokemon = mPokemonFactory.with(pokemonData);
		final String format = mRenamePreferences.getFormat();

		StringBuilder builder = new StringBuilder();

		for (int i = 0, len = format.length(); i < len; ) {
			int nextDelimiter = Integer.MAX_VALUE;
			for (int j = 0; j < DELIMITERS.size(); j++) {
				int nextDelimiterTmp = format.indexOf(DELIMITERS.get(j), i + 1);
				if (nextDelimiterTmp == -1) {
					continue;
				}

				nextDelimiter = Math.min(nextDelimiter, nextDelimiterTmp);
			}

			if (!DELIMITERS.contains(format.charAt(i))) {
				final int end = (nextDelimiter == Integer.MAX_VALUE) ? len : nextDelimiter;

				builder.append(format.substring(i, end));
				i = end;
			} else if (nextDelimiter == Integer.MAX_VALUE) {
				builder.append(format.substring(i));
				i = len;
			} else if (format.substring(i + 1, nextDelimiter).contains(" ") || format.substring(i, nextDelimiter).length() < 3) {
				builder.append(format.substring(i, nextDelimiter));
				i = nextDelimiter;
			} else {
				builder.append(processFormat(pokemon, format.substring(i + 1, nextDelimiter), candyList));
				i = nextDelimiter + 1;
			}
		}

		return builder.toString();
	}

	private String processFormat(Pokemon pokemon, String command, HashMap<Integer, Integer> candyList) {
		final String target = command.toUpperCase(Locale.getDefault());

		String processed = null;

		if (target.startsWith(BASE_NAME)) {
			processed = processName(target, pokemon.getName());
		} else if (target.startsWith(BASE_NICK)) {
			processed = processNick(target, pokemon.getName(), pokemon.getNickname());
		} else if (target.startsWith(BASE_MV1)) {
			processed = processMove(target, pokemon.getMoveFast());
		} else if (target.startsWith(BASE_MV2)) {
			processed = processMove(target, pokemon.getMoveCharge());
		} else if (target.startsWith(BASE_MVT1)) {
			processed = processMoveType(target, pokemon.getMoveFast());
		} else if (target.startsWith(BASE_MVT2)) {
			processed = processMoveType(target, pokemon.getMoveCharge());
		} else if (target.startsWith(BASE_MVP1)) {
			processed = processMovePower(target, pokemon.getMoveFast());
		} else if (target.startsWith(BASE_MVP2)) {
			processed = processMovePower(target, pokemon.getMoveCharge());
		} else if (target.startsWith(BASE_TYP1)) {
			processed = processType(target, pokemon.getType1());
		} else if (target.startsWith(BASE_TYP2)) {
			processed = processType(target, pokemon.getType2());
		} else if (target.startsWith(BASE_LVL)) {
			processed = processLevel(target, pokemon.getLevel());
		} else if (target.startsWith(BASE_IV)) {
			processed = processIv(target, pokemon.getIv() * 100);
		} else if (target.startsWith(BASE_ATT)) {
			processed = processAttack(target, pokemon.getIVAttack());
		} else if (target.startsWith(BASE_DEF)) {
			processed = processDefense(target, pokemon.getIVDefense());
		} else if (target.startsWith(BASE_STA)) {
			processed = processStamina(target, pokemon.getIVStamina());
		} else if (target.startsWith(BASE_CP)) {
			processed = processCP(target, pokemon);
		} else if (target.startsWith(BASE_CAND)) {
			processed = processCandy(target, pokemon, candyList);
		}

		return Strings.isNullOrEmpty(processed) ? DELIMITERS.get(0) + command + DELIMITERS.get(0) : processed;
	}

	@Nullable
	private String processName(String target, String name) {
		final int length = target.length();
		final int dot = target.indexOf('.') + 1;

		if (length == BASE_NAME.length()) {
			return name;
		} else if (dot > 0 && length > dot) {
			try {
				return Strings.truncateAt(name, parseInt(target.substring(dot)));
			} catch (NumberFormatException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processNick(String target, String name, String nick) {
		return Strings.isNullOrEmpty(nick) ? processName(target.replace(BASE_NICK, BASE_NAME), name) : nick;
	}

	@Nullable
	private String processMove(String target, MoveSettings moveSettings) {
		if (moveSettings == null) {
			return null;
		}

		final int length = target.length();
		final int dot = target.indexOf('.') + 1;
		final PokemonMove move = moveSettings.getMovementId();

		if (length == BASE_MV1.length() || length == BASE_MV2.length()) {
			return PokemonFormat.formatMove(move);
		} else if (dot > 0 && length > dot) {
			try {
				return Strings.truncateAt(PokemonFormat.formatMove(move), parseInt(target.substring(dot)));
			} catch (NumberFormatException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processMoveType(String target, MoveSettings moveSettings) {
		if (moveSettings == null) {
			return null;
		}

		final int length = target.length();
		final int dot = target.indexOf('.') + 1;
		final PokemonType type = moveSettings.getPokemonType();
		final String typeName = PokemonFormat.formatType(type);

		if (length == BASE_MVT1.length() || length == BASE_MVT2.length()) {
			return typeName;
		} else if (dot > 0 && length > dot) {
			try {
				final int truncateLength = parseInt(target.substring(dot));
				final String typeNameFormatted = (truncateLength == 2) ? typeName.charAt(0) + typeName.substring(2) : typeName;

				return Strings.truncateAt(typeNameFormatted, truncateLength);
			} catch (NumberFormatException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processMovePower(String target, MoveSettings moveSettings) {
		if (moveSettings == null) {
			return null;
		}

		final float power = moveSettings.getPower();

		if (target.equals(BASE_MVP1) || target.equals(BASE_MVP2)) {
			return Decimals.format(power, 1, 3, 0, 0);
		}
		if (target.equals(BASE_MVP1.concat("P")) || target.equals(BASE_MVP2.concat("P"))) {
			return Decimals.format(power, 3, 3, 0, 0);
		}

		return null;
	}

	@Nullable
	private String processType(String target, PokemonType type) {
		final int length = target.length();
		final int dot = target.indexOf('.') + 1;
		final String typeName = PokemonFormat.formatType(type);

		if (length == BASE_MVT1.length() || length == BASE_MVT2.length()) {
			return typeName;
		} else if (dot > 0 && length > dot) {
			try {
				final int truncateLength = parseInt(target.substring(dot));
				final String typeNameFormatted = (truncateLength == 2) ? typeName.charAt(0) + typeName.substring(2) : typeName;

				return Strings.truncateAt(typeNameFormatted, truncateLength);
			} catch (NumberFormatException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processLevel(String target, float level) {

		if (target.equals(BASE_LVL)) {
			return Decimals.format(level, 1, 2, 1, 1);
		}
		if (target.equals(BASE_LVL.concat("P"))) {
			return Decimals.format(level, 2, 2, 1, 1);
		}
		if (target.startsWith(BASE_LVL.concat("."))) {
			try {
				final int decimals = Integer.parseInt(target.substring(target.indexOf('.') + 1));
				return Decimals.format(level, 1, 2, decimals, decimals);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				Log.e(e);
			}
		}
		if (target.startsWith(BASE_LVL.concat("P."))) {
			try {
				final int decimals = Integer.parseInt(target.substring(target.indexOf('.') + 1));
				return Decimals.format(level, 2, 2, decimals, decimals);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processIv(String target, double iv) {
		if (target.equals(BASE_IV)) {
			return Decimals.format(iv, 1, 3, 1, 1);
		}
		if (target.equals(BASE_IV.concat("P"))) {
			return Decimals.format(iv, 3, 3, 1, 1);
		}
		if (target.startsWith(BASE_IV.concat("."))) {
			try {
				final int decimals = Integer.parseInt(target.substring(target.indexOf('.') + 1));
				return Decimals.format(iv, 1, 3, decimals, decimals);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				Log.e(e);
			}
		}
		if (target.startsWith(BASE_IV.concat("P."))) {
			try {
				final int decimals = Integer.parseInt(target.substring(target.indexOf('.') + 1));
				return Decimals.format(iv, 3, 3, decimals, decimals);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				Log.e(e);
			}
		}

		return null;
	}

	@Nullable
	private String processAttack(String target, int attack) {
		if (target.equals(BASE_ATT)) {
			return Decimals.format(attack, 1, 2, 0, 0);
		}
		if (target.equals(BASE_ATT.concat("P"))) {
			return Decimals.format(attack, 2, 2, 0, 0);
		}
		if (target.equals(BASE_ATT.concat("H"))) {
			return Integer.toHexString(attack).toUpperCase(Locale.getDefault());
		}

		return null;
	}

	@Nullable
	private String processDefense(String target, int defense) {
		if (target.equals(BASE_DEF)) {
			return Decimals.format(defense, 1, 2, 0, 0);
		}
		if (target.equals(BASE_DEF.concat("P"))) {
			return Decimals.format(defense, 2, 2, 0, 0);
		}
		if (target.equals(BASE_DEF.concat("H"))) {
			return Integer.toHexString(defense).toUpperCase(Locale.getDefault());
		}

		return null;
	}

	@Nullable
	private String processStamina(String target, int stamina) {
		if (target.equals(BASE_STA)) {
			return Decimals.format(stamina, 1, 2, 0, 0);
		}
		if (target.equals(BASE_STA.concat("P"))) {
			return Decimals.format(stamina, 2, 2, 0, 0);
		}
		if (target.equals(BASE_STA.concat("H"))) {
			return Integer.toHexString(stamina).toUpperCase(Locale.getDefault());
		}

		return null;
	}

	@Nullable
	private String processCP(String target, Pokemon pokemon) {
		if (target.equals(BASE_CP)) {
			return Decimals.format(pokemon.getCp(), 2, 4, 0, 0);
		} else if (target.equals(BASE_CPL)) {
			final int cp = pokemon.getLastEvolutionCp();
			if (cp == Pokemon.ERROR_LAST_EVOL_CP_MULTI) {
				return "?";
			} else if (cp == Pokemon.ERROR_LAST_EVOL_CP_NO_EVOL) {
				return "-";
			}

			return Decimals.format(cp, 2, 4, 0, 0);
		}

		return null;
	}

	@Nullable
	private String processCandy(String target, Pokemon pokemon, HashMap<Integer, Integer> candyList) {
		if(candyList == null || !candyList.containsKey(pokemon.getFamilyID()))
			return "-";
		else
			return Decimals.format(candyList.get(pokemon.getFamilyID()), 1, 3, 0, 0);
	}
}
