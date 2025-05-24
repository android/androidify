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

package app.getnuri.navigation

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.getnuri.camera.CameraPreviewScreen
import app.getnuri.creation.CreationScreen
import app.getnuri.home.AboutScreen
import app.getnuri.home.HomeScreen
import app.getnuri.home.MealTrackingChoiceScreen
import app.getnuri.theme.transitions.ColorSplashTransitionScreen
// import app.getnuri.feature.nuri_creation.photo.capture.MealPhotoCaptureScreen
// import app.getnuri.feature.nuri_creation.photo.confirm.MealPhotoConfirmationScreen
// import app.getnuri.feature.nuri_creation.text.entry.MealTextEntryScreen
import app.getnuri.feature.history.MealHistoryScreen
import app.getnuri.feature.nuri_creation.ingredient.IngredientExtractionScreen
import app.getnuri.data.NuriMealAnalyzer
import app.getnuri.data.model.MealAnalysisData
// import app.getnuri.feature.feedback.entry.FeedbackEntryScreen
import android.net.Uri
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

// Simple ViewModel wrapper to inject NuriMealAnalyzer
@HiltViewModel
class MealAnalysisViewModel @Inject constructor(
    val analyzer: NuriMealAnalyzer
) : ViewModel()

@ExperimentalMaterial3ExpressiveApi
@Composable
fun MainNavigation() {
    val backStack = rememberMutableStateListOf<NavigationRoute>(Home)
    val coroutineScope = rememberCoroutineScope()
    val mealAnalysisViewModel = hiltViewModel<MealAnalysisViewModel>()
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
                    onHistoryClicked = {
                        backStack.add(MealHistory)
                    },
                )
            }
            entry<Camera> {
                CameraPreviewScreen(
                    onImageCaptured = { uri ->
                        // Check if we're in meal tracking flow
                        val isMealTrackingFlow = backStack.any { it is MealTrackingChoice }
                        
                        if (isMealTrackingFlow) {
                            // For meal tracking flow, go back to MealTrackingChoice with the captured image
                            backStack.removeAll { it is Camera }
                            backStack.removeAll { it is MealTrackingChoice }
                            backStack.add(MealTrackingChoice(uri.toString()))
                        } else {
                            // Original creation flow
                            backStack.removeAll { it is Create }
                            backStack.add(Create(uri.toString()))
                            backStack.removeAll { it is Camera }
                        }
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
                )
            }
            entry<About> {
                AboutScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                )
            }
            
            // New Nuri meal tracking entries
            entry<MealTrackingChoice> { mealTrackingRoute ->
                MealTrackingChoiceScreen(
                    fileName = mealTrackingRoute.fileName,
                    onCameraPressed = {
                        backStack.removeAll { it is Camera }
                        backStack.add(Camera)
                    },
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onMealLogged = { imageUri, description ->
                        // Use real Firebase Gemini analysis instead of mock data
                        coroutineScope.launch {
                            try {
                                val analysisResult = if (imageUri != null && description.isBlank()) {
                                    // Analyze image
                                    mealAnalysisViewModel.analyzer.analyzeMealFromImage(imageUri)
                                } else if (description.isNotBlank()) {
                                    // Analyze text description
                                    mealAnalysisViewModel.analyzer.analyzeMealFromText(description)
                                } else {
                                    // Fallback to default
                                    Result.success(MealAnalysisData(
                                        extractedIngredients = listOf(
                                            "Mixed Vegetables | 200g",
                                            "Protein Source | 100g",
                                            "Grains | 80g"
                                        ),
                                        potentialTriggers = emptyList()
                                    ))
                                }
                                
                                analysisResult.fold(
                                    onSuccess = { analysisData ->
                                        backStack.add(IngredientExtraction(
                                            mealTitle = if (description.isNotBlank()) description else "Your Meal",
                                            mealImageUri = imageUri?.toString(),
                                            extractedIngredients = analysisData.extractedIngredients,
                                            potentialTriggers = analysisData.potentialTriggers
                                        ))
                                    },
                                    onFailure = { error ->
                                        // Handle error - for now, use fallback data
                                        backStack.add(IngredientExtraction(
                                            mealTitle = if (description.isNotBlank()) description else "Your Meal",
                                            mealImageUri = imageUri?.toString(),
                                            extractedIngredients = listOf(
                                                "Analysis Error | Unable to analyze meal",
                                                "Please add ingredients manually"
                                            ),
                                            potentialTriggers = emptyList()
                                        ))
                                    }
                                )
                            } catch (e: Exception) {
                                // Handle exception - use fallback data
                                backStack.add(IngredientExtraction(
                                    mealTitle = if (description.isNotBlank()) description else "Your Meal",
                                    mealImageUri = imageUri?.toString(),
                                    extractedIngredients = listOf(
                                        "Analysis Error | Unable to analyze meal",
                                        "Please add ingredients manually"
                                    ),
                                    potentialTriggers = emptyList()
                                ))
                            }
                        }
                    },
                    // Test navigation to ingredient extraction screen
                    onTestIngredientExtraction = {
                        backStack.add(IngredientExtraction())
                    }
                )
            }
            
            entry<MealHistory> {
                MealHistoryScreen(
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            
            entry<IngredientExtraction> { ingredientRoute ->
                IngredientExtractionScreen(
                    mealTitle = ingredientRoute.mealTitle,
                    mealImageUri = ingredientRoute.mealImageUri,
                    analysisData = MealAnalysisData(
                        extractedIngredients = ingredientRoute.extractedIngredients,
                        potentialTriggers = ingredientRoute.potentialTriggers
                    ),
                    onBackPressed = {
                        backStack.removeLastOrNull()
                    },
                    onNextPressed = { finalIngredients ->
                        // TODO: Navigate to next screen in the meal creation flow
                        // For now, just go back to home
                        backStack.removeAll { it !is Home }
                    }
                )
            }
            
            /*
            entry<FeedbackEntry> { feedbackRoute ->
                FeedbackEntryScreen(
                    mealId = feedbackRoute.mealId,
                    onFeedbackSubmitted = {
                        backStack.removeLastOrNull()
                    }
                )
            }
            */
        },
    )
    if (showSplash) {
        ColorSplashTransitionScreen(
            startPoint = positionReveal,
            onTransitionFinished = {
                showSplash = false
            },
            onTransitionMidpoint = {
                backStack.add(MealTrackingChoice())
            },
        )
    }
}
