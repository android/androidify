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

package com.android.developers.androidify.customize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import com.android.developers.androidify.theme.Primary

@Composable
fun ToolSelector(
    tools: List<CustomizeTool>,
    selectedOption: CustomizeTool,
    onToolSelected: (CustomizeTool) -> Unit,
    horizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    val buttons = @Composable {
        tools.forEachIndexed { index, tool ->
            ToolSelectorToggleButton(
                modifier = Modifier,
                tool = tool,
                checked = selectedOption == tool,
                onCheckedChange = { onToolSelected(tool) },
            )
            if (index != tools.size - 1) {
                Spacer(Modifier.size(8.dp))
            }
        }
    }
    val toolbarColors = FloatingToolbarColors(
        toolbarContainerColor = MaterialTheme.colorScheme.surface,
        toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        fabContainerColor = MaterialTheme.colorScheme.tertiary,
        fabContentColor = MaterialTheme.colorScheme.onTertiary,
    )

    if (horizontal) {
        HorizontalFloatingToolbar(
            modifier = modifier.toolbarBorder(),
            shape = MaterialTheme.shapes.large,
            colors = toolbarColors,
            expanded = true,
        ) {
            buttons()
        }
    } else {
        VerticalFloatingToolbar(
            modifier = modifier.toolbarBorder(),
            shape = MaterialTheme.shapes.large,
            colors = toolbarColors,
            expanded = true,
        ) {
            buttons()
        }
    }
}

@Composable
private fun ToolSelectorToggleButton(
    tool: CustomizeTool,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = ToggleButtonDefaults.shapes(
            checkedShape = MaterialTheme.shapes.large,
        ),
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
}

@Composable
private fun Modifier.toolbarBorder() = this.border(
    2.dp,
    color = MaterialTheme.colorScheme.outline,
    shape = MaterialTheme.shapes.large,
)

@Preview
@Composable
private fun ToolsPreviewHorizontal() {
    AndroidifyTheme {
        Box(Modifier.background(Primary)) {
            ToolSelector(
                tools = listOf(CustomizeTool.Size, CustomizeTool.Background),
                selectedOption = CustomizeTool.Size,
                horizontal = true,
                onToolSelected = {},
            )
        }
    }
}

@Preview
@Composable
private fun ToolsPreviewVertical() {
    AndroidifyTheme {
        Box(Modifier.background(Primary)) {
            ToolSelector(
                tools = listOf(CustomizeTool.Size, CustomizeTool.Background),
                selectedOption = CustomizeTool.Size,
                horizontal = false,
                onToolSelected = {},
            )
        }
    }
}
