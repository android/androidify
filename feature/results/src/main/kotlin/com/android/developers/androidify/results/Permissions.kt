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
@file:OptIn(ExperimentalPermissionsApi::class)

package com.android.developers.androidify.results

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@Composable
fun PermissionRationaleDialog(
    showRationaleDialog: Boolean,
    onDismiss: () -> Unit,
    externalStoragePermission: PermissionState,
) {
    if (showRationaleDialog) {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.grant_storage_permission))
            },
            text = {
                Text(text = stringResource(R.string.to_save_your_bot_to_the_device_you_need_to_grant_storage_permission))
            },
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        externalStoragePermission.launchPermissionRequest()
                    },
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.dismiss))
                }
            },
        )
    }
}
