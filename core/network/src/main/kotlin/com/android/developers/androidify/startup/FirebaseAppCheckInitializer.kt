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
import com.android.developers.androidify.network.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import timber.log.Timber

/**
 * Initialize [FirebaseAppCheck] using the App Startup Library.
 */
@SuppressLint("EnsureInitializerMetadata") // Registered in :app module
class FirebaseAppCheckInitializer : Initializer<FirebaseAppCheck> {
    override fun create(context: Context): FirebaseAppCheck {
        val appCheck = Firebase.appCheck.apply {
            if (BuildConfig.DEBUG) {
                Timber.i(
                    "Installing Firebase debug, ensure your " +
                        "debug token is registered on Firebase Console",
                )
                installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance(),
                )
            } else {
                Timber.i("Play integrity installing...")
                installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance(),
                )
            }
            setTokenAutoRefreshEnabled(true)
        }

        val token = appCheck.getAppCheckToken(false)
        token.addOnCompleteListener {
            if (token.isSuccessful) {
                Timber.i("Firebase app check token success: ${token.result.expireTimeMillis}")
            } else {
                Timber.e(token.exception, "Firebase app check token failure")
            }
        }
        return appCheck
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return listOf(FirebaseAppInitializer::class.java)
    }
}
