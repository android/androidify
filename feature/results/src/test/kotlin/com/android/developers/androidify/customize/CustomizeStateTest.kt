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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.android.developers.androidify.results.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomizeStateTest {

    @Test
    fun customizeExportState_defaultValues() {
        val state = CustomizeExportState()
        Assert.assertNull(state.originalImageUrl)
        Assert.assertNull(state.savedUri)
        Assert.assertNull(state.externalSavedUri)
        Assert.assertNull(state.externalOriginalSavedUri)
        Assert.assertEquals(CustomizeTool.Size, state.selectedTool)
        Assert.assertTrue(state.tools.containsAll(CustomizeTool.entries))
        Assert.assertTrue(state.toolState.containsKey(CustomizeTool.Size))
        Assert.assertTrue(state.toolState.containsKey(CustomizeTool.Background))
        Assert.assertTrue(state.toolState[CustomizeTool.Size] is AspectRatioToolState)
        Assert.assertTrue(state.toolState[CustomizeTool.Background] is BackgroundToolState)
        Assert.assertEquals(ExportImageCanvas(), state.exportImageCanvas)
    }

    @Test
    fun aspectRatioToolState_defaultValues() {
        val state = AspectRatioToolState()
        Assert.assertEquals(SizeOption.Square, state.selectedToolOption)
        Assert.assertEquals(
            listOf(
                SizeOption.Square,
                SizeOption.Wallpaper,
                SizeOption.WallpaperTablet,
                SizeOption.Banner,
                SizeOption.SocialHeader,
                SizeOption.Sticker,
            ),
            state.options,
        )
    }

    @Test
    fun backgroundToolState_defaultValues() {
        val state = BackgroundToolState()
        Assert.assertEquals(BackgroundOption.IO, state.selectedToolOption)
        Assert.assertEquals(
            listOf(
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
            state.options,
        )
    }

    @Test
    fun exportImageCanvas_defaultValues() {
        val canvas = ExportImageCanvas()
        Assert.assertNull(canvas.imageBitmap)
        Assert.assertEquals(SizeOption.Square, canvas.aspectRatioOption)
        Assert.assertEquals(Size(1000f, 1000f), canvas.canvasSize)
        Assert.assertNull(canvas.mainImageUri)
        Assert.assertEquals(Size(600f, 600f), canvas.imageSize)
        Assert.assertEquals(Offset(200f, 160f), canvas.imageOffset)
        Assert.assertEquals(0f, canvas.imageRotation)
        Assert.assertEquals(Size(1024f, 1024f), canvas.imageOriginalBitmapSize)
        Assert.assertEquals(BackgroundOption.IO, canvas.selectedBackgroundOption)
        Assert.assertEquals(com.android.developers.androidify.results.R.drawable.background_square_blocks, canvas.selectedBackgroundDrawable)
        Assert.assertTrue(canvas.includeWatermark)
    }

    @Test
    fun updateAspectRatioAndBackground_Square_None() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.None,
            sizeOption = SizeOption.Square,
        )

        Assert.assertEquals(SizeOption.Square, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.None, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(SizeOption.Square.dimensions, updatedCanvas.canvasSize)
        Assert.assertNull(updatedCanvas.selectedBackgroundDrawable)
        Assert.assertEquals(0f, updatedCanvas.imageRotation)

        // For None background, imageSize should match canvasSize and offset be Zero
        Assert.assertEquals(updatedCanvas.canvasSize, updatedCanvas.imageSize)
        Assert.assertEquals(Offset.Companion.Zero, updatedCanvas.imageOffset)
    }

    @Test
    fun updateAspectRatioAndBackground_Square_IO() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.IO,
            sizeOption = SizeOption.Square,
        )
        val newCanvasSize = SizeOption.Square.dimensions

        Assert.assertEquals(SizeOption.Square, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.IO, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertEquals(
            R.drawable.background_square_blocks,
            updatedCanvas.selectedBackgroundDrawable,
        )
        Assert.assertEquals(0f, updatedCanvas.imageRotation)
        Assert.assertEquals(
            Size(newCanvasSize.width * 0.6f, newCanvasSize.width * 0.6f),
            updatedCanvas.imageSize,
        )
        Assert.assertEquals(
            Offset(newCanvasSize.width * 0.2f, newCanvasSize.height * 0.16f),
            updatedCanvas.imageOffset,
        )
    }

    @Test
    fun updateAspectRatioAndBackground_Banner_Lightspeed() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Lightspeed,
            sizeOption = SizeOption.Banner,
        )
        val newCanvasSize = SizeOption.Banner.dimensions

        Assert.assertEquals(SizeOption.Banner, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.Lightspeed, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertEquals(
            R.drawable.background_banner_lightspeed,
            updatedCanvas.selectedBackgroundDrawable,
        )
        Assert.assertEquals(-11f, updatedCanvas.imageRotation)
        Assert.assertEquals(
            Size(newCanvasSize.width * 0.26f, newCanvasSize.width * 0.26f),
            updatedCanvas.imageSize,
        )
        Assert.assertEquals(
            Offset(newCanvasSize.width * 0.51f, newCanvasSize.height * -0.03f),
            updatedCanvas.imageOffset,
        )
    }

    @Test
    fun updateAspectRatioAndBackground_SocialHeader_Plain() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Plain,
            sizeOption = SizeOption.SocialHeader,
        )
        val newCanvasSize = SizeOption.SocialHeader.dimensions

        Assert.assertEquals(SizeOption.SocialHeader, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.Plain, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertEquals(
            R.drawable.background_social_header_plain,
            updatedCanvas.selectedBackgroundDrawable,
        )
        Assert.assertEquals(-9f, updatedCanvas.imageRotation)
        Assert.assertEquals(
            Size(newCanvasSize.width * 0.26f, newCanvasSize.width * 0.3f),
            updatedCanvas.imageSize,
        )
        Assert.assertEquals(
            Offset(newCanvasSize.width * 0.49f, newCanvasSize.height * 0.01f),
            updatedCanvas.imageOffset,
        )
    }

    @Test
    fun updateAspectRatioAndBackground_Wallpaper_IO() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.IO,
            sizeOption = SizeOption.Wallpaper,
        )
        val newCanvasSize = SizeOption.Wallpaper.dimensions

        Assert.assertEquals(SizeOption.Wallpaper, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.IO, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertEquals(
            R.drawable.background_wallpaper_shapes,
            updatedCanvas.selectedBackgroundDrawable,
        )
        Assert.assertEquals(-9f, updatedCanvas.imageRotation)
        Assert.assertEquals(
            Size(newCanvasSize.width * 1.1f, newCanvasSize.width * 1.3f),
            updatedCanvas.imageSize,
        )
        Assert.assertEquals(
            Offset(newCanvasSize.width * -0.02f, newCanvasSize.height * 0.1f),
            updatedCanvas.imageOffset,
        )
    }

    @Test
    fun updateAspectRatioAndBackground_Sticker() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.None,
            sizeOption = SizeOption.Sticker,
        )
        val newCanvasSize = SizeOption.Sticker.dimensions

        Assert.assertEquals(SizeOption.Sticker, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.None, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertNull(updatedCanvas.selectedBackgroundDrawable)
        Assert.assertEquals(0f, updatedCanvas.imageRotation)
        Assert.assertEquals(newCanvasSize, updatedCanvas.imageSize)
        Assert.assertEquals(Offset.Companion.Zero, updatedCanvas.imageOffset)
    }

    @Test
    fun updateAspectRatioAndBackground_WallpaperTablet_None() {
        val initialCanvas = ExportImageCanvas()
        val updatedCanvas = initialCanvas.updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.None,
            sizeOption = SizeOption.WallpaperTablet,
        )
        val newCanvasSize = SizeOption.WallpaperTablet.dimensions

        Assert.assertEquals(SizeOption.WallpaperTablet, updatedCanvas.aspectRatioOption)
        Assert.assertEquals(BackgroundOption.None, updatedCanvas.selectedBackgroundOption)
        Assert.assertEquals(newCanvasSize, updatedCanvas.canvasSize)
        Assert.assertNull(updatedCanvas.selectedBackgroundDrawable)
        Assert.assertEquals(0f, updatedCanvas.imageRotation)
        Assert.assertEquals(newCanvasSize, updatedCanvas.imageSize)
        Assert.assertEquals(Offset.Companion.Zero, updatedCanvas.imageOffset)
    }
}
