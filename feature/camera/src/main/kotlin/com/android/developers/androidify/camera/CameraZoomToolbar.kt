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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.developers.androidify.theme.AndroidifyTheme
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun ZoomToolbar(
    defaultZoomOptions: List<Float>,
    zoomLevel: () -> Float,
    onZoomLevelSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Only render the zoom toolbar when there's exactly two options
    if (defaultZoomOptions.size != 2) return

    val selectedOptionIndex by remember(defaultZoomOptions) {
        derivedStateOf {
            if ((zoomLevel() * 10).roundToInt() < defaultZoomOptions[1] * 10) 0 else 1
        }
    }
    val options by remember(defaultZoomOptions) {
        derivedStateOf {
            val zoom = (zoomLevel() * 10).roundToInt() / 10f
            fun Float.formattedZoom() = "%.1f".format(this)
                .trimStart('0')
                .removeSuffix(".0") + "X"
            val formattedCurrentZoom = zoom.formattedZoom()

            if (zoom < defaultZoomOptions[1]) {
                listOf(
                    formattedCurrentZoom,
                    defaultZoomOptions[1].formattedZoom(),
                )
            } else {
                listOf(
                    defaultZoomOptions[0].formattedZoom(),
                    formattedCurrentZoom,
                )
            }
        }
    }
    val textMeasurer = rememberTextMeasurer()

    ButtonGroup(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        expandedRatio = 0f,
    ) {
        ToggleButton(
            checked = selectedOptionIndex == 0,
            onCheckedChange = { onZoomLevelSelected(defaultZoomOptions[0]) },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(),
            modifier = Modifier,
        ) {
            val textLayoutResult = textMeasurer.measure("M.MX", LocalTextStyle.current)
            val width = with(LocalDensity.current) { textLayoutResult.size.width.toDp() }
            Text(options[0], Modifier.widthIn(min = width), textAlign = TextAlign.Center)
        }
        ToggleButton(
            checked = selectedOptionIndex == 1,
            onCheckedChange = { onZoomLevelSelected(defaultZoomOptions[1]) },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(),
            modifier = Modifier,
        ) {
            val textLayoutResult = textMeasurer.measure("M.MX", LocalTextStyle.current)
            val width = with(LocalDensity.current) { textLayoutResult.size.width.toDp() }
            Text(options[1], Modifier.widthIn(min = width), textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
private fun ZoomToolbarPreview() {
    var zoomLevel by remember { mutableFloatStateOf(0.4343f) }

    AndroidifyTheme {
        Column {
            ZoomToolbar(
                defaultZoomOptions = listOf(0.6f, 1f),
                zoomLevel = { zoomLevel },
                onZoomLevelSelected = { zoomLevel = it },
            )
            ZoomToolbar(
                defaultZoomOptions = listOf(1f, 2f),
                zoomLevel = { zoomLevel },
                onZoomLevelSelected = { zoomLevel = it },
            )
            // Doesn't render
            ZoomToolbar(
                defaultZoomOptions = listOf(1f),
                zoomLevel = { zoomLevel },
                onZoomLevelSelected = { zoomLevel = it },
            )
            Row {
                Button(onClick = { zoomLevel -= 0.1f }) {
                    Text("-")
                }
                Button(onClick = { zoomLevel += 0.1f }) {
                    Text("+")
                }
            }
        }
    }
}
