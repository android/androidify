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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize

/**
 * Initialize [FirebaseApp] using the App Startup Library.
 */
@SuppressLint("EnsureInitializerMetadata") // Registered in :app module
class FirebaseAppInitializer : Initializer<FirebaseApp> {
    override fun create(context: Context): FirebaseApp {
        return Firebase.initialize(context)!!
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        // No dependencies
        return emptyList()
    }
}
