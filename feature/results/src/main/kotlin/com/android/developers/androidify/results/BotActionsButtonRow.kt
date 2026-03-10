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
package com.android.developers.androidify.results

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.components.PrimaryButton

@Composable
fun BotActionsButtonRow(
    onCustomizeShareClicked: () -> Unit,
    layoutType: ResultsLayoutType,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        PrimaryButton(
            onClick = {
                onCustomizeShareClicked()
            },
            trailingIcon = {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        ImageVector
                            .vectorResource(com.android.developers.androidify.theme.R.drawable.rounded_arrow_forward_24),
                        contentDescription = null, // decorative element
                    )
                }
            },
            buttonText = when (layoutType) {
                ResultsLayoutType.Spatial, ResultsLayoutType.Verbose -> stringResource(R.string.customize_and_share)
                ResultsLayoutType.Constrained -> null
            },
        )
    }
}
