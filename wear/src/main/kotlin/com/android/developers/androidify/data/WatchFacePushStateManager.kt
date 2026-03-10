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
package com.android.developers.androidify.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.watchFaceInstallationStatusDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wfp")

/**
 * Storage for the state of Watch Face Push, namely whether permissions and one-shot APIs have
 * already been used / set.
 */
class WatchFacePushStateManager(val context: Context) {
    private val activeWatchFaceApiUsedKey = booleanPreferencesKey("setActiveUsed")
    private val permissionDeniedKey = booleanPreferencesKey("permissionDenied")

    /**
     * The [setWatchFaceAsActive] API is a single-shot API - after one once it will not
     * work again. This indicates whether the API has already been used.
     */
    val activeWatchFaceApiUsed: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[activeWatchFaceApiUsedKey] == true
        }

    /**
     * Marks that the [setWatchFaceAsActive] API call has already been used.
     */
    suspend fun setActiveWatchFaceApiUsedKey(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[activeWatchFaceApiUsedKey] = value
        }
    }

    /**
     * Indicates whether the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission has been denied already.
     */
    val watchFacePermissionDenied: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[permissionDeniedKey] == true
        }

    /**
     * Sets whether the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission has already been denied once.
     */
    suspend fun setWatchFacePermissionDenied(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[permissionDeniedKey] = value
        }
    }

    /**
     * Sets the current status of a watch face installation.
     */
    suspend fun setWatchFaceInstallationStatus(watchFaceInstallationStatus: WatchFaceInstallationStatus) {
        context.watchFaceInstallationStatusDataStore.updateData { watchFaceInstallationStatus }
    }

    /**
     * The current status of a watch face installation.
     */
    val watchFaceInstallationStatus = context.watchFaceInstallationStatusDataStore.data
}
