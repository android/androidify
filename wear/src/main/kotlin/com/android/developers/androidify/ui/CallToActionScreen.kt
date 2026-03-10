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
package com.android.developers.androidify.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.ResponsiveTransformingLazyColumn
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

@Composable
fun CallToActionScreen(
    callToActionText: String,
    buttonText: String,
    onCallToActionClick: () -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(
        scrollState = listState,
    ) { contentPadding ->
        ResponsiveTransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
        ) {
            item {
                Image(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.logo_description),
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = callToActionText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            item {
                CallToActionButton(
                    buttonText = buttonText,
                    onClick = onCallToActionClick,
                )
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun CallToActionScreenPreview() {
    AndroidifyWearTheme {
        CallToActionScreen(
            callToActionText = "Call to action text",
            buttonText = "Button text",
            onCallToActionClick = {},
        )
    }
}
