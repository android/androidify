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
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

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
    val showImageEditProgress: Boolean = false,
)

interface ToolState {
    val selectedToolOption: ToolOption
    val options: List<ToolOption>
}

data class AspectRatioToolState(
    override val selectedToolOption: SizeOption = SizeOption.Square,
    override val options: List<SizeOption> = listOf(
        SizeOption.Square,
        SizeOption.Wallpaper,
        SizeOption.WallpaperTablet,
        SizeOption.Banner,
        SizeOption.SocialHeader,
        SizeOption.Sticker,
    ),
) : ToolState

data class BackgroundToolState(
    override val selectedToolOption: BackgroundOption = BackgroundOption.IO,
    override val options: List<BackgroundOption> = listOf(
        BackgroundOption.None,
        BackgroundOption.Plain,
        BackgroundOption.Lightspeed,
        BackgroundOption.IO,
        BackgroundOption.MusicLover,
        BackgroundOption.PoolMaven,
        BackgroundOption.SoccerFanatic,
        BackgroundOption.StarGazer,
        BackgroundOption.FitnessBuff,
        BackgroundOption.Fandroid,
        BackgroundOption.GreenThumb,
        BackgroundOption.Gamer,
        BackgroundOption.Jetsetter,
        BackgroundOption.Chef,
    ),
) : ToolState

