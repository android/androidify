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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.results

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme

@Preview
@Composable
private fun ResultToolBarOptionPreview() {
    AndroidifyTheme {
        Column {
            ResultToolbarOption {
            }
        }
    }
}

@Composable
fun ResultToolbarOption(
    modifier: Modifier = Modifier,
    selectedOption: ResultOption = ResultOption.ResultImage,
    wasPromptUsed: Boolean = false,
    onResultOptionSelected: (ResultOption) -> Unit,
) {
    val options = ResultOption.entries
    HorizontalFloatingToolbar(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .border(
                2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large,
            ),
        colors = FloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surface,
            toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fabContainerColor = MaterialTheme.colorScheme.tertiary,
            fabContentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        expanded = true,
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                modifier = Modifier,
                checked = selectedOption == label,
                onCheckedChange = { onResultOptionSelected(label) },
                shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Text(stringResource(label.displayText(wasPromptUsed)), maxLines = 1)
            }
            if (index != options.size - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

enum class ResultOption(val displayName: Int) {
    OriginalInput(R.string.photo),
    ResultImage(R.string.bot),
    ;

    fun toFlippableState(): FlippableState {
        return when (this) {
            ResultImage -> FlippableState.Front
            OriginalInput -> FlippableState.Back
        }
    }

    fun displayText(wasPromptUsed: Boolean): Int {
        return if (this == OriginalInput) {
            if (wasPromptUsed) return R.string.prompt else R.string.photo
        } else {
            this.displayName
        }
    }
}
