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
package com.android.developers.androidify.creation.xr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxHeight
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.offset
import com.android.developers.androidify.creation.LoadingScreenContents
import com.android.developers.androidify.creation.LoadingScreenScaffold
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.util.TabletPreview
import com.android.developers.androidify.xr.BackgroundSubspace
import com.android.developers.androidify.xr.FillBackground
import com.android.developers.androidify.xr.MainPanelWorkaround
import com.android.developers.androidify.xr.RequestHomeSpaceIconButton
import com.android.developers.androidify.creation.R as CreationR

private const val squiggleSafeContentWidth = 0.4f
private const val squiggleSafeContentHeight = 0.6f

@Composable
fun LoadingScreenSpatial(
    onCancelPress: () -> Unit,
) {
    MainPanelWorkaround()
    BackgroundSubspace(
        aspectRatio = 1.4f,
        minimumHeight = 500.dp,
        drawable = CreationR.drawable.squiggle_light,
    ) {
        Orbiter(
            position = ContentEdge.Top,
            offsetType = OrbiterOffsetType.OuterEdge,
            offset = 32.dp,
            alignment = Alignment.End,
        ) {
            RequestHomeSpaceIconButton(
                modifier = Modifier
                    .size(64.dp, 64.dp)
                    .padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            )
        }
        SpatialPanel(
            SubspaceModifier
                .fillMaxWidth(squiggleSafeContentWidth)
                .fillMaxHeight(squiggleSafeContentHeight)
                .offset(z = 10.dp),
        ) {
            LoadingScreenScaffold(
                topBar = {},
                onCancelPress = onCancelPress,
                containerColor = Color.Transparent,
            ) { contentPadding ->
                LoadingScreenContents(contentPadding)
            }
        }
    }
}

@TabletPreview
@Composable
private fun LoadingScreenSpatialPreview() {
    AndroidifyTheme {
        Box {
            FillBackground(CreationR.drawable.squiggle_light)
            Box(
                Modifier
                    .fillMaxHeight(squiggleSafeContentHeight)
                    .fillMaxWidth(squiggleSafeContentWidth)
                    .align(Alignment.Center),
            ) {
                LoadingScreenScaffold(
                    topBar = {},
                    onCancelPress = { },
                    containerColor = Color.Transparent,
                ) { contentPadding ->
                    LoadingScreenContents(contentPadding)
                }
            }
        }
    }
}
