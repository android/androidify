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
package com.android.developers.androidify.results

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun BotResultCard(
    resultImageUri: Uri,
    originalImageUrl: Uri?,
    promptText: String?,
    flippableState: FlippableState,
    modifier: Modifier = Modifier,
    onFlipStateChanged: ((FlippableState) -> Unit)? = null,
) {
    FlippableCard(
        modifier = modifier
            .rotate(-5f)
            .aspectRatio(BOT_ASPECT_RATIO)
            .padding(16.dp)
            .safeContentPadding(),
        flippableState = flippableState,
        onFlipStateChanged = onFlipStateChanged,
        front = {
            ImageCard(resultImageUri, isBack = false)
        },
        back = {
            if (originalImageUrl != null) {
                ImageCard(originalImageUrl, isBack = true)
            } else {
                BackCardPrompt(promptText!!)
            }
        },
    )
}

@Composable
private fun ImageCard(originalImageUrl: Uri, isBack: Boolean) {
    AsyncImage(
        model = originalImageUrl,
        contentDescription = if (isBack)stringResource(R.string.original_image) else stringResource(R.string.resultant_android_bot),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(BOT_ASPECT_RATIO)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large),
    )
}

@Composable
private fun BackCardPrompt(promptText: String) {
    val annotatedString = buildAnnotatedString {
        pushStyle(SpanStyle(fontWeight = Bold))
        append(stringResource(R.string.my_bot_is_wearing))
        append(" ")
        pop()
        pushStyle(SpanStyle(fontWeight = FontWeight.Normal))
        append(promptText)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(BOT_ASPECT_RATIO)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            rememberVectorPainter(ImageVector.vectorResource(R.drawable.pen_spark)),
            modifier = Modifier.size(36.dp),
            contentDescription = null,
        ) // decorative element
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
        )
    }
}

const val BOT_ASPECT_RATIO = 3f / 4f
