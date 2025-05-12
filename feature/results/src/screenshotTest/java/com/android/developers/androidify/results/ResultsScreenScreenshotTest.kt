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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme

class ResultsScreenScreenshotTest {

    @Preview(showBackground = true)
    @Composable
    fun ResultsScreenScreenshot() {
        val width = 200
        val height = 200
        val mockBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mockBitmap)
        val paint = Paint()
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            Color.RED, Color.BLUE, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val state = remember {
            mutableStateOf(
                ResultState(
                    resultImageBitmap = mockBitmap,
                    promptText = "a test prompt",
                ),
            )
        }
        CompositionLocalProvider(value = LocalInspectionMode provides true) {
            AndroidifyTheme {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    verboseLayout = false,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }
    }
}
