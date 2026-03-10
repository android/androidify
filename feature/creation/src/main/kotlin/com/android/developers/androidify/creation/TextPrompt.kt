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
@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
)

package com.android.developers.androidify.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LimeGreen
import com.android.developers.androidify.theme.Primary90
import com.android.developers.androidify.theme.Secondary
import com.android.developers.androidify.theme.components.GradientAssistElevatedChip
import com.android.developers.androidify.theme.components.gradientChipColorDefaults
import com.android.developers.androidify.theme.components.infinitelyAnimatingLinearGradient
import com.android.developers.androidify.util.AnimatedTextField
import com.android.developers.androidify.util.dashedRoundedRectBorder
import com.android.developers.androidify.creation.R as CreationR

@Preview(showBackground = true)
@Composable
private fun TextPromptGenerationPreview() {
    AndroidifyTheme {
        TextPrompt(
            TextFieldState(),
            false,
            generatedPrompt = "wearing a red sweater",
            onPromptGenerationPressed = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextPromptGenerationInProgressPreview() {
    AndroidifyTheme {
        TextPrompt(
            TextFieldState(),
            true,
            generatedPrompt = "wearing a red sweater",
            onPromptGenerationPressed = {},
        )
    }
}

@Composable
fun TextPrompt(
    textFieldState: TextFieldState,
    promptGenerationInProgress: Boolean,
    modifier: Modifier = Modifier,
    generatedPrompt: String? = null,
    onPromptGenerationPressed: () -> Unit,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                rememberVectorPainter(ImageVector.vectorResource(CreationR.drawable.rounded_draw_24)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                stringResource(CreationR.string.headline_my_bot_is),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 24.sp,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .dashedRoundedRectBorder(
                    2.dp,
                    MaterialTheme.colorScheme.outline,
                    cornerRadius = 28.dp,
                )
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxSize(),
        ) {
            AnimatedTextField(
                textFieldState,
                targetEndState = generatedPrompt,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                textStyle = TextStyle(fontSize = 24.sp),
                decorator = { innerTextField ->
                    if (textFieldState.text.isEmpty()) {
                        Text(
                            stringResource(CreationR.string.prompt_text_hint).trimIndent(),
                            color = Color.Gray,
                            fontSize = 24.sp,
                        )
                    }
                    innerTextField()
                },
            )
            AnimatedVisibility(
                !WindowInsets.isImeVisible,
                enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec()),
            ) {
                HelpMeWriteButton(promptGenerationInProgress, onPromptGenerationPressed)
            }
        }
    }
}

@Composable
private fun HelpMeWriteButton(
    promptGenerationInProgress: Boolean,
    onPromptGenerationPressed: () -> Unit,
) {
    val color = if (promptGenerationInProgress) {
        Brush.infinitelyAnimatingLinearGradient(
            listOf(
                LimeGreen,
                Primary90,
                Secondary,
            ),
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.surfaceContainerLow)
    }
    GradientAssistElevatedChip(
        onClick = {
            onPromptGenerationPressed()
        },
        label = {
            if (promptGenerationInProgress) {
                Text(stringResource(CreationR.string.writing))
            } else {
                Text(stringResource(CreationR.string.write_me_a_prompt))
            }
        },
        leadingIcon = {
            Icon(
                rememberVectorPainter(ImageVector.vectorResource(CreationR.drawable.pen_spark_24)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        colors = gradientChipColorDefaults().copy(
            containerColor = color,
            disabledContainerColor = color,
        ),
        enabled = !promptGenerationInProgress,
    )
}
