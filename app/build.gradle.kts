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
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.androidify.androidApplication)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.baselineprofile)
    id("com.google.android.gms.oss-licenses-plugin")
    id("org.spdx.sbom") version "0.9.0"
}

android {
    namespace = "com.android.developers.androidify"
    defaultConfig {
        applicationId = "com.android.developers.androidify"
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
            baselineProfile.automaticGenerationDuringBuild = false
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            baselineProfile.automaticGenerationDuringBuild = false
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }
            // Conditionally apply signingConfig for release builds
            // If the 'CI_BUILD' project property is set to 'true', do not assign a signingConfig.
            // Otherwise, (e.g., for local Android Studio builds), sign with the debug key.
            if (project.findProperty("CI_BUILD")?.toString()?.toBoolean() == true) {
                // For CI builds, we want an unsigned artifact.
                // No signingConfig is assigned here.
                // The bundleRelease task will produce an unsigned AAB.
                println("CI_BUILD property detected. Release build will be unsigned by Gradle.")
            } else {
                // For local builds (not CI), sign with the debug key to allow easy deployment.
                // This ensures you can select the "release" variant in Android Studio and run it.
                println("Not a CI_BUILD or CI_BUILD property not set. Signing release build with debug key.")
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    // To avoid packaging conflicts when using bouncycastle
    packaging {
        resources {
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }
}

baselineProfile() {
    dexLayoutOptimization = true
}

spdxSbom {
    targets {
        // create a target named "release",
        // this is used for the task name (spdxSbomForRelease)
        // and output file (release.spdx.json)
        create("release") {
            configurations.set(listOf("releaseRuntimeClasspath"))
        }
    }
}
dependencies {
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.app.startup)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.timber)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.app.check)
    implementation(libs.firebase.config)
    implementation(libs.firebase.appcheck.debug)

    implementation(libs.androidx.window)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.oss.licenses) {
        exclude(group = "androidx.appcompat")
    }

    implementation(projects.feature.camera)
    implementation(projects.feature.creation)
    implementation(projects.feature.home)
    implementation(projects.feature.results)

    implementation(projects.core.theme)
    implementation(projects.core.util)

    // library must be compileOnly, see
    // https://developer.android.com/develop/xr/jetpack-xr-sdk/getting-started#enable-minification
    compileOnly(libs.androidx.xr.extensions)

    baselineProfile(projects.benchmark)

    // Android Instrumented Tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(projects.core.testing)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.androidx.ui.test.manifest)
}

androidComponents {
    beforeVariants { variantBuilder ->
        variantBuilder.enableAndroidTest = false
    }
}