data class ExportImageCanvas(
    val imageUri: Uri? = null,
    val imageBitmap: Bitmap? = null,
    val imageBitmapRemovedBackground: Bitmap? = null,
    val aspectRatioOption: SizeOption = SizeOption.Square,
    val canvasSize: Size = Size(1000f, 1000f),
    val mainImageUri: Uri? = null,
    val imageSize: Size = Size(600f, 600f),
    val imageOffset: Offset = Offset(canvasSize.width * 0.2f, canvasSize.height * 0.16f),
    val imageRotation: Float = 0f,
    val imageOriginalBitmapSize: Size? = Size(1024f, 1024f),
    val selectedBackgroundOption: BackgroundOption = BackgroundOption.IO,
    @param:DrawableRes
    val selectedBackgroundDrawable: Int? = com.android.developers.androidify.results.R.drawable.background_square_blocks,
    val includeWatermark: Boolean = true,
    val imageWithEdit: Bitmap? = null,
    val backgroundColor: Color? = Color.White,
    val showSticker: Boolean = false,
) {
    fun updateAspectRatioAndBackground(
        backgroundOption: BackgroundOption,
        sizeOption: SizeOption,
    ): ExportImageCanvas {
        val newCanvasSize = sizeOption.dimensions
        var imageSize: Size

        var offset = Offset.Zero
        var image: Int?
        var rotation: Float
        var backgroundColor: Color? = Color.White
        var showSticker = false
        when (sizeOption) {
            SizeOption.Square -> {
                offset = Offset(newCanvasSize.width * 0.2f, newCanvasSize.height * 0.16f)
                imageSize = Size(newCanvasSize.width * 0.6f, newCanvasSize.width * 0.6f)
                rotation = 0f
                image = when (backgroundOption) {
                    BackgroundOption.IO -> com.android.developers.androidify.results.R.drawable.background_square_blocks
                    BackgroundOption.Lightspeed -> com.android.developers.androidify.results.R.drawable.background_square_lightspeed
                    BackgroundOption.None -> {
                        offset = Offset(0f, 0f)
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                    BackgroundOption.Plain -> com.android.developers.androidify.results.R.drawable.background_square_none
                    else -> {
                        offset = Offset(0f, 0f)
                        rotation = 0f
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                }
            }
            SizeOption.Banner -> {
                offset = Offset(newCanvasSize.width * 0.51f, newCanvasSize.height * -0.03f)
                imageSize = Size(newCanvasSize.width * 0.26f, newCanvasSize.width * 0.26f)
                rotation = -11f
                image = when (backgroundOption) {
                    BackgroundOption.IO -> com.android.developers.androidify.results.R.drawable.background_banner_square
                    BackgroundOption.Lightspeed -> com.android.developers.androidify.results.R.drawable.background_banner_lightspeed
                    BackgroundOption.None -> {
                        offset = Offset(0f, 0f)
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)

                        rotation = 0f
                        null
                    }
                    BackgroundOption.Plain -> com.android.developers.androidify.results.R.drawable.background_banner_plain
                    else -> {
                        offset = Offset(0f, 0f)
                        rotation = 0f
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                }
            }
            SizeOption.SocialHeader -> {
                offset = Offset(newCanvasSize.width * 0.49f, newCanvasSize.height * 0.01f)
                imageSize = Size(newCanvasSize.width * 0.26f, newCanvasSize.width * 0.3f)
                rotation = -9f
                image = when (backgroundOption) {
                    BackgroundOption.IO -> com.android.developers.androidify.results.R.drawable.background_social_header_shape
                    BackgroundOption.Lightspeed -> com.android.developers.androidify.results.R.drawable.background_social_header_lightspeed
                    BackgroundOption.None -> {
                        offset = Offset(0f, 0f)
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        rotation = 0f
                        null
                    }
                    BackgroundOption.Plain -> com.android.developers.androidify.results.R.drawable.background_social_header_plain
                    else -> {
                        offset = Offset(0f, 0f)
                        rotation = 0f
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                }
            }

            SizeOption.Wallpaper -> {
                offset = Offset(newCanvasSize.width * -0.02f, newCanvasSize.height * 0.1f)
                imageSize = Size(newCanvasSize.width * 1.1f, newCanvasSize.width * 1.3f)
                rotation = -9f
                image = when (backgroundOption) {
                    BackgroundOption.IO -> com.android.developers.androidify.results.R.drawable.background_wallpaper_shapes
                    BackgroundOption.Lightspeed -> com.android.developers.androidify.results.R.drawable.background_wallpaper_lightspeed
                    BackgroundOption.None -> {
                        offset = Offset(0f, 0f)
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        rotation = 0f
                        null
                    }
                    BackgroundOption.Plain -> com.android.developers.androidify.results.R.drawable.background_wallpaper_plain
                    else -> {
                        offset = Offset(0f, 0f)
                        rotation = 0f
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                }
            }

            SizeOption.WallpaperTablet -> {
                offset = Offset(newCanvasSize.width * 0.24f, newCanvasSize.height * 0.06f)
                imageSize = Size(newCanvasSize.width * 0.52f, newCanvasSize.width * 0.52f)
                rotation = -10f
                image = when (backgroundOption) {
                    BackgroundOption.IO -> com.android.developers.androidify.results.R.drawable.background_wallpaper_tablet_shapes
                    BackgroundOption.Lightspeed -> com.android.developers.androidify.results.R.drawable.background_wallpaper_tablet_lightspeed
                    BackgroundOption.None -> {
                        offset = Offset(0f, 0f)
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        rotation = 0f
                        null
                    }
                    BackgroundOption.Plain -> com.android.developers.androidify.results.R.drawable.background_wallpaper_tablet_light
                    else -> {
                        offset = Offset(0f, 0f)
                        rotation = 0f
                        imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                        null
                    }
                }
            }
            SizeOption.Sticker -> {
                offset = Offset(0f, 0f)
                imageSize = Size(newCanvasSize.width, newCanvasSize.height)
                rotation = 0f
                backgroundColor = null
                showSticker = true
                image = null
            }
        }
        return copy(
            selectedBackgroundDrawable = image,
            imageRotation = rotation,
            imageSize = imageSize,
            imageOffset = offset,
            canvasSize = newCanvasSize,
            aspectRatioOption = sizeOption,
            selectedBackgroundOption = if (SizeOption.Sticker == sizeOption) BackgroundOption.None else backgroundOption,
            backgroundColor = backgroundColor,
            showSticker = showSticker,
        )
    }
}
