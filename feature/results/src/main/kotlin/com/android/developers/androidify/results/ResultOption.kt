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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.HorizontalToolbar

@Composable
fun ResultToolbarOption(
    modifier: Modifier = Modifier,
    selectedOption: ResultOption = ResultOption.ResultImage,
    wasPromptUsed: Boolean = false,
    onResultOptionSelected: (ResultOption) -> Unit,
) {
    HorizontalToolbar(
        selectedOption = selectedOption,
        modifier = modifier,
        label = { stringResource(it.displayText(wasPromptUsed)) },
        onOptionSelected = onResultOptionSelected,
    )
}

enum class ResultOption {
    OriginalInput,
    ResultImage,
    ;

    fun toFlippableState(): FlippableState {
        return when (this) {
            ResultImage -> FlippableState.Front
            OriginalInput -> FlippableState.Back
        }
    }

    fun displayText(wasPromptUsed: Boolean): Int {
        return when (this) {
            OriginalInput -> if (wasPromptUsed) R.string.prompt else R.string.photo
            ResultImage -> R.string.bot
        }
    }
}

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
