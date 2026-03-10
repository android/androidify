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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Helper composable for the viewfinder slot in tests
    private val dummyViewfinder: @Composable (Modifier) -> Unit = { modifier -> Box(modifier) }

    @Test
    fun captureButton_isEnabled_whenPoseDetected() {
        val captureButtonDesc = composeTestRule.activity.getString(R.string.cd_capture_button)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                detectedPose = true, // Pose detected
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNode(hasClickActionWithLabel(captureButtonDesc)).assertIsEnabled()
    }

    @Test
    fun captureButton_isDisabled_whenPoseNotDetected() {
        val captureButtonDesc = composeTestRule.activity.getString(R.string.cd_capture_button)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                detectedPose = false, // Pose NOT detected
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNode(hasClickActionWithLabel(captureButtonDesc)).assertIsNotEnabled()
    }

    @Test
    fun captureButton_click_invokesCallback_whenEnabled() {
        val captureButtonDesc = composeTestRule.activity.getString(R.string.cd_capture_button)
        var wasClicked = false

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                detectedPose = true, // Ensure button is enabled
                requestCaptureImage = { wasClicked = true }, // Callback to test
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
            )
        }

        composeTestRule.onNode(hasClickActionWithLabel(captureButtonDesc)).performClick()

        assertTrue("requestCaptureImage callback should have been invoked", wasClicked)
    }

    @Test
    fun flipCameraButton_isDisplayed_whenCanFlipIsTrue() {
        val flipButtonDesc = composeTestRule.activity.getString(R.string.flip_camera_direction)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                canFlipCamera = true, // Can flip
                // Default values for other params
                detectedPose = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNode(hasClickActionWithLabel(flipButtonDesc)).assertIsDisplayed()
    }

    @Test
    fun flipCameraButton_isNotDisplayed_whenCanFlipIsFalse() {
        val flipButtonDesc = composeTestRule.activity.getString(R.string.flip_camera_direction)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                canFlipCamera = false, // Cannot flip
                // Default values for other params
                detectedPose = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        // Assert the node does not exist, as the composable slot will be empty
        composeTestRule.onNodeWithContentDescription(flipButtonDesc).assertDoesNotExist()
    }

    @Test
    fun flipCameraButton_click_invokesCallback_whenDisplayed() {
        val flipButtonDesc = composeTestRule.activity.getString(R.string.flip_camera_direction)
        var wasClicked = false

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                canFlipCamera = true, // Ensure button is displayed
                requestFlipCamera = { wasClicked = true }, // Callback to test
                // Default values for other params
                detectedPose = true,
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNode(hasClickActionWithLabel(flipButtonDesc)).performClick()

        assertTrue("requestFlipCamera callback should have been invoked", wasClicked)
    }

    @Test
    fun zoomToolbar_isDisplayed_whenTwoOptionsProvided() {
        val zoomOptions = listOf(0.6f, 1.0f)
        val expectedText1 = ".6X" // Assuming initial zoom is < 1.0f
        val expectedText2 = "1X"

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                defaultZoomOptions = zoomOptions,
                zoomLevel = { 0.6f }, // Initial zoom
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                detectedPose = true,
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNodeWithText(expectedText1).assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedText2).assertIsDisplayed()
    }

    @Test
    fun zoomToolbar_isNotDisplayed_whenNotTwoOptionsProvided() {
        val zoomOptions = listOf(1.0f) // Only one option

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                defaultZoomOptions = zoomOptions,
                zoomLevel = { 1.0f },
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                detectedPose = true,
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        // Since the component returns early, the nodes with potential text won't exist
        composeTestRule.onNodeWithText("1X").assertDoesNotExist()
    }

    @Test
    fun zoomToolbar_firstButtonClick_invokesCallbackWithFirstOption() {
        val zoomOptions = listOf(0.6f, 1.0f)
        val buttonText = ".6X" // Text of the first button
        var changedZoomLevel: Float? = null

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                defaultZoomOptions = zoomOptions,
                zoomLevel = { 1.0f }, // Start at second option to ensure first button click changes it
                onAnimateZoom = { changedZoomLevel = it }, // Callback to test
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                detectedPose = true,
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNodeWithText(buttonText).performClick()

        assertTrue("onChangeZoomLevel should have been called", changedZoomLevel != null)
        assertTrue("Zoom level should be changed to ${zoomOptions[0]}", changedZoomLevel == zoomOptions[0])
    }

    @Test
    fun zoomToolbar_secondButtonClick_invokesCallbackWithSecondOption() {
        val zoomOptions = listOf(0.6f, 1.0f)
        val buttonText = "1X" // Text of the second button
        var changedZoomLevel: Float? = null

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                defaultZoomOptions = zoomOptions,
                zoomLevel = { 0.6f }, // Start at first option
                onAnimateZoom = { changedZoomLevel = it }, // Callback to test
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                detectedPose = true,
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNodeWithText(buttonText).performClick()

        assertTrue("onChangeZoomLevel should have been called", changedZoomLevel != null)
        assertTrue("Zoom level should be changed to ${zoomOptions[1]}", changedZoomLevel == zoomOptions[1])
    }

    @Test
    fun guideText_isNotDisplayed_whenPoseDetected() {
        val guideText = composeTestRule.activity.getString(R.string.camera_guide_text_label)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                detectedPose = true, // Pose detected
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        composeTestRule.onNodeWithText(guideText).assertIsNotDisplayed()
    }

    @Test
    fun guideText_isDisplayed_whenPoseNotDetected() {
        val guideText = composeTestRule.activity.getString(R.string.camera_guide_text_label)

        composeTestRule.setContent {
            StatelessCameraPreviewContent(
                viewfinder = dummyViewfinder,
                detectedPose = false, // Pose NOT detected
                // Default values for other params
                canFlipCamera = true,
                requestFlipCamera = {},
                defaultZoomOptions = listOf(1f),
                zoomLevel = { 1f },
                onAnimateZoom = {},
                requestCaptureImage = {},
            )
        }

        // Assert the node does not exist, as the composable slot will be empty
        composeTestRule.onNodeWithText(guideText).assertIsDisplayed()
    }

    // Helper method to test for the existence of the action with the correct label
    fun hasClickActionWithLabel(label: String): SemanticsMatcher {
        return SemanticsMatcher("") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label == label
        }
    }
}
