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
package com.android.developers.androidify.customize.watchface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.watchface.WatchFaceAsset
import kotlinx.coroutines.launch

@Composable
fun InstallAndroidifyPanel(
    onInstallClick: suspend () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isPlayLaunched by remember { mutableStateOf(false) }
    val placeholderWatchFace = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = R.drawable.watch_app_placeholder,
    )
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WatchFacePreviewItem(
                watchFace = placeholderWatchFace,
                isSelected = true,
                onClick = { },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val buttonText = if (isPlayLaunched) {
            stringResource(R.string.continue_on_watch)
        } else {
            stringResource(R.string.install_androidify)
        }
        val launchedColors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        val installColors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.surface,
            containerColor = MaterialTheme.colorScheme.onSurface,
        )
        val scope = rememberCoroutineScope()
        WatchFacePanelButton(
            modifier = modifier.padding(horizontal = 16.dp),
            buttonText = buttonText,
            iconResId = R.drawable.watch_arrow_24,
            onClick = {
                if (!isPlayLaunched) {
                    scope.launch {
                        val launchResult = onInstallClick()
                        if (launchResult) {
                            isPlayLaunched = true
                        }
                    }
                }
            },
            colors = if (isPlayLaunched) {
                launchedColors
            } else {
                installColors
            },
            isInProgress = isPlayLaunched,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallAndroidifyPanelPreview() {
    AndroidifyTheme {
        InstallAndroidifyPanel({ true })
    }
}
