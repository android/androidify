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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.Primary80

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RearCameraButton(
    isRearCameraEnabled: Boolean = false,
    toggleRearCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionLabel = stringResource(R.string.rear_camera_description)
    val colors = if (isRearCameraEnabled) {
        IconButtonDefaults.filledTonalIconButtonColors().copy(
            containerColor = Primary80,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        IconButtonDefaults.filledTonalIconButtonColors()
    }
    FilledTonalIconButton(
        onClick = toggleRearCamera,
        modifier = modifier
            .semantics {
                onClick(
                    label = actionLabel,
                    action = {
                        toggleRearCamera()
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
        colors = colors,
    ) {
        Icon(
            painterResource(R.drawable.outline_rear_camera),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}
