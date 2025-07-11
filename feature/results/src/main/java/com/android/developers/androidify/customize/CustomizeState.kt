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
package com.android.developers.androidify.customize

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Size

data class CustomizeExportState(
    val originalImageUrl: Uri? = null,
    val savedUri: Uri? = null,
    val externalSavedUri: Uri? = null,
    val externalOriginalSavedUri: Uri? = null,
    val selectedTool: CustomizeTool = CustomizeTool.Size,
    val tools: List<CustomizeTool> = CustomizeTool.entries,
    val toolState: Map<CustomizeTool, ToolState> = mapOf(
        CustomizeTool.Size to AspectRatioToolState(),
        CustomizeTool.Background to BackgroundToolState(),
    ),
    val exportImageCanvas: ExportImageCanvas = ExportImageCanvas(),
)
interface ToolState {
    val selectedToolOption: ToolOption
    val options: List<ToolOption>
}
data class AspectRatioToolState(
    override val selectedToolOption: SizeOption = SizeOption.Square,
    override val options: List<SizeOption> = listOf(
        SizeOption.Square,
        SizeOption.Banner,
    ),
) : ToolState

data class BackgroundToolState(
    override val selectedToolOption: BackgroundOption = BackgroundOption.None,
    override val options: List<BackgroundOption> = listOf(
        BackgroundOption.None,
        BackgroundOption.Lightspeed,
        BackgroundOption.IO,
    ),
) : ToolState

data class ExportImageCanvas(
    val imageBitmap: Bitmap? = null,
    val aspectRatioOption: SizeOption = SizeOption.Square,
    val canvasSize: Size = Size(1000f, 1000f),
    val mainImageUri: Uri? = null,
    val imageOriginalBitmapSize: Size? = Size(1024f, 1024f),
    val selectedBackgroundOption: BackgroundOption = BackgroundOption.None,
    val includeWatermark: Boolean = true
)