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
package com.android.developers.androidify.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.developers.androidify.theme.SharedElementContextPreview
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backButton_invokesCallback() {
        var wasClicked = false
        val backButtonContentDesc = composeTestRule.activity.getString(R.string.about_back_content_description)

        composeTestRule.setContent {
            SharedElementContextPreview {
                AboutScreenContents(
                    onBackPressed = { wasClicked = true },
                    onTermsClicked = {},
                    onPrivacyClicked = {},
                    onLicensesClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(backButtonContentDesc).performClick()
        assertTrue("onBackPressed callback should have been invoked", wasClicked)
    }

    @Test
    fun termsButton_invokesCallback() {
        var wasClicked = false
        val termsButtonText = composeTestRule.activity.getString(R.string.terms)

        composeTestRule.setContent {
            SharedElementContextPreview {
                AboutScreenContents(
                    onBackPressed = {},
                    onTermsClicked = { wasClicked = true },
                    onPrivacyClicked = {},
                    onLicensesClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText(termsButtonText).performClick()
        assertTrue("onTermsClicked callback should have been invoked", wasClicked)
    }

    @Test
    fun privacyButton_invokesCallback() {
        var wasClicked = false
        val privacyButtonText = composeTestRule.activity.getString(R.string.privacy)

        composeTestRule.setContent {
            SharedElementContextPreview {
                AboutScreenContents(
                    onBackPressed = {},
                    onTermsClicked = {},
                    onPrivacyClicked = { wasClicked = true },
                    onLicensesClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithText(privacyButtonText).performClick()
        assertTrue("onPrivacyClicked callback should have been invoked", wasClicked)
    }

    @Test
    fun licensesButton_invokesCallback() {
        var wasClicked = false
        val licensesButtonText = composeTestRule.activity.getString(R.string.oss_license)

        composeTestRule.setContent {
            SharedElementContextPreview {
                AboutScreenContents(
                    onBackPressed = {},
                    onTermsClicked = {},
                    onPrivacyClicked = {},
                    onLicensesClicked = { wasClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(licensesButtonText).performClick()
        assertTrue("onLicensesClicked callback should have been invoked", wasClicked)
    }
}

