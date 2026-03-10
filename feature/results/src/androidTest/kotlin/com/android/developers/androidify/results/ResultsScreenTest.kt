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

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.developers.androidify.data.ConfigProvider
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.testing.network.TestRemoteConfigDataSource
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Create a test bitmap for testing
    val testUri = Uri.parse("placeholder://image")

    @Test
    fun resultsScreenContents_displaysActionButtons() {
        val shareButtonText = composeTestRule.activity.getString(R.string.customize_and_share)
        // Note: Download button is identified by icon, harder to test reliably without tags/desc

        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, null, promptText = "test", configProvider)

        composeTestRule.setContent {
            SharedElementContextPreview {
                // Disable animation
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
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
        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, null, promptText = "test", configProvider)

        composeTestRule.setContent {
            SharedElementContextPreview {
                // Disable animation
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
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
        val testUri = Uri.parse("placeholder://image")

        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, testUri, null, configProvider)

        composeTestRule.setContent {
            SharedElementContextPreview {
                // Disable animation
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
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
        val promptOptionText = composeTestRule.activity.getString(R.string.prompt)
        val promptText = "test prompt"
        val promptPrefix = composeTestRule.activity.getString(R.string.my_bot_is_wearing)

        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, null, promptText, configProvider)

        composeTestRule.setContent {
            SharedElementContextPreview {
                // Disable animation
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
            }
        }

        // Click Photo option
        composeTestRule.onNodeWithText(promptOptionText).performClick()

        // Check toolbar state
        composeTestRule.onNodeWithText(botOptionText).assertIsOff()
        composeTestRule.onNodeWithText(promptOptionText).assertIsOn()

        // Check back card (prompt) is visible by finding its text
        composeTestRule.onNodeWithText(promptPrefix + " " + promptText, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun toolbarOption_ClickBot_selectsBot_andShowsFrontCard() {
        val botOptionText = composeTestRule.activity.getString(R.string.bot)
        val photoOptionText = composeTestRule.activity.getString(R.string.photo)
        val frontCardDesc = composeTestRule.activity.getString(R.string.resultant_android_bot)
        val testUri = Uri.parse("placeholder://image")

        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, originalImageUrl = testUri, null, configProvider)

        composeTestRule.setContent {
            // Disable animation
            SharedElementContextPreview {
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
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
    fun actionButton_CustomizeExport_invokesCallback() {
        val shareButtonText = composeTestRule.activity.getString(R.string.customize_and_share)
        var shareClicked = false

        // Ensure promptText is non-null when bitmap is present
        val configProvider = ConfigProvider(TestRemoteConfigDataSource(false))
        val viewModel = ResultsViewModel(testUri, originalImageUrl = null, "test", configProvider)

        composeTestRule.setContent {
            SharedElementContextPreview {
                // Disable animation
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    ResultsScreen(
                        onBackPress = {},
                        onAboutPress = {},
                        onNextPress = { _, _ ->
                            shareClicked = true
                        },
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(shareButtonText).performClick()

        assertTrue("onCustomizeShareClicked callback should have been invoked", shareClicked)
    }
}
