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
package com.android.developers.androidify.xr

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialBoxScope
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SubspaceComposable
import androidx.xr.compose.subspace.layout.SpatialMoveEvent
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.aspectRatio
import androidx.xr.compose.subspace.layout.fillMaxSize
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.unit.DpVolumeSize
import com.android.developers.androidify.theme.AndroidifyTheme

/**
 * A composable for a Subspace with a Squiggle background.
 * This Subspace is generally the top-level Subspace. It contains a full-sized squiggle background
 * that is grabbable and movable, allowing all child components to move with the background.
 */
@Composable
fun SquiggleBackgroundSubspace(
    minimumHeight: Dp,
    onMove: ((SpatialMoveEvent) -> Boolean)? = null,
    content:
    @SubspaceComposable @Composable
    SpatialBoxScope.() -> Unit,
) {
    BackgroundSubspace(
        aspectRatio = 1.7f,
        drawable = R.drawable.squiggle_full,
        onMove = onMove,
        minimumHeight = minimumHeight,
        content = content,
    )
}

@Composable
fun BackgroundSubspace(
    aspectRatio: Float,
    @DrawableRes drawable: Int,
    minimumHeight: Dp,
    onMove: ((SpatialMoveEvent) -> Boolean)? = null,
    content:
    @SubspaceComposable @Composable
    SpatialBoxScope.() -> Unit,
) {
    Subspace {
        SpatialPanel(
            SubspaceModifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            dragPolicy = MovePolicy(onMove = onMove),
            resizePolicy = ResizePolicy(
                minimumSize = DpVolumeSize(0.dp, minimumHeight, 0.dp),
                shouldMaintainAspectRatio = true,
            ),
        ) {
            FillBackground(drawable)
            Subspace {
                SpatialBox(SubspaceModifier.offset(z = 10.dp).fillMaxSize(), content = content)
            }
        }
    }
}

/**
 * Background squiggle that tries to fit in its parent.
 */
@Composable
fun FillBackground(@DrawableRes drawable: Int) {
    val vectorBackground =
        rememberVectorPainter(ImageVector.vectorResource(drawable))
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = vectorBackground,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(device = PIXEL_TABLET)
@Composable
fun SquiggleFullImagePreview() {
    AndroidifyTheme {
        FillBackground(R.drawable.squiggle_full)
    }
}
