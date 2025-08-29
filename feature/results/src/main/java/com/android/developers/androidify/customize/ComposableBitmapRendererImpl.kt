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

import android.app.Application
import android.app.Presentation
import android.content.Context
import android.content.Context.DISPLAY_SERVICE
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.view.Display
import android.view.Surface
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

interface ComposableBitmapRenderer {

    suspend fun renderComposableToBitmap(canvasSize: Size, composableContent: @Composable () -> Unit): Bitmap?
}

/**
 * Use a virtual display to capture composable content thats on a display.
 * This is necessary because Compose doesn't yet support offscreen bitmap creation (https://issuetracker.google.com/298037598)
 *
 * Original source: https://gist.github.com/iamcalledrob/871568679ad58e64959b097d4ef30738
 * Adapted to use new GraphicsLayer commands (record and toBitmap())
 *     Usage example:
 *     val offscreenBitmapManager = OffscreenBitmapManager(context)
 *     val bitmap = offscreenBitmapManager.renderComposableToBitmap {
 *              ImageResult() // etc
 *              }
 */
@Singleton
class ComposableBitmapRendererImpl @Inject constructor(private val application: Application) : ComposableBitmapRenderer {

    private suspend fun <T> useVirtualDisplay(callback: suspend (display: Display) -> T): T? {
        val texture = SurfaceTexture(false)
        val surface = Surface(texture)
        val virtualDisplay: VirtualDisplay? =
            (application.getSystemService(DISPLAY_SERVICE) as DisplayManager).createVirtualDisplay(
                "virtualDisplay",
                1,
                1,
                72,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
            )

        val result = callback(virtualDisplay!!.display)
        virtualDisplay.release()
        surface.release()
        texture.release()
        return result
    }

    override suspend fun renderComposableToBitmap(canvasSize: Size, composableContent: @Composable () -> Unit): Bitmap? {
        val bitmap = useVirtualDisplay { display ->
            val outputDensity = Density(1f)

            val logicalHeightDp = canvasSize.height.dp
            val logicalWidthDp = canvasSize.width.dp

            val captureDpSize = DpSize(width = logicalWidthDp, height = logicalHeightDp)

            captureComposable(
                context = application,
                size = captureDpSize,
                density = outputDensity,
                display = display,
            ) {
                LaunchedEffect(Unit) {
                    capture()
                }
                composableContent()
            }
        }
        return bitmap
    }
    private data class CaptureComposableScope(val capture: () -> Unit)

    private fun Size.roundedToIntSize(): IntSize =
        IntSize(width.toInt(), height.toInt())

    private class EmptySavedStateRegistryOwner : SavedStateRegistryOwner {
        private val controller = SavedStateRegistryController.create(this).apply {
            performRestore(null)
        }

        private val lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()

        override val lifecycle: Lifecycle
            get() =
                object : Lifecycle() {
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    override fun addObserver(observer: LifecycleObserver) {
                        lifecycleOwner?.lifecycle?.addObserver(observer)
                    }

                    @Suppress("UNNECESSARY_SAFE_CALL")
                    override fun removeObserver(observer: LifecycleObserver) {
                        lifecycleOwner?.lifecycle?.removeObserver(observer)
                    }

                    override val currentState = State.INITIALIZED
                }

        override val savedStateRegistry: SavedStateRegistry
            get() = controller.savedStateRegistry
    }

    /** Captures composable content, by default using a hidden window on the default display.
     *
     *  Be sure to invoke capture() within the composable content (e.g. in a LaunchedEffect) to perform the capture.
     *  This gives some level of control over when the capture occurs, so it's possible to wait for async resources */
    private suspend fun captureComposable(
        context: Context,
        size: DpSize,
        density: Density = Density(density = 1f),
        display: Display = (context.getSystemService(DISPLAY_SERVICE) as DisplayManager)
            .getDisplay(Display.DEFAULT_DISPLAY),
        content: @Composable CaptureComposableScope.() -> Unit,
    ): Bitmap {
        val presentation = Presentation(context.applicationContext, display).apply {
            window?.decorView?.let { view ->
                view.setViewTreeLifecycleOwner(ProcessLifecycleOwner.get())
                view.setViewTreeSavedStateRegistryOwner(EmptySavedStateRegistryOwner())
                view.alpha =
                    0f // If using default display, to ensure this does not appear on top of content.
            }
        }

        val composeView = ComposeView(context).apply {
            val intSize = with(density) { size.toSize().roundedToIntSize() }
            require(intSize.width > 0 && intSize.height > 0) { "pixel size must not have zero dimension" }

            layoutParams = ViewGroup.LayoutParams(intSize.width, intSize.height)
        }

        presentation.setContentView(composeView, composeView.layoutParams)
        presentation.show()

        val androidBitmap = suspendCancellableCoroutine { continuation ->
            composeView.setContent {
                val coroutineScope = rememberCoroutineScope()
                val graphicsLayer = rememberGraphicsLayer()
                Box(
                    modifier = Modifier
                        .size(size)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        },
                ) {
                    CaptureComposableScope(
                        capture = {
                            coroutineScope.launch {
                                val composeImageBitmap = graphicsLayer.toImageBitmap()
                                continuation.resumeWith(Result.success(composeImageBitmap.asAndroidBitmap()))
                            }
                        },
                    ).content()
                }
            }
        }
        presentation.dismiss()
        return androidBitmap
    }
}
