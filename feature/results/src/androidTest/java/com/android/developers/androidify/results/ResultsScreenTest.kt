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
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Create a dummy bitmap for testing
    private val testBitmap: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    @Test
    fun resultsScreenContents_displaysActionButtons() {
        val shareButtonText = composeTestRule.activity.getString(R.string.share_your_bot)
        // Note: Download button is identified by icon, harder to test reliably without tags/desc

        val initialState = ResultState(resultImageBitmap = testBitmap, promptText = "test")
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }

        // Verify the Share button is displayed
        composeTestRule.onNodeWithText(shareButtonText).assertIsDisplayed()

        // TODO: Add assertion for Download button if a reliable finder (test tag/content desc) is added
    }

    // --- Add tests for BotResultCard flipping, toolbar options, etc. ---

    @Test
    fun toolbarOption_Bot_isSelectedByDefault_andFrontCardVisible() {
        val botOptionText = composeTestRule.activity.getString(R.string.bot)
        val photoOptionText = composeTestRule.activity.getString(R.string.prompt)
        val frontCardDesc = composeTestRule.activity.getString(R.string.resultant_android_bot)

        // Ensure promptText is non-null when bitmap is present
        val initialState = ResultState(resultImageBitmap = testBitmap, promptText = "test")
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }

        // Check toolbar state
        composeTestRule.onNodeWithText(botOptionText).assertIsOn()
        composeTestRule.onNodeWithText(photoOptionText).assertIsOff()

        // Check front card is visible
        composeTestRule.onNodeWithContentDescription(frontCardDesc).assertIsDisplayed()
    }

    @Test
    fun toolbarOption_ClickPhoto_selectsPhoto_andShowsBackCard_Image() {
        val botOptionText = composeTestRule.activity.getString(R.string.bot)
        val photoOptionText = composeTestRule.activity.getString(R.string.photo)
        val frontCardDesc = composeTestRule.activity.getString(R.string.resultant_android_bot)
        val backCardDesc = composeTestRule.activity.getString(R.string.original_image)
        val dummyUri = android.net.Uri.parse("dummy://image")

        val initialState = ResultState(resultImageBitmap = testBitmap, originalImageUrl = dummyUri)
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }

        // Click Photo option
        composeTestRule.onNodeWithText(photoOptionText).performClick()

        // Check toolbar state
        composeTestRule.onNodeWithText(botOptionText).assertIsOff()
        composeTestRule.onNodeWithText(photoOptionText).assertIsOn()

        // Check back card (image) is visible - front should not be (due to flip)
        // Note: Direct assertion on front might fail depending on FlippableCard implementation details.
        // It's safer to assert the *intended* visible content.
        composeTestRule.onNodeWithContentDescription(backCardDesc).assertIsDisplayed()
    }

    @Test
    fun toolbarOption_ClickPhoto_selectsPhoto_andShowsBackCard_Prompt() {
        val botOptionText = composeTestRule.activity.getString(R.string.bot)
        val photoOptionText = composeTestRule.activity.getString(R.string.prompt)
        val frontCardDesc = composeTestRule.activity.getString(R.string.resultant_android_bot)
        val promptText = "test prompt"
        val promptPrefix = composeTestRule.activity.getString(R.string.my_bot_is_wearing)

        val initialState = ResultState(resultImageBitmap = testBitmap, promptText = promptText) // No original image URI
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }

        // Click Photo option
        composeTestRule.onNodeWithText(photoOptionText).performClick()

        // Check toolbar state
        composeTestRule.onNodeWithText(botOptionText).assertIsOff()
        composeTestRule.onNodeWithText(photoOptionText).assertIsOn()

        // Check back card (prompt) is visible by finding its text
        composeTestRule.onNodeWithText(promptPrefix + " " + promptText, substring = true).assertIsDisplayed()
    }

    @Test
    fun toolbarOption_ClickBot_selectsBot_andShowsFrontCard() {
        val botOptionText = composeTestRule.activity.getString(R.string.bot)
        val photoOptionText = composeTestRule.activity.getString(R.string.photo)
        val frontCardDesc = composeTestRule.activity.getString(R.string.resultant_android_bot)
        val dummyUri = android.net.Uri.parse("dummy://image")

        val initialState = ResultState(resultImageBitmap = testBitmap, originalImageUrl = dummyUri)
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = {},
                )
            }
        }

        // Start by clicking Photo to select it
        composeTestRule.onNodeWithText(photoOptionText).performClick()
        composeTestRule.onNodeWithText(photoOptionText).assertIsOn() // Verify it's selected

        // Now click Bot
        composeTestRule.onNodeWithText(botOptionText).performClick()

        // Check toolbar state
        composeTestRule.onNodeWithText(botOptionText).assertIsOn()
        composeTestRule.onNodeWithText(photoOptionText).assertIsOff()

        // Check front card is visible again
        composeTestRule.onNodeWithContentDescription(frontCardDesc).assertIsDisplayed()
    }

    @Test
    fun actionButton_Share_invokesCallback() {
        val shareButtonText = composeTestRule.activity.getString(R.string.share_your_bot)
        var shareClicked = false

        // Ensure promptText is non-null when bitmap is present
        val initialState = ResultState(resultImageBitmap = testBitmap, promptText = "test")
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = {},
                    shareClicked = { shareClicked = true }, // Callback to test
                )
            }
        }

        composeTestRule.onNodeWithText(shareButtonText).performClick()

        assertTrue("shareClicked callback should have been invoked", shareClicked)
    }

    @Test
    fun actionButton_Download_invokesCallback() {
        val downloadButtonDesc = composeTestRule.activity.getString(R.string.download_bot) // Use the new content description
        var downloadClicked = false

        // Ensure promptText is non-null when bitmap is present
        val initialState = ResultState(resultImageBitmap = testBitmap, promptText = "test")
        val state = mutableStateOf(initialState)

        composeTestRule.setContent {
            // Disable animation
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ResultsScreenContents(
                    contentPadding = PaddingValues(0.dp),
                    state = state,
                    downloadClicked = { downloadClicked = true }, // Callback to test
                    shareClicked = {},
                )
            }
        }

        // Click the download button - using a sibling finder relative to Share is complex.
        // A more robust approach needs test tags.
        // As a placeholder, we'll just assert the callback wasn't called initially.
        // To make this test pass, manual interaction or a better finder is needed.
        // Find the node by its content description and click it
        // Note: We find the Icon, but click its parent (the Button)
        composeTestRule.onNodeWithContentDescription(downloadButtonDesc).performClick()

        // Assert the callback was invoked
        assertTrue("downloadClicked callback should have been invoked", downloadClicked)
    }
}
