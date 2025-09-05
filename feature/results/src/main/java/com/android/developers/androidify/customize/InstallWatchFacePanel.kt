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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.watchface.WatchFaceAsset

@Composable
fun InstallWatchFacePanel(
    modifier: Modifier = Modifier,
    watchFaceSelectionState: WatchFaceSelectionState,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit,
    onInstallClick: () -> Unit = { },
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val noAvailableWatchFaces = watchFaceSelectionState.watchFaces.isEmpty() &&
            !watchFaceSelectionState.isLoadingWatchFaces
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (noAvailableWatchFaces) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.no_watch_faces),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                WatchFacesRow(
                    watchFaces = watchFaceSelectionState.watchFaces,
                    selectedWatchFace = watchFaceSelectionState.selectedWatchFace,
                    onWatchFaceSelect = onWatchFaceSelect,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        WatchFacePanelButton(
            modifier = modifier.padding(horizontal = 16.dp),
            buttonText = stringResource(R.string.send_to_watch),
            iconResId = R.drawable.watch_arrow_24,
            onClick = onInstallClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallWatchFacePanelPreview() {
    val watchFace1 = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = R.drawable.watch_face_preview,
    )
    val watchFace2 = WatchFaceAsset(
        id = "watch_face_2",
        previewPath = R.drawable.watch_face_preview,
    )
    val watchFaceSelectionState = WatchFaceSelectionState(
        watchFaces = listOf(watchFace1, watchFace2),
        selectedWatchFace = watchFace1,
        isLoadingWatchFaces = false,
    )
    AndroidifyTheme {
        InstallWatchFacePanel(
            watchFaceSelectionState = watchFaceSelectionState,
            onWatchFaceSelect = {},
            onInstallClick = {},
        )
    }
}

@Composable
fun WatchFacesRow(
    watchFaces: List<WatchFaceAsset>,
    selectedWatchFace: WatchFaceAsset? = null,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(watchFaces, key = { it.id }) { watchFace ->
            WatchFacePreviewItem(
                watchFace = watchFace,
                isSelected = watchFace.id == selectedWatchFace?.id,
                onClick = {
                    onWatchFaceSelect(watchFace)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallWatchFacePanelNoWatchFacesPreview() {
    val watchFaceSelectionState = WatchFaceSelectionState(
        isLoadingWatchFaces = false,
        watchFaces = listOf(),
        selectedWatchFace = null,
    )
    AndroidifyTheme {
        InstallWatchFacePanel(
            watchFaceSelectionState = watchFaceSelectionState,
            onWatchFaceSelect = {},
            onInstallClick = {},
        )
    }
}
