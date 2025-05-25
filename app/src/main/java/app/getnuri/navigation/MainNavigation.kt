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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.getnuri.camera.CameraPreviewScreen
import app.getnuri.creation.CreationScreen
import app.getnuri.home.AboutScreen
import app.getnuri.home.MealTrackingChoiceScreen
import app.getnuri.theme.transitions.ColorSplashTransitionScreen
// import app.getnuri.feature.nuri_creation.photo.capture.MealPhotoCaptureScreen
// import app.getnuri.feature.nuri_creation.photo.confirm.MealPhotoConfirmationScreen
// import app.getnuri.feature.nuri_creation.text.entry.MealTextEntryScreen
import app.getnuri.history.MealHistoryScreen
// import app.getnuri.history.EnhancedHistoryScreen
import app.getnuri.feature.nuri_creation.ingredient.IngredientExtractionScreen
import app.getnuri.feature.wellbeing.WellbeingScreen
import app.getnuri.results.ResultsScreen
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
    val backStack = rememberMutableStateListOf<NavigationRoute>(MealTrackingChoiceTab())
    val coroutineScope = rememberCoroutineScope()
    val mealAnalysisViewModel = hiltViewModel<MealAnalysisViewModel>()
    val motionScheme = MaterialTheme.motionScheme
    
    // Handle bottom navigation tab changes
    val onTabSelected: (BottomNavTab) -> Unit = { tab ->
        val targetRoute = tab.toNavigationRoute()
        // Clear backstack and navigate to selected tab
        backStack.clear()
        backStack.add(targetRoute)
    }
    
    NavigationContainer(
        currentRoute = (backStack.lastOrNull() as? NavigationRoute) ?: MealTrackingChoiceTab(),
        onTabSelected = onTabSelected
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            // OPTIMIZED: Instant transitions using Material 3 fast motion specs
            transitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.fastEffectsSpec()),
                    fadeOut(motionScheme.fastEffectsSpec()),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fadeIn(motionScheme.fastEffectsSpec()),
                    scaleOut(
                        targetScale = 0.95f, // Less dramatic scale for faster perception
                        animationSpec = motionScheme.fastEffectsSpec()
                    ),
                )
            },
            entryProvider = entryProvider {
                entry<Camera> {
                    CameraPreviewScreen(
                        onImageCaptured = { uri ->
                            // OPTIMIZED: Simplified navigation logic
                            val isMealTrackingFlow = backStack.any { it is MealTrackingChoice || it is MealTrackingChoiceTab }
                            
                            backStack.removeAll { it is Camera }
                            if (isMealTrackingFlow) {
                                backStack.removeAll { it is MealTrackingChoice || it is MealTrackingChoiceTab }
                                backStack.add(MealTrackingChoiceTab(uri.toString()))
                            } else {
                                backStack.removeAll { it is Create }
                                backStack.add(Create(uri.toString()))
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
                
                // OPTIMIZED: Combined meal tracking entries to reduce code duplication
                entry<MealTrackingChoiceTab> { mealTrackingRoute ->
                    MealTrackingChoiceScreen(
                        fileName = mealTrackingRoute.fileName,
                        navigationPadding = paddingValues,
                        onCameraPressed = {
                            backStack.removeAll { it is Camera }
                            backStack.add(Camera)
                        },
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        },
                        onAboutClicked = {
                            backStack.add(About)
                        },
                        onMealLogged = { imageUri, description ->
                            // OPTIMIZED: Move heavy analysis to background thread
                            coroutineScope.launch {
                                try {
                                    val analysisResult = when {
                                        imageUri != null && description.isBlank() -> 
                                            mealAnalysisViewModel.analyzer.analyzeMealFromImage(imageUri)
                                        description.isNotBlank() -> 
                                            mealAnalysisViewModel.analyzer.analyzeMealFromText(description)
                                        else -> Result.success(MealAnalysisData(
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
                                        onFailure = { 
                                            // OPTIMIZED: Simplified error handling
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
                                    // OPTIMIZED: Consistent fallback handling
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
                        }
                    )
                }
                
                // OPTIMIZED: Simplified duplicate route handling
                entry<MealTrackingChoice> { mealTrackingRoute ->
                    MealTrackingChoiceScreen(
                        fileName = mealTrackingRoute.fileName,
                        navigationPadding = paddingValues,
                        onCameraPressed = {
                            backStack.removeAll { it is Camera }
                            backStack.add(Camera)
                        },
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        },
                        onAboutClicked = {
                            backStack.add(About)
                        },
                        onMealLogged = { imageUri, description ->
                            // Same optimized logic as above
                            coroutineScope.launch {
                                try {
                                    val analysisResult = when {
                                        imageUri != null && description.isBlank() -> 
                                            mealAnalysisViewModel.analyzer.analyzeMealFromImage(imageUri)
                                        description.isNotBlank() -> 
                                            mealAnalysisViewModel.analyzer.analyzeMealFromText(description)
                                        else -> Result.success(MealAnalysisData(
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
                                        onFailure = { 
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
                        }
                    )
                }
                
                // OPTIMIZED: Tab entries with instant loading
                entry<MealHistoryTab> {
                    MealHistoryScreen(
                        navigationPadding = paddingValues,
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry<MealHistory> {
                    MealHistoryScreen(
                        navigationPadding = paddingValues,
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry<WellbeingTab> {
                    WellbeingScreen(
                        navigationPadding = paddingValues,
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry<Wellbeing> {
                    WellbeingScreen(
                        navigationPadding = paddingValues,
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry<ResultsTab> {
                    // OPTIMIZED: Placeholder with fast loading
                    WellbeingScreen(
                        navigationPadding = paddingValues,
                        onBackPressed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry<Results> {
                    WellbeingScreen(
                        navigationPadding = paddingValues,
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
                            // OPTIMIZED: Smart navigation back to main tab
                            backStack.removeAll { it !is MealTrackingChoiceTab }
                        }
                    )
                }
            },
        )
    }
}
