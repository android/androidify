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
package com.android.developers.androidify.startup

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.data.GeminiNanoDownloaderImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("EnsureInitializerMetadata") // Registered in :app module
class GeminiNanoDownloaderInitializer : Initializer<Unit> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GeminiNanoDownloaderInitializerEntryPoint {
        fun geminiNanoDownloader(): GeminiNanoDownloaderImpl
        fun remoteConfigDataSource(): RemoteConfigDataSource
    }

    override fun create(context: Context) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            GeminiNanoDownloaderInitializerEntryPoint::class.java,
        )
        val remoteConfigDataSource = hiltEntryPoint.remoteConfigDataSource()
        if (remoteConfigDataSource.useGeminiNano()) {
            val geminiNanoDownloader = hiltEntryPoint.geminiNanoDownloader()
            CoroutineScope(Dispatchers.IO).launch {
                geminiNanoDownloader.downloadModel()
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(FirebaseRemoteConfigInitializer::class.java)
    }
}
