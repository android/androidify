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
package com.android.developers.androidify.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

/**
 * If the user has already denied the permission to set the watch face, then this screen is shown.
 */
@Composable
fun OpenSettingsScreen(modifier: Modifier = Modifier) {
    val activity = LocalActivity.current
    CallToActionScreen(
        callToActionText = stringResource(R.string.open_settings_prompt),
        buttonText = stringResource(R.string.open_settings_button_text),
        onCallToActionClick = {
            val intent =
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
            activity?.startActivity(intent)
        },
    )
}

@WearPreviewDevices
@Composable
fun OpenSettingsPreview() {
    AndroidifyWearTheme {
        OpenSettingsScreen()
    }
}
