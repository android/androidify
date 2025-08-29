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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.watchface.WatchFaceAsset

@Composable
fun SendingWatchFacePanel(
    modifier: Modifier = Modifier,
    selectedWatchFace: WatchFaceAsset?,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WatchFacesRow(
                watchFaces = listOfNotNull(selectedWatchFace),
                selectedWatchFace = selectedWatchFace,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        WatchFacePanelButton(
            buttonText = stringResource(R.string.sending_to_watch),
            isSending = true,
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SendingWatchFacePanelPreview() {
    val watchFace1 = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = R.drawable.watch_face_preview,
    )
    AndroidifyTheme {
        SendingWatchFacePanel(
            selectedWatchFace = watchFace1,
        )
    }
}
