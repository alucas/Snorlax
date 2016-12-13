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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.alucas.snorlax.R;
import com.alucas.snorlax.common.Strings;
import com.alucas.snorlax.module.NotificationId;
import com.alucas.snorlax.module.context.pokemongo.PokemonGo;
import com.alucas.snorlax.module.context.snorlax.Snorlax;
import com.alucas.snorlax.module.pokemon.Pokemon;
import com.alucas.snorlax.module.pokemon.PokemonFormat;
import com.alucas.snorlax.module.util.Resource;
import com.alucas.snorlax.module.util.Resource.MODIFIER;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.PokemonRarityOuterClass.PokemonRarity;
import POGOProtos.Enums.PokemonTypeOuterClass.PokemonType;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;

@Singleton
final class EncounterNotification {
	private final Context mContext;
	private final Resources mResources;
	private final NotificationManager mNotificationManager;
	private static final Map<PokemonType, Integer> TYPE_SYMBOL = new EnumMap<>(PokemonType.class);

	static {
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_BUG, R.string.symbol_type_bug);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_DARK, R.string.symbol_type_dark);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_DRAGON, R.string.symbol_type_dragon);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_ELECTRIC, R.string.symbol_type_electric);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_FAIRY, R.string.symbol_type_fairy);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_FIGHTING, R.string.symbol_type_fighting);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_FIRE, R.string.symbol_type_fire);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_FLYING, R.string.symbol_type_flying);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_GHOST, R.string.symbol_type_ghost);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_GRASS, R.string.symbol_type_grass);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_GROUND, R.string.symbol_type_ground);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_ICE, R.string.symbol_type_ice);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_NORMAL, R.string.symbol_type_normal);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_POISON, R.string.symbol_type_poison);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_PSYCHIC, R.string.symbol_type_psychic);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_ROCK, R.string.symbol_type_rock);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_STEEL, R.string.symbol_type_steel);
		TYPE_SYMBOL.put(PokemonType.POKEMON_TYPE_WATER, R.string.symbol_type_water);
	}

	@Inject
	EncounterNotification(@Snorlax Context context, @Snorlax Resources resources, @PokemonGo NotificationManager notificationManager) {
		mContext = context;
		mResources = resources;
		mNotificationManager = notificationManager;
	}

	void cancel() {
		mNotificationManager.cancel(NotificationId.ID_ENCOUNTER);
	}

	@SuppressWarnings("deprecation")
	void show(RequestTypeOuterClass.RequestType encounterType, Pokemon pokemon, double pokeRate, double greatRate, double ultraRate) {
		final double weightRatio = pokemon.getWeight() / pokemon.getBaseWeight();
		final double heightRatio = pokemon.getHeight() / pokemon.getBaseHeight();
		final MODIFIER resourceModifier = (pokemon.getNumber() == PokemonId.PIKACHU_VALUE ? MODIFIER.FAN
			: pokemon.getNumber() == PokemonId.RATTATA_VALUE && heightRatio < 0.80 ? MODIFIER.YOUNGSTER
			: pokemon.getNumber() == PokemonId.MAGIKARP_VALUE && weightRatio > 1.30 ? MODIFIER.FISHERMAN
			: MODIFIER.NO);

		final String fastMoveName = PokemonFormat.formatMove(pokemon.getMoveFast().getMovementId());
		final String chargeMoveName = PokemonFormat.formatMove(pokemon.getMoveCharge().getMovementId());
		final String fastMoveTypeName = PokemonFormat.formatType(pokemon.getMoveFast().getPokemonType());
		final String chargeMoveTypeName = PokemonFormat.formatType(pokemon.getMoveCharge().getPokemonType());
		final String fastMoveTypeSymbol = TYPE_SYMBOL.containsKey(pokemon.getMoveFast().getPokemonType()) ? mResources.getString(TYPE_SYMBOL.get(pokemon.getMoveFast().getPokemonType())) : "?";
		final String chargeMoveTypeSymbol = TYPE_SYMBOL.containsKey(pokemon.getMoveCharge().getPokemonType()) ? mResources.getString(TYPE_SYMBOL.get(pokemon.getMoveCharge().getPokemonType())) : "?";

		final Map<String, Pair<String, Integer>> symbols = getSymbolReplacementTable();
		new Handler(Looper.getMainLooper()).post(() -> {
			Notification notification = new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_pokeball)
				.setLargeIcon(Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(
						mResources,
						Resource.getPokemonResourceId(mContext, mResources, pokemon.getNumber(), resourceModifier)
					),
					Resource.getLargeIconWidth(mResources),
					Resource.getLargeIconHeight(mResources),
					false
				))
				.setContentTitle(EncounterFormat.format(mContext.getString(R.string.notification_title, pokemon.getName(), pokemon.getCp(), pokemon.getLevel(), EncounterFormat.formatEncounterType((encounterType))), symbols))
				.setContentText(EncounterFormat.format(mContext.getString(R.string.notification_content, pokemon.getIv() * 100, pokemon.getFleeRate() * 100, pokeRate, greatRate, ultraRate), symbols))
				.setStyle(new NotificationCompat.InboxStyle()
					.addLine(EncounterFormat.format(mContext.getString(R.string.notification_category_stats_content_iv, pokemon.getIv() * 100, pokemon.getIVAttack(), pokemon.getIVDefense(), pokemon.getIVStamina()), symbols))
					.addLine(EncounterFormat.format(mContext.getString(R.string.notification_category_stats_content_hp, pokemon.getHp(), pokemon.getFleeRate() * 100), symbols))
					.addLine(EncounterFormat.bold(mContext.getString(R.string.notification_category_moves_title)))
					.addLine(EncounterFormat.format(mContext.getString(R.string.notification_category_moves_fast, fastMoveName, fastMoveTypeName, pokemon.getMoveFast().getPower()), symbols))
					.addLine(EncounterFormat.format(mContext.getString(R.string.notification_category_moves_charge, chargeMoveName, chargeMoveTypeName, pokemon.getMoveCharge().getPower()), symbols))
					.addLine(EncounterFormat.bold(mContext.getString(R.string.notification_categoty_catch_title)))
					.addLine(EncounterFormat.format(mContext.getString(R.string.notification_categoty_catch_content, pokeRate, greatRate, ultraRate), symbols))
					.setSummaryText(getFooter(pokemon.getType1(), pokemon.getType2(), pokemon.getPokemonClass()))
				)
				.setColor(ContextCompat.getColor(mContext, R.color.red_700))
				.setAutoCancel(true)
				.setVibrate(new long[]{0})
				.setPriority(Notification.PRIORITY_MAX)
				.setCategory(NotificationCompat.CATEGORY_ALARM)
				.build();

			hideIcon(notification);

			mNotificationManager.notify(NotificationId.ID_ENCOUNTER, notification);
		});
	}

	private Map<String, Pair<String, Integer>> getSymbolReplacementTable() {
		final Map<String, Pair<String, Integer>> symbolTable = new HashMap<>();
		symbolTable.put(mContext.getString(R.string.notification_symbol_pokeball_key), new Pair<>(mContext.getString(R.string.notification_symbol_pokeball_value), ContextCompat.getColor(mContext, R.color.notification_symbol_pokeball_color)));
		symbolTable.put(mContext.getString(R.string.notification_symbol_greatball_key), new Pair<>(mContext.getString(R.string.notification_symbol_greatball_value), ContextCompat.getColor(mContext, R.color.notification_symbol_greatball_color)));
		symbolTable.put(mContext.getString(R.string.notification_symbol_ultraball_key), new Pair<>(mContext.getString(R.string.notification_symbol_ultraball_value), ContextCompat.getColor(mContext, R.color.notification_symbol_ultraball_color)));
		symbolTable.put(mContext.getString(R.string.notification_symbol_flee_key), new Pair<>(mContext.getString(R.string.notification_symbol_flee_value), ContextCompat.getColor(mContext, R.color.notification_symbol_flee_color)));
		symbolTable.put(mContext.getString(R.string.notification_symbol_iv_key), new Pair<>(mContext.getString(R.string.notification_symbol_iv_value), ContextCompat.getColor(mContext, R.color.notification_symbol_iv_color)));
		symbolTable.put(mContext.getString(R.string.notification_symbol_hp_key), new Pair<>(mContext.getString(R.string.notification_symbol_hp_value), ContextCompat.getColor(mContext, R.color.notification_symbol_hp_color)));

		return symbolTable;
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	private String getFooter(PokemonType type1, PokemonType type2, PokemonRarity pokemonClass) {
		return new StringBuilder()
			.append(PokemonFormat.formatType(type1))
			.append(type2 == PokemonType.POKEMON_TYPE_NONE ? Strings.EMPTY : String.format(Locale.US, "/%s", PokemonFormat.formatType(type2)))
			.append(" - ")
			.append(PokemonFormat.formatRarity(pokemonClass))
			.toString();
	}

	@SuppressWarnings("deprecation")
	private void hideIcon(Notification notification) {
		int iconId = mResources.getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
		if (iconId != 0) {
			if (notification.contentView != null) {
				notification.contentView.setViewVisibility(iconId, View.INVISIBLE);
			}
			if (notification.bigContentView != null) {
				notification.bigContentView.setViewVisibility(iconId, View.INVISIBLE);
			}
		}
	}
}
