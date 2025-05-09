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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraGuideText(
    modifier: Modifier = Modifier,
) {
    BasicText(
        stringResource(R.string.camera_guide_text_label),
        style = MaterialTheme.typography.bodyMediumEmphasized,
        autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.bodyMediumEmphasized.fontSize),
        maxLines = 1,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(24.dp),
            )
            .padding(10.dp),
    )
}
