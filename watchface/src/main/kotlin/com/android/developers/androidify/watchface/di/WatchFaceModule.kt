/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.developers.androidify.watchface.di

import android.content.Context
import android.os.Build
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.watchface.creator.WatchFaceCreator
import com.android.developers.androidify.watchface.creator.WatchFaceCreatorImpl
import com.android.developers.androidify.watchface.transfer.EmptyWatchFaceInstallationRepositoryImpl
import com.android.developers.androidify.watchface.transfer.MIN_WATCH_FACE_SDK_VERSION
import com.android.developers.androidify.watchface.transfer.WatchFaceInstallationRepository
import com.android.developers.androidify.watchface.transfer.WatchFaceInstallationRepositoryImpl
import com.android.developers.androidify.watchface.transfer.WearAssetTransmitter
import com.android.developers.androidify.watchface.transfer.WearAssetTransmitterImpl
import com.android.developers.androidify.watchface.transfer.WearDeviceRepository
import com.android.developers.androidify.watchface.transfer.WearDeviceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WatchFaceModule {
    @Provides
    @Singleton
    fun provideWatchFaceCreator(
        @ApplicationContext context: Context,
    ): WatchFaceCreator {
        return WatchFaceCreatorImpl(context)
    }

    @Provides
    @Singleton
    fun provideWearAssetTransmitter(
        @ApplicationContext context: Context,
    ): WearAssetTransmitter {
        return WearAssetTransmitterImpl(context)
    }

    @Provides
    @Singleton
    fun providesWearDeviceRepository(
        @ApplicationContext context: Context,
    ): WearDeviceRepository {
        return WearDeviceRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideWatchFaceInstallationRepository(
        supportedImpl: WatchFaceInstallationRepositoryImpl,
        noSupportImpl: EmptyWatchFaceInstallationRepositoryImpl,
        remoteConfigDataSource: RemoteConfigDataSource,
    ): WatchFaceInstallationRepository {
        val watchFacesEnabled = remoteConfigDataSource.watchfaceFeatureEnabled()
        return if (Build.VERSION.SDK_INT >= MIN_WATCH_FACE_SDK_VERSION && watchFacesEnabled || true) {
            supportedImpl
        } else {
            noSupportImpl
        }
    }
}
