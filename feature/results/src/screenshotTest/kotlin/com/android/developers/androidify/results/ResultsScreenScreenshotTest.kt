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
package com.android.developers.androidify.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.util.AdaptivePreview
import com.android.developers.androidify.util.SmallPhonePreview

class ResultsScreenScreenshotTest {

    @AdaptivePreview
    @Preview(showBackground = true)
    @Composable
    fun ResultsScreen_AdaptivePreview() {
        val mockBitmap = createMockBitmap()
        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageUri = "test://mockbitmap/${mockBitmap.hashCode()}".toUri(),
                    promptText = "wearing a hat with straw hair",
                ),
            )
        }
        CompositionLocalProvider(value = LocalInspectionMode provides true) {
            AndroidifyTheme {
                ResultsScreenContents(
                    state = state.value,
                    snackbarHostState = SnackbarHostState(),
                    onCustomizeShareClicked = {},
                    selectedResultOption = ResultOption.ResultImage,
                    onResultOptionSelected = {},
                    wasPromptUsed = false,
                    onBackPress = {},
                    layoutType = ResultsLayoutType.Verbose, // Replicates ResultsScreenPreview
                    onAboutPress = {},
                )
            }
        }
    }

    @SmallPhonePreview
    @Preview(showBackground = true)
    @Composable
    fun ResultsScreen_SmallPreview() {
        val mockBitmap = createMockBitmap()
        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageUri = "test://mockbitmap/${mockBitmap.hashCode()}".toUri(),
                    promptText = "wearing a hat with straw hair",
                ),
            )
        }
        CompositionLocalProvider(value = LocalInspectionMode provides true) {
            AndroidifyTheme {
                ResultsScreenContents(
                    state = state.value,
                    snackbarHostState = SnackbarHostState(),
                    onCustomizeShareClicked = {},
                    selectedResultOption = ResultOption.ResultImage,
                    onResultOptionSelected = {},
                    wasPromptUsed = false,
                    onBackPress = {},
                    layoutType = ResultsLayoutType.Constrained, // Replicates ResultsScreenPreviewSmall
                    onAboutPress = {},
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ResultsScreen_OriginalInputPreview() {
        val mockBitmap = createMockBitmap()
        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageUri = "test://mockbitmap/${mockBitmap.hashCode()}".toUri(),
                    promptText = "wearing a hat with straw hair",
                ),
            )
        }
        CompositionLocalProvider(value = LocalInspectionMode provides true) {
            AndroidifyTheme {
                ResultsScreenContents(
                    state = state.value,
                    snackbarHostState = SnackbarHostState(),
                    onCustomizeShareClicked = {},
                    selectedResultOption = ResultOption.OriginalInput, // Set the non-default option
                    onResultOptionSelected = {},
                    wasPromptUsed = false,
                    onBackPress = {},
                    layoutType = ResultsLayoutType.Verbose,
                    onAboutPress = {},
                )
            }
        }
    }

    // Helper function to create a consistent mock bitmap
    private fun createMockBitmap(): Bitmap {
        val width = 200
        val height = 200
        val mockBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mockBitmap)
        val paint = Paint()
        val gradient = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.RED,
            Color.BLUE,
            Shader.TileMode.CLAMP,
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return mockBitmap
    }
}
