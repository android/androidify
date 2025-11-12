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
    alias(libs.plugins.androidify.androidComposeLibrary)
}
val fontName = properties["fontName"] as String?

android {
    namespace = "com.android.developers.androidify.theme"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            buildConfigField("String" , "fontName" , fontName ?: "\"Roboto Flex\"")
        }
        release {
            buildConfigField("String" , "fontName" , fontName ?: "\"Roboto Flex\"")
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
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(projects.core.util)
    implementation(libs.guava)

    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    // api because we need to access LocalNavAnimatedScope in feature modules for animations.
    api(libs.androidx.navigation3.ui)
    debugImplementation(libs.ui.tooling)

    // Android Instrumented Tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(projects.core.testing)

    debugImplementation(libs.androidx.ui.test.manifest)
}
