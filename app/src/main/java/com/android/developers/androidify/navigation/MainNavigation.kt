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
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.android.developers.androidify.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.SavedStateNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.SceneSetupNavEntryDecorator
import com.android.developers.androidify.camera.CameraPreviewScreen
import com.android.developers.androidify.creation.CreationScreen
import com.android.developers.androidify.home.AboutScreen
import com.android.developers.androidify.home.HomeScreen
import com.android.developers.androidify.theme.transitions.ColorSplashTransitionScreen

/*
* Copyright 2025 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
@ExperimentalMaterial3ExpressiveApi
@Composable
fun MainNavigation() {
    val backStack = rememberMutableStateListOf<NavigationRoute>(Home)
    var positionReveal by remember {
        mutableStateOf(IntOffset.Zero)
    }
    var showSplash by remember {
        mutableStateOf(false)
    }
    val motionScheme = MaterialTheme.motionScheme
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            SceneSetupNavEntryDecorator,
            SavedStateNavEntryDecorator,
            ViewModelStoreNavEntryDecorator,
        ),
        transitionSpec = {
            ContentTransform(
                fadeIn(motionScheme.defaultEffectsSpec()),
                fadeOut(motionScheme.defaultEffectsSpec()),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                fadeIn(motionScheme.defaultEffectsSpec()),
                scaleOut(
                    targetScale = 0.7f,
                    transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
                ),
            )
        },
        entryProvider = entryProvider {
            entry<Home> { entry ->
                HomeScreen(
                    onClickLetsGo = { positionOffset ->
                        showSplash = true
                        positionReveal = positionOffset
                    },
                    onAboutClicked = {
                        backStack.add(About)
                    },
                )
            }
            entry<Camera> {
                CameraPreviewScreen(
                    onImageCaptured = { uri ->
                        backStack.add(Create(uri.toString()))
                    },
                )
            }
            entry<Create> { createKey ->
                CreationScreen(
                    createKey.fileName,
                    onCameraPressed = {
                        backStack.add(Camera)
                    },
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onAboutPressed = {
                        backStack.add(About)
                    },
                )
            }
            entry<About> {
                AboutScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                )
            }
        },
    )
    if (showSplash) {
        ColorSplashTransitionScreen(
            startPoint = positionReveal,
            onTransitionFinished = {
                showSplash = false
            },
            onTransitionMidpoint = {
                backStack.add(Create())
            },
        )
    }
}
