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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolSelector(
    tools: List<CustomizeTool>,
    selectedOption: CustomizeTool,
    onToolSelected: (CustomizeTool) -> Unit,
    horizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    if (horizontal) {
        HorizontalFloatingToolbar(
            modifier = modifier.border(
                2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large,
            ).padding(2.dp),
            colors = FloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surface,
                toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                fabContainerColor = MaterialTheme.colorScheme.tertiary,
                fabContentColor = MaterialTheme.colorScheme.onTertiary,
            ),
            expanded = true,
        ) {
            tools.forEachIndexed { index, tool ->
                ToggleButton(
                    modifier = Modifier,
                    checked = selectedOption == tool,
                    onCheckedChange = { onToolSelected(tool) },
                    shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        painterResource(tool.icon),
                        contentDescription = tool.displayName,
                    )
                }
                if (index != tools.size - 1) {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    } else {
        VerticalFloatingToolbar(
            modifier = modifier.border(
                2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large,
            ).padding(2.dp),
            colors = FloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surface,
                toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                fabContainerColor = MaterialTheme.colorScheme.tertiary,
                fabContentColor = MaterialTheme.colorScheme.onTertiary,
            ),
            expanded = true,
        ) {
            tools.forEachIndexed { index, tool ->
                ToggleButton(
                    modifier = Modifier,
                    checked = selectedOption == tool,
                    onCheckedChange = { onToolSelected(tool) },
                    shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        painterResource(tool.icon),
                        contentDescription = tool.displayName,
                    )
                }
                if (index != tools.size - 1) {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun ToolsPreviewHorizontal() {
    AndroidifyTheme {
        ToolSelector(
            tools = listOf(CustomizeTool.Size, CustomizeTool.Background),
            selectedOption = CustomizeTool.Size,
            horizontal = true,
            onToolSelected = {},
        )
    }
}

@Preview
@Composable
private fun ToolsPreviewVertical() {
    AndroidifyTheme {
        ToolSelector(
            tools = listOf(CustomizeTool.Size, CustomizeTool.Background),
            selectedOption = CustomizeTool.Size,
            horizontal = false,
            onToolSelected = {},
        )
    }
}
