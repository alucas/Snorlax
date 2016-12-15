package com.alucas.snorlax.module.feature.gym;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.util.LongSparseArray;

@Singleton
public class GymManager {
	private LongSparseArray<GymData> mPokemonsInGym = new LongSparseArray<>();

	@Inject
	public GymManager() {
		// Must be present because dependency injection
	}

	public void initPokemonInGym(final LongSparseArray<GymData> pokemons) {
		mPokemonsInGym = pokemons.clone();
	}

	public void removePokemonInGym(final long pokemonUID) {
		mPokemonsInGym.remove(pokemonUID);
	}

	public void savePokemonInGym(final long pokemonUID, GymData gymData) {
		mPokemonsInGym.put(pokemonUID, gymData);
	}

	public boolean wasPokemonInGym(final Long pokemonUID) {
		return mPokemonsInGym.get(pokemonUID) != null;
	}

	public GymData getPokemonInGym(final Long pokemonUID) {
		return mPokemonsInGym.get(pokemonUID);
	}

	public int getPokemonInGymSize() {
		return mPokemonsInGym.size();
	}

	public GymData getPokemonInGymById(final int id) {
		return getPokemonInGym(mPokemonsInGym.keyAt(id));
	}
}
