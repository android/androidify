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
package com.android.developers.androidify

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.window.TrustedPresentationThresholds
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.android.developers.androidify.navigation.MainNavigation
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.util.LocalOcclusion
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.Consumer

@ExperimentalMaterial3ExpressiveApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val isWindowOccluded = mutableStateOf(false)
    private val presentationListener = Consumer<Boolean> { isMinFractionRendered ->
        isWindowOccluded.value = !isMinFractionRendered
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidifyTheme {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                    ),
                    navigationBarStyle = SystemBarStyle.light(
                        Color.Transparent.toArgb(),
                        Color.Transparent.toArgb(),
                    ),
                )
                CompositionLocalProvider(LocalOcclusion provides isWindowOccluded) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val minAlpha = 1f
            val minFractionRendered = 0.25f
            val stabilityRequirements = 500
            val presentationThreshold = TrustedPresentationThresholds(
                minAlpha,
                minFractionRendered,
                stabilityRequirements,
            )

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.registerTrustedPresentationListener(
                window.decorView.windowToken,
                presentationThreshold,
                mainExecutor,
                presentationListener,
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.unregisterTrustedPresentationListener(presentationListener)
        }
    }
}
