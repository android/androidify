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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme

@Composable
fun BackgroundTool(
    backgroundOptions: List<BackgroundOption>,
    selectedOption: BackgroundOption,
    onBackgroundOptionSelected: (BackgroundOption) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
) {
    GenericTool(
        modifier = modifier.wrapContentSize(),
        tools = backgroundOptions,
        singleLine = singleLine,
        selectedOption = selectedOption,
        onToolSelected = {
            onBackgroundOptionSelected(it)
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
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(6.dp),
            ) {
                if (tool.previewDrawableInt != null) {
                    Image(
                        rememberAsyncImagePainter(tool.previewDrawableInt),
                        contentDescription = null, // described below
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small),
                    )
                }
                if (tool.aiBackground) {
                    Box(
                        Modifier
                            .padding(2.dp)
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(size = 24.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.spark),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun BackgroundToolPreview() {
    AndroidifyTheme {
        BackgroundTool(
            backgroundOptions = listOf(
                BackgroundOption.None,
                BackgroundOption.Plain,
                BackgroundOption.Lightspeed,
                BackgroundOption.IO,
                BackgroundOption.Intergalactic,
                BackgroundOption.Yeehaw,
                BackgroundOption.Island,
            ),
            selectedOption = BackgroundOption.Lightspeed,
            onBackgroundOptionSelected = {},
        )
    }
}
