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
package com.android.developers.androidify.camera

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Surface.ROTATION_90
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import com.android.developers.androidify.camera.xr.CameraLayoutSpatial
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.TertiaryContainer
import com.android.developers.androidify.util.FoldablePreviewParameters
import com.android.developers.androidify.util.FoldablePreviewParametersProvider
import com.android.developers.androidify.util.allowsFullContent
import com.android.developers.androidify.util.isWidthAtLeastMedium
import com.android.developers.androidify.util.shouldShowTabletopLayout
import com.android.developers.androidify.util.supportsTabletop
import com.android.developers.androidify.xr.LocalSpatialCapabilities

@Composable
internal fun CameraLayout(
    modifier: Modifier = Modifier,
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    rearCameraButton: @Composable (modifier: Modifier) -> Unit,
    surfaceAspectRatio: Float,
    xrEnabled: Boolean = false,
    supportsTabletop: Boolean = supportsTabletop(),
    isTabletop: Boolean = false,
) {
    val mContext = LocalContext.current
    val inspection = LocalInspectionMode.current
    var isCameraLeft by remember { mutableStateOf(false) }
    LifecycleStartEffect(Unit) {
        if (inspection) return@LifecycleStartEffect onStopOrDispose { }
        val displayManager = mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayChanged(displayId: Int) {
                val rotation = displayManager.getDisplay(displayId).rotation
                isCameraLeft = rotation == ROTATION_90
            }
            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayRemoved(displayId: Int) {}
        }
        displayManager.registerDisplayListener(displayListener, null)
        onStopOrDispose {
            displayManager.unregisterDisplayListener(displayListener)
        }
    }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(TertiaryContainer),
    ) {
        when {
            xrEnabled && LocalSpatialCapabilities.current.isSpatialUiEnabled -> CameraLayoutSpatial(
                viewfinder,
                captureButton,
                flipCameraButton,
                zoomButton,
                guideText,
                guide,
                surfaceAspectRatio,
            )

            isWidthAtLeastMedium() && shouldShowTabletopLayout(
                supportsTabletop = supportsTabletop,
                isTabletop = isTabletop,
            ) -> TableTopSupportedCameraLayout(
                viewfinder,
                captureButton,
                flipCameraButton,
                zoomButton,
                guideText,
                guide,
                rearCameraButton,
                isTabletop = isTabletop,
            )

            isWidthAtLeastMedium() && maxWidth > maxHeight -> MediumHorizontalCameraLayout(
                viewfinder,
                captureButton,
                flipCameraButton,
                zoomButton,
                guideText,
                guide,
                isCameraLeft,
            )

            this.maxWidth > maxHeight && allowsFullContent() -> CompactHorizontalCameraLayout(
                viewfinder,
                captureButton,
                flipCameraButton,
                zoomButton,
                guideText,
                guide,
            )

            this.maxWidth > maxHeight && !allowsFullContent() -> SubCompactHorizontalCameraLayout(
                viewfinder,
                captureButton,
                flipCameraButton,
                guideText,
                guide,
            )

            else -> VerticalCameraLayout(
                viewfinder,
                captureButton,
                flipCameraButton,
                zoomButton,
                guideText,
                guide,
                rearCameraButton,
            )
        }
    }
}

@Composable
fun CompactHorizontalCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        VerticalControlsLayout(
            captureButton,
            flipCameraButton,
            zoomButton,
            Modifier.weight(1f),
        )

        Box(Modifier.weight(1f)) {
            viewfinder(Modifier)
            guide(Modifier.fillMaxSize())
        }
        Box(
            Modifier
                .fillMaxHeight()
                .weight(1f),
        ) {
            guideText(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
fun SubCompactHorizontalCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        VerticalControlsLayout(
            captureButton,
            flipCameraButton,
            null,
            Modifier.weight(1f),
        )

        Box(Modifier.weight(1f)) {
            viewfinder(Modifier)
            guide(Modifier.fillMaxSize())
            guideText(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
fun TableTopSupportedCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    rearCameraButton: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    isTabletop: Boolean = false,
) {
    if (isTabletop) {
        TableTopCameraLayout(
            viewfinder,
            captureButton,
            flipCameraButton,
            zoomButton,
            guideText,
            guide,
            rearCameraButton,
            modifier,
        )
    } else {
        TableTopReadyCameraLayout(
            viewfinder,
            captureButton,
            flipCameraButton,
            zoomButton,
            guideText,
            guide,
            rearCameraButton,
            modifier,
        )
    }
}

@Composable
private fun TableTopReadyCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    rearCameraButton: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .weight(1f)
                .safeDrawingPadding(),
        ) {
            viewfinder(Modifier.fillMaxSize())
            guide(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            )
            guideText(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 36.dp, vertical = 64.dp),
            )
        }
        HorizontalControlsLayout(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            captureButton,
            flipCameraButton,
            zoomButton,
            rearCameraButton,
        )
    }
}

@Composable
private fun TableTopCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    rearCameraButton: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(9 / 16f),
        ) {
            viewfinder(Modifier)
            guide(Modifier.fillMaxSize())
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 48.dp),
        ) {
            guideText(Modifier.align(Alignment.TopCenter))
            HorizontalControlsLayout(
                modifier = Modifier.align(Alignment.Center),
                captureButton,
                flipCameraButton,
                zoomButton,
                rearCameraButton,
            )
        }
    }
}

