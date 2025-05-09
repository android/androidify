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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.android.developers.androidify.theme.AndroidifyTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraDirectionButton(
    flipCameraDirection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionLabel = stringResource(R.string.flip_camera_direction)
    FilledTonalIconButton(
        onClick = flipCameraDirection,
        modifier = modifier
            .semantics {
                onClick(
                    label = actionLabel,
                    action = {
                        flipCameraDirection()
                        true
                    },
                )
            }
            .size(
                IconButtonDefaults.mediumContainerSize(
                    IconButtonDefaults.IconButtonWidthOption.Narrow,
                ),
            ),
        shape = IconButtonDefaults.filledShape,

    ) {
        Icon(painterResource(R.drawable.outline_cameraswitch_24), contentDescription = null)
    }
}

@Preview
@Composable
fun CameraDirectionButtonPreview() {
    AndroidifyTheme {
        CameraDirectionButton(
            flipCameraDirection = {},
        )
    }
}
