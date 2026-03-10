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

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.util.LargeScreensPreview

@Composable
fun BackgroundQuotes(
    quote1: String,
    quote2: String?,
    verboseLayout: Boolean = true,
    enableAnimations: Boolean = !LocalInspectionMode.current,
) {
    // Disable animation in tests
    val iterations = if (enableAnimations) 100 else 0

    Box(modifier = Modifier.fillMaxSize()) {
        AlwaysMarquee(
            size = 1.2f,
            modifier = Modifier.align(if (verboseLayout) Alignment.TopCenter else Alignment.Center),
            marqueeModifier = Modifier.basicMarquee(
                iterations = iterations,
                repeatDelayMillis = 0,
                velocity = 80.dp,
                initialDelayMillis = 500,
            ),
        ) {
            Text(
                quote1,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                fontSize = 120.sp,
            )
        }
        if (quote2 != null) {
            AlwaysMarquee(
                size = 1.2f,
                modifier = Modifier.align(Alignment.BottomCenter),
                marqueeModifier = Modifier.basicMarquee(
                    iterations = iterations,
                    repeatDelayMillis = 0,
                    velocity = 60.dp,
                    initialDelayMillis = 500,
                ),
            ) {
                Text(
                    quote2,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                    fontSize = 110.sp,
                )
            }
        }
    }
}

@Composable
fun BackgroundRandomQuotes(verboseLayout: Boolean = true) {
    val localInspectionMode = LocalInspectionMode.current
    val listResultCompliments = stringArrayResource(R.array.list_compliments)

    val quote1 = remember {
        if (localInspectionMode) {
            listResultCompliments.first()
        } else {
            listResultCompliments.random()
        }
    }
    val quote2 = remember {
        if (verboseLayout) {
            val listMinusOther = listResultCompliments.asList().minus(quote1)
            if (localInspectionMode) {
                listMinusOther.first()
            } else {
                listMinusOther.random()
            }
        } else {
            null
        }
    }
    BackgroundQuotes(quote1, quote2, verboseLayout)
}

/**
 * A composable that will always scroll its contents. [Modifier.basicMarquee] does nothing when the
 * contents fit in the max constraints. This composable creates a box that is always larger than the
 * max constraints and applies the marquee to that box.
 */
@Composable
private fun AlwaysMarquee(
    size: Float,
    marqueeModifier: Modifier,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Box(marqueeModifier) {
            Box(Modifier.requiredWidthIn(min = this@BoxWithConstraints.maxWidth * size)) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun ShortTextMarqueePreview() {
    Box(Modifier.size(600.dp)) {
        AlwaysMarquee(
            1.2f,
            marqueeModifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                repeatDelayMillis = 0,
                velocity = 80.dp,
                initialDelayMillis = 500,
            ),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "I'm small but I want to marquee!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                fontSize = 20.sp,
            )
        }
    }
}

@Preview
@Composable
private fun LongTextMarqueePreview() {
    Box(Modifier.size(600.dp)) {
        AlwaysMarquee(
            1.2f,
            marqueeModifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                repeatDelayMillis = 0,
                velocity = 80.dp,
                initialDelayMillis = 500,
            ),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),

                text = "I'm big and moving!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                fontSize = 70.sp,
            )
        }
    }
}

@Preview
@Composable
@LargeScreensPreview
private fun BackgroundQuotesPreview() {
    AndroidifyTheme {
        BackgroundRandomQuotes()
    }
}
