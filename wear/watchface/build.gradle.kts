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

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.android.developers.androidify.watchfacepush.defaultwf"

    defaultConfig {
        applicationId = "com.android.developers.androidify.watchfacepush.defaultwf"
        minSdk = 36
        // The default watch face version is kept in lock step with the Wear OS app.
        versionCode = libs.versions.appVersionWearOffset.get().toInt() + libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = false
        }
        debug {
            isMinifyEnabled = true
        }
    }
}

