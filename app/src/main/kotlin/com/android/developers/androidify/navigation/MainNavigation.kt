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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.android.developers.androidify.camera.CameraPreviewScreen
import com.android.developers.androidify.creation.CreationScreen
import com.android.developers.androidify.creation.CreationViewModel
import com.android.developers.androidify.customize.CustomizeAndExportScreen
import com.android.developers.androidify.customize.CustomizeExportViewModel
import com.android.developers.androidify.home.AboutScreen
import com.android.developers.androidify.home.HomeScreen
import com.android.developers.androidify.results.ResultsScreen
import com.android.developers.androidify.results.ResultsViewModel
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
            rememberSaveableStateHolderNavEntryDecorator(),
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
                        backStack.add(Create(uri))
                        backStack.removeAll { it is Camera }
                    },
                )
            }
            entry<Create> { createKey ->
                val creationViewModel = hiltViewModel<CreationViewModel, CreationViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            originalImageUrl = createKey.fileName,
                        )
                    },
                )
                CreationScreen(
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
                        backStack.removeAll { it is Result }
                        backStack.add(
                            Result(
                                resultImageUri = resultImageUri,
                                prompt = prompt,
                                originalImageUri = originalImageUri,
                            ),
                        )
                    },
                    creationViewModel = creationViewModel,
                )
            }
            entry<Result> { resultKey ->
                val resultsViewModel = hiltViewModel<ResultsViewModel, ResultsViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            resultImageUrl = resultKey.resultImageUri,
                            originalImageUrl = resultKey.originalImageUri,
                            promptText = resultKey.prompt,
                        )
                    },
                )
                ResultsScreen(
                    onNextPress = { resultImageUri, originalImageUri ->
                        backStack.add(
                            CustomizeExport(
                                resultImageUri = resultImageUri,
                                originalImageUri = originalImageUri,
                            ),
                        )
                    },
                    onAboutPress = {
                        backStack.add(About)
                    },
                    onBackPress = {
                        backStack.removeLastOrNull()
                    },
                    viewModel = resultsViewModel,
                )
            }
            entry<CustomizeExport> { shareKey ->
                val customizeExportViewModel = hiltViewModel<CustomizeExportViewModel, CustomizeExportViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            resultImageUrl = shareKey.resultImageUri,
                            originalImageUrl = shareKey.originalImageUri,
                        )
                    },
                )
                CustomizeAndExportScreen(
                    onBackPress = {
                        backStack.removeLastOrNull()
                    },
                    onInfoPress = {
                        backStack.add(About)
                    },
                    viewModel = customizeExportViewModel,
                )
            }
            entry<About> {
                val context = LocalContext.current
                val uriHandler = LocalUriHandler.current
                AboutScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onLicensesClicked = {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    },
                    onPrivacyClicked = {
                        uriHandler.openUri("https://policies.google.com/privacy")
                    },
                    onTermsClicked = {
                        uriHandler.openUri("https://policies.google.com/terms")
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
