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

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialBoxScope
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SubspaceComposable
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.aspectRatio
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.resizable
import com.android.developers.androidify.theme.components.SquiggleBackgroundFull

/**
 * A composable for a Subspace with a Squiggle background.
 * This Subspace is generally the top-level Subspace. It contains a full-sized squiggle background
 * that is grabbable and movable, allowing all child components to move with the background.
 */
@Composable
fun SquiggleBackgroundSubspace(
    content:
    @SubspaceComposable @Composable
    SpatialBoxScope.() -> Unit,
) {
    Subspace {
        SpatialPanel(
            SubspaceModifier
                .movable()
                .resizable()
                .fillMaxWidth(1f)
                .aspectRatio(1.7f),
        ) {
            SquiggleBackgroundFull()
            Subspace {
                SpatialBox(SubspaceModifier.offset(z = 10.dp), content = content)
            }
        }
    }
}
