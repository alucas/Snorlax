package com.alucas.snorlax.common.dagger;

import javax.inject.Singleton;

import com.alucas.snorlax.module.util.GsonAdapterFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class GsonModule {
	@Provides
	@Singleton
	TypeAdapterFactory provideTypeAdapterFactory() {
		return GsonAdapterFactory.create();
	}

	@Provides
	@Singleton
	Gson provideGson(TypeAdapterFactory typeAdapterFactory) {
		return new GsonBuilder()
			.serializeNulls()
			.setPrettyPrinting()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapterFactory(typeAdapterFactory)
			.create();
	}
}
