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
package com.android.developers.androidify.util

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.round
import kotlinx.coroutines.delay
import java.text.BreakIterator
import java.text.StringCharacterIterator

/*
* Copyright 2025 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
 * Skips to the end size for a particular composable, skipping through the intermediate animated sizes.
 * Similar to skipToLookaheadSize, but for placement instead.
 * This is useful if you'd like your content to be placed in its final position and not have any animations affect its layout.
 * See the usage on the CameraPreviewScreen composable, we want the camera contents to remain in place,
 * but the animation should perform a progressive reveal.
 *
 * @param scope The SharedTransitionScope where the transition is taking place.
 * @return Modifier chain.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.skipToLookaheadPlacement(scope: SharedTransitionScope): Modifier =
    this.approachLayout(
        isMeasurementApproachInProgress = { false },
        isPlacementApproachInProgress = { scope.isTransitionActive },
    ) { measurable, constraints ->
        measurable.measure(constraints).run {
            layout(width, height) {
                coordinates?.let {
                    with(scope) {
                        val target = lookaheadScopeCoordinates.localLookaheadPositionOf(it)
                        val actual = lookaheadScopeCoordinates.localPositionOf(it)
                        place((target - actual).round())
                    }
                } ?: place(0, 0)
            }
        }
    }

@Composable
fun AnimatedTextField(
    textFieldState: TextFieldState,
    targetEndState: String? = null, // when this value is null, no animation will happen
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    decorator: TextFieldDecorator? = null,
) {
    // Use BreakIterator as it correctly iterates over characters regardless of how they are
    // stored, for example, some emojis are made up of multiple characters.
    // You don't want to break up an emoji as it animates, so using BreakIterator will ensure
    // this is correctly handled!
    val breakIterator = remember(targetEndState) { BreakIterator.getCharacterInstance() }

    // Define how many milliseconds between each character should pause for. This will create the
    // illusion of an animation, as we delay the job after each character is iterated on.
    val typingDelayInMs = 30L

    LaunchedEffect(targetEndState) {
        if (targetEndState != null && !textFieldState.text.startsWith(targetEndState)) {
            // Initial start delay of the typing animation
            delay(200)
            breakIterator.text = StringCharacterIterator(targetEndState)

            var nextIndex = breakIterator.next()
            // Iterate over the string, by index boundary
            while (nextIndex != BreakIterator.DONE) {
                textFieldState.edit {
                    replace(0, length, targetEndState.subSequence(0, nextIndex).toString())
                }
                // Go to the next logical character boundary
                nextIndex = breakIterator.next()
                delay(typingDelayInMs)
            }
        }
    }
    BasicTextField(
        state = textFieldState,
        modifier = modifier,
        textStyle = textStyle,
        decorator = decorator,
    )
}