@Composable
private fun MediumHorizontalCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    isCameraLeft: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        if (isCameraLeft) {
            Spacer(
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
            )
        } else {
            VerticalControlsLayout(
                captureButton,
                flipCameraButton,
                zoomButton = null,
                Modifier.weight(1f),
                isCameraLeft,
            )
        }

        Box(
            Modifier
                .aspectRatio(9 / 16f)
                .navigationBarsPadding(),
        ) {
            viewfinder(Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.weight(1f)) {
                    guide(Modifier.fillMaxSize())
                    guideText(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                zoomButton(Modifier)
            }
        }
        if (isCameraLeft) {
            VerticalControlsLayout(
                captureButton,
                flipCameraButton,
                zoomButton = null,
                Modifier.weight(1f),
                isCameraLeft,
            )
        } else {
            Spacer(
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun VerticalCameraLayout(
    viewfinder: @Composable (modifier: Modifier) -> Unit,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: @Composable (modifier: Modifier) -> Unit,
    zoomButton: @Composable (modifier: Modifier) -> Unit,
    guideText: @Composable (modifier: Modifier) -> Unit,
    guide: @Composable (modifier: Modifier) -> Unit,
    rearCameraButton: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        viewfinder(Modifier)
        Column(modifier.safeContentPadding()) {
            Box(Modifier.weight(1f)) {
                guide(Modifier.fillMaxSize())
                guideText(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                )
            }
            HorizontalControlsLayout(
                modifier = Modifier,
                captureButton,
                flipCameraButton,
                zoomButton,
                rearCameraButton,
            )
        }
    }
}

@Composable
private fun HorizontalControlsLayout(
    modifier: Modifier = Modifier,
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: (@Composable (modifier: Modifier) -> Unit)?,
    zoomButton: (@Composable (modifier: Modifier) -> Unit)?,
    rearCameraButton: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (zoomButton != null) zoomButton(Modifier)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (flipCameraButton != null) flipCameraButton(Modifier)
            }
            captureButton(Modifier)
            if (rearCameraButton != null) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    rearCameraButton(Modifier)
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun VerticalControlsLayout(
    captureButton: @Composable (modifier: Modifier) -> Unit,
    flipCameraButton: (@Composable (modifier: Modifier) -> Unit)?,
    zoomButton: (@Composable (modifier: Modifier) -> Unit)?,
    modifier: Modifier = Modifier,
    isCameraLeft: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCameraLeft) Arrangement.End else Arrangement.Start,
    ) {
        if (isCameraLeft) {
            if (zoomButton != null) zoomButton(Modifier)
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (flipCameraButton != null) flipCameraButton(Modifier)
                }
                captureButton(Modifier)
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (flipCameraButton != null) flipCameraButton(Modifier)
                }
                captureButton(Modifier)
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.width(12.dp))
            if (zoomButton != null) zoomButton(Modifier)
        }
    }
}

@Preview("compact vertical", widthDp = 200, heightDp = 400)
@Preview("horizontal", widthDp = 400, heightDp = 200)
@Preview("expanded horizontal", widthDp = 841, heightDp = 480)
@Composable
private fun CameraOverlayPreview(
    @PreviewParameter(FoldablePreviewParametersProvider::class) parameters: FoldablePreviewParameters,
) {
    AndroidifyTheme {
        CameraLayout(
            viewfinder = { modifier ->
                Box(
                    modifier
                        .fillMaxSize()
                        .background(Color.Green.copy(alpha = .3f))
                        .border(2.dp, Color.Green),
                )
            },
            captureButton = { modifier ->
                Box(
                    modifier
                        .size(80.dp)
                        .background(Color.Blue.copy(alpha = .3f))
                        .border(2.dp, Color.Blue),
                )
            },
            guide = { modifier ->
                Box(
                    modifier
                        .fillMaxSize()
                        .background(Color.Cyan.copy(alpha = .3f))
                        .border(2.dp, Color.Cyan),
                )
            },
            guideText = { modifier ->
                Box(
                    modifier
                        .size(width = 180.dp, height = 80.dp)
                        .background(Color.Red.copy(alpha = .3f))
                        .border(2.dp, Color.Red),
                )
            },
            flipCameraButton = { modifier ->
                Box(
                    modifier
                        .size(48.dp)
                        .background(Color.Magenta.copy(alpha = .3f))
                        .border(2.dp, Color.Magenta),
                )
            },
            zoomButton = { modifier ->
                Box(
                    modifier
                        .size(40.dp)
                        .background(Color.Yellow.copy(alpha = .3f))
                        .border(2.dp, Color.Yellow),
                )
            },
            rearCameraButton = { modifier ->
                Box(
                    modifier
                        .size(40.dp)
                        .background(Color.LightGray.copy(alpha = .3f))
                        .border(2.dp, Color.LightGray),
                )
            },
            supportsTabletop = parameters.supportsTabletop,
            isTabletop = parameters.isTabletop,
            surfaceAspectRatio = 16f / 9f,
        )
    }
}
