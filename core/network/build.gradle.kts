import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

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
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.android.developers.androidify.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "com.android.developers.testing.AndroidifyTestRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

kotlin {
    explicitApi = ExplicitApiMode.Warning
}

// Explicitly disable the connectedAndroidTest task for this module
androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.enableAndroidTest = false
    }
}

dependencies {
    implementation(libs.androidx.app.startup)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    debugImplementation(libs.okhttp3.loggingInterceptor)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.http)
    implementation(libs.coil.gif)
    implementation(platform(libs.firebase.bom))
    implementation(libs.timber)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.analytics) {
        exclude(group = "com.google.guava")
    }

    implementation(libs.firebase.app.check)
    implementation(libs.firebase.config)
    implementation(projects.core.util)
    implementation(libs.mlkit.segmentation)
    implementation(libs.mlkit.common)
    implementation(libs.play.services.base)
    implementation(libs.google.firebase.appcheck.debug)
    ksp(libs.hilt.compiler)

    testImplementation(libs.play.services.base.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core)

    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(projects.core.testing)
    kspAndroidTest(libs.hilt.compiler)
}
