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
package com.android.developers.androidify.theme.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import kotlin.enums.enumEntries

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
inline fun <reified T : Enum<T>> HorizontalToolbar(
    selectedOption: T,
    crossinline label: @Composable (T) -> String,
    crossinline onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = enumEntries<T>()
    HorizontalFloatingToolbar(
        modifier = modifier.border(
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
        options.forEachIndexed { index, item ->
            ToggleButton(
                modifier = Modifier,
                checked = selectedOption == item,
                onCheckedChange = { onOptionSelected(item) },
                shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Text(label(options[index]), maxLines = 1)
            }
            if (index != options.size - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}
