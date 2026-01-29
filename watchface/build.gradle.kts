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
    alias(libs.plugins.androidify.androidLibrary)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.android.developers.androidify.watchface"
    defaultConfig {
        testInstrumentationRunner = "com.android.developers.testing.AndroidifyTestRunner"
        consumerProguardFiles("proguard-rules.pro")
    }
    buildFeatures {
        buildConfig = true
    }
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("src/androidTest/assets")
        }
    }
    // To avoid packaging conflicts when using bouncycastle
    packaging {
        resources {
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.validator.push.android) {
        exclude(group = "com.google.guava", "listenablefuture")
    }
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(projects.wear.common)
    implementation(projects.core.network)
    implementation(libs.apksig)

    // For testing
    androidTestImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(projects.core.testing)
    kspAndroidTest(libs.hilt.compiler)
}