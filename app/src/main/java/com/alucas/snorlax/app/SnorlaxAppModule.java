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

package com.alucas.snorlax.app;

import javax.inject.Singleton;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;

import com.alucas.snorlax.module.util.GsonAdapterFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import dagger.Module;
import dagger.Provides;

@Module
final class SnorlaxAppModule {

	private final Application mApplication;

	SnorlaxAppModule(final Application application) {
		this.mApplication = application;
	}

	@Provides
	@Singleton
	Resources provideResources() {
		return mApplication.getResources();
	}

	@Provides
	@Singleton
	NotificationManager provideNotificationManager() {
		return (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
	}
}
