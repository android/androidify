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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.android.developers.androidify.theme.AndroidifyTheme

@Composable
fun <T : ToolOption> GenericTool(
    tools: List<T>,
    selectedOption: T,
    onToolSelected: (T) -> Unit,
    individualToolContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val scrollModifier = if (singleLine) Modifier.horizontalScroll(rememberScrollState()) else Modifier
    FlowRow(
        modifier = modifier.then(scrollModifier),
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
    ) {
        tools.forEach { tool ->
            GenericToolButton(
                isSelected = tool == selectedOption,
                toolContent = {
                    individualToolContent(tool)
                },
                onToolSelected = onToolSelected,
                tool = tool,
            )
        }
    }
}

@Composable
fun <T : ToolOption> GenericToolButton(
    tool: T,
    toolContent: @Composable () -> Unit,
    isSelected: Boolean,
    onToolSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.height(128.dp)
            .padding(8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                onToolSelected(tool)
            },
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        val backgroundModifier = if (isSelected) {
            Modifier.background(
                MaterialTheme.colorScheme.surfaceBright,
                MaterialTheme.shapes.medium,
            )
        } else {
            Modifier
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = backgroundModifier.padding(8.dp)
                .width(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
            ) {
                toolContent()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                tool.displayName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    repeatDelayMillis = 0,
                    iterations = 300,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GenericToolPreview() {
    AndroidifyTheme {
        GenericTool(
            tools = listOf(
                BackgroundOption.None,
                BackgroundOption.Lightspeed,
                BackgroundOption.IO,
                BackgroundOption.None,
                BackgroundOption.Plain,
                BackgroundOption.Lightspeed,
                BackgroundOption.IO,
                BackgroundOption.MusicLover,
                BackgroundOption.PoolMaven,
                BackgroundOption.SoccerFanatic,
                BackgroundOption.StarGazer,
                BackgroundOption.FitnessBuff,
                BackgroundOption.Fandroid,
                BackgroundOption.GreenThumb,
                BackgroundOption.Gamer,
                BackgroundOption.Jetsetter,
                BackgroundOption.Chef,
            ),
            singleLine = false,
            selectedOption = BackgroundOption.Lightspeed,
            onToolSelected = {
            },
            individualToolContent = { tool ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(6.dp),
                ) {
                    Image(
                        rememberAsyncImagePainter(tool.previewDrawableInt),
                        contentDescription = null, // described below
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GenericToolPreviewSingleLine() {
    AndroidifyTheme {
        GenericTool(
            tools = listOf(
                BackgroundOption.None,
                BackgroundOption.Lightspeed,
                BackgroundOption.IO,
            ),
            selectedOption = BackgroundOption.Lightspeed,
            singleLine = true,
            onToolSelected = {
            },
            individualToolContent = { tool ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(6.dp),
                ) {
                    Image(
                        rememberAsyncImagePainter(tool.previewDrawableInt),
                        contentDescription = null, // described below
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
            },
        )
    }
}
