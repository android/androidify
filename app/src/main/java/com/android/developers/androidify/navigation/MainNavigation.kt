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

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.android.developers.androidify.camera.CameraPreviewScreen
import com.android.developers.androidify.creation.CreationScreen
import com.android.developers.androidify.customize.CustomizeAndExportScreen
import com.android.developers.androidify.home.AboutScreen
import com.android.developers.androidify.home.HomeScreen
import com.android.developers.androidify.results.ResultsScreen
import com.android.developers.androidify.theme.transitions.ColorSplashTransitionScreen
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

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
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
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
                        backStack.removeAll { it is Create }
                        backStack.add(Create(uri.toString()))
                        backStack.removeAll { it is Camera }
                    },
                )
            }
            entry<Create> { createKey ->
                CreationScreen(
                    createKey.fileName,
                    onCameraPressed = {
                        backStack.removeAll { it is Camera }
                        backStack.add(Camera)
                    },
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onAboutPressed = {
                        backStack.add(About)
                    },
                    onImageCreated = { resultImageUri, prompt, originalImageUri ->
                        backStack.removeAll{ it is ImageResult}
                        backStack.add(
                            ImageResult(
                                result = resultImageUri.toString(),
                                prompt = prompt,
                                originalImageUri = originalImageUri?.toString()
                            )
                        )
                    }
                )
            }
            entry<ImageResult> { resultKey ->
                ResultsScreen(
                    resultImageUri = resultKey.result.toUri(),
                    originalImageUri = resultKey.originalImageUri?.toUri(),
                    onNextPress = { resultImageUri, originalImageUri ->
                        backStack.removeAll{ it is ImageResult}
                        backStack.add(
                            ShareResult(
                                resultUri = resultImageUri.toString(),
                                originalImageUri = originalImageUri?.toString()
                            )
                        )
                    },
                    promptText = resultKey.prompt,
                    onAboutPress = {
                        backStack.add(About)
                    },
                    onBackPress = {
                        backStack.removeLastOrNull()
                        backStack.add(Create(fileName = resultKey.originalImageUri, prompt = resultKey.prompt))
                    }
                )
            }
            entry<ShareResult> { shareKey ->
                CustomizeAndExportScreen(
                    resultImageUri = shareKey.resultUri.toUri(),
                    originalImageUri = shareKey.originalImageUri?.toUri(),
                    onBackPress = {
                        backStack.removeLastOrNull()
                    },
                    onInfoPress = {
                        backStack.add(About)
                    }
                )
            }
            entry<About> {
                val context = LocalContext.current
                AboutScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onLicensesClicked = {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
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
                backStack.add(Create(fileName = null))
            },
        )
    }
}
