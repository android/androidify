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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.developers.androidify.results.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WatchFacePanelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    buttonText: String,
    isInProgress: Boolean = false,
    iconResId: Int? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        contentColor = MaterialTheme.colorScheme.surface,
        containerColor = MaterialTheme.colorScheme.onSurface,
    ),
) {
    Button(
        colors = colors,
        modifier = modifier
            .heightIn(min = 64.dp)
            .fillMaxWidth()
            .animateContentSize(),
        border = BorderStroke(2.dp, color = colors.contentColor),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isInProgress) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(24.dp),
                    containerColor = colors.containerColor,
                    indicatorColor = colors.contentColor,
                )
            } else if (iconResId != null) {
                Icon(
                    ImageVector.vectorResource(iconResId),
                    contentDescription = null,
                )
            }
            Spacer(modifier.width(8.dp))
            Text(buttonText, fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchFaceInstallButtonPreview() {
    MaterialTheme {
        WatchFacePanelButton(
            onClick = { },
            buttonText = stringResource(R.string.send_to_watch),
            isInProgress = false,
            iconResId = R.drawable.watch_arrow_24,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchFaceInstalledButtonPreview() {
    MaterialTheme {
        WatchFacePanelButton(
            onClick = { },
            buttonText = stringResource(R.string.watch_face_sent),
            isInProgress = false,
            iconResId = R.drawable.check_24,
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WatchFaceInstallingButtonPreview() {
    MaterialTheme {
        WatchFacePanelButton(
            onClick = { },
            buttonText = stringResource(R.string.sending_to_watch),
            isInProgress = true,
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }
}
