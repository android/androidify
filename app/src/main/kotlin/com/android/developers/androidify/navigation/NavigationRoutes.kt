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

package com.android.developers.androidify.navigation

import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

sealed interface NavigationRoute

@Serializable
data object Home : NavigationRoute

@Serializable
data class Create(
    @Serializable(with = UriSerializer::class) val fileName: Uri? = null,
    val prompt: String? = null,
) : NavigationRoute

@Serializable
object Camera : NavigationRoute

@Serializable
object About : NavigationRoute

/**
 * Represents the result of an image generation process, used for navigation.
 *
 * @param resultImageUri The URI of the generated image.
 * @param originalImageUri The URI of the original image used as a base for generation, if any.
 * @param prompt The text prompt used to generate the image, if any.
 */
@Serializable
data class Result(
    @Serializable(with = UriSerializer::class) val resultImageUri: Uri,
    @Serializable(with = UriSerializer::class) val originalImageUri: Uri? = null,
    val prompt: String? = null,
) : NavigationRoute

/**
 * Represents the navigation route to the screen for customizing and exporting a generated image.
 *
 * @param resultImageUri The URI of the generated image to be customized.
 * @param originalImageUri The URI of the original image, passed along for context.
 */
@Serializable
data class CustomizeExport(
    @Serializable(with = UriSerializer::class) val resultImageUri: Uri,
    @Serializable(with = UriSerializer::class) val originalImageUri: Uri?,
) : NavigationRoute
