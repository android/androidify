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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.theme.AndroidifyTheme

@Composable
fun AspectRatioTool(
    sizeOptions: List<SizeOption>,
    selectedOption: SizeOption,
    onSizeOptionSelected: (SizeOption) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    GenericTool(
        modifier = modifier.wrapContentSize(),
        tools = sizeOptions,
        singleLine = singleLine,
        selectedOption = selectedOption,
        onToolSelected = {
            onSizeOptionSelected(it)
        },
        individualToolContent = { tool ->
            Box(
                modifier = Modifier
                    .size(70.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(tool.aspectRatio)
                        .border(
                            2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(6.dp)
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceBright),
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AspectRatioToolPreview() {
    AndroidifyTheme {
        AspectRatioTool(
            sizeOptions = listOf(
                SizeOption.Square,
                SizeOption.Banner,
            ),
            selectedOption = SizeOption.Square,
            onSizeOptionSelected = {},
        )
    }
}
