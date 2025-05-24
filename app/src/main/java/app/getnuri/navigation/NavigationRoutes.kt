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
@file:OptIn(ExperimentalSerializationApi::class)

package app.getnuri.navigation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

interface NavigationRoute

@Serializable
data object Home : NavigationRoute

@Serializable
data class Create(val fileName: String? = null, val prompt: String? = null) : NavigationRoute

@Serializable
object Camera : NavigationRoute

@Serializable
object About : NavigationRoute

// New Nuri meal tracking routes
@Serializable
object MealTrackingChoice : NavigationRoute

@Serializable
object MealPhotoCapture : NavigationRoute

@Serializable
data class MealPhotoConfirmation(val imageUri: String) : NavigationRoute

@Serializable
object MealTextEntry : NavigationRoute

@Serializable
object MealHistory : NavigationRoute

@Serializable
data class FeedbackEntry(val mealId: Long) : NavigationRoute

@Serializable
data class IngredientExtraction(
    val mealTitle: String = "Avocado Toast with Poached Eggs",
    val mealImageUri: String? = null,
    val extractedIngredients: List<String> = listOf(
        "Sourdough Bread | 180g",
        "Avocado | 120g", 
        "Poached Eggs | 100g",
        "Cherry Tomatoes | 120g",
        "Salt | 1g",
        "Pepper | 1g"
    ),
    val extractedQuantities: List<String> = emptyList()
) : NavigationRoute
