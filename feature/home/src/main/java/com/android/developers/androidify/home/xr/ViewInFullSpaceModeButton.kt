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
package com.android.developers.androidify.home.xr

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSession
import androidx.xr.scenecore.scene
import com.android.developers.androidify.home.R
import com.android.developers.androidify.theme.Blue
import com.android.developers.androidify.xr.FullSpaceIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ViewInFullSpaceModeButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors().copy(containerColor = Blue),
) {
    val style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight(700),
        letterSpacing = .15f.sp,
    )
    val session = LocalSession.current

    Button(
        onClick = {
            session?.scene?.requestFullSpaceMode()
        },
        modifier = modifier,
        colors = colors,
    ) {
        FullSpaceIcon(Modifier.size(ButtonDefaults.LargeIconSize))
        Spacer(Modifier.width(ButtonDefaults.LargeIconSpacing))
        Text(
            stringResource(R.string.xr_full_space_button_label),
            style = style,
        )
    }
}
