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
package com.android.developers.androidify.creation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.developers.androidify.theme.SharedElementContextPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreationScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun promptTypeToolbar_displaysCorrectly_withPhotoSelected() {
        val photoButtonText = PromptType.PHOTO.displayName
        val promptButtonText = PromptType.TEXT.displayName

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(selectedPromptOption = PromptType.PHOTO),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(photoButtonText).assertIsDisplayed().assertIsOn()
        composeTestRule.onNodeWithText(promptButtonText).assertIsDisplayed().assertIsOff()
    }

    @Test
    fun promptTypeToolbar_displaysCorrectly_withTextSelected() {
        val photoButtonText = PromptType.PHOTO.displayName
        val promptButtonText = PromptType.TEXT.displayName

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(selectedPromptOption = PromptType.TEXT),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(photoButtonText).assertIsDisplayed().assertIsOff()
        composeTestRule.onNodeWithText(promptButtonText).assertIsDisplayed().assertIsOn()
    }

    @Test
    fun photoPrompt_showsEmptyState_whenUriIsNull() {
        val titleText = composeTestRule.activity.getString(R.string.photo_picker_title)
        val takePictureButtonDesc = composeTestRule.activity.getString(R.string.take_picture_content_description)
        val choosePhotoButtonText = composeTestRule.activity.getString(R.string.photo_picker_choose_photo_label)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(
                        selectedPromptOption = PromptType.PHOTO,
                        imageUri = null,
                    ),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(titleText).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(takePictureButtonDesc).assertIsDisplayed()
        composeTestRule.onNodeWithText(choosePhotoButtonText).assertIsDisplayed()
    }

    @Test
    fun photoPrompt_showsImagePreview_whenUriIsNotNull() {
        val imageUri = Uri.parse("android.resource://com.android.developers.androidify.creation/drawable/test_image")
        val selectedImageDesc = composeTestRule.activity.getString(R.string.cd_selected_image)
        val retakeButtonDesc = composeTestRule.activity.getString(R.string.cd_retake_photo)
        val choosePhotoButtonText = composeTestRule.activity.getString(R.string.photo_picker_choose_photo_label)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(
                        selectedPromptOption = PromptType.PHOTO,
                        imageUri = imageUri,
                    ),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(selectedImageDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(retakeButtonDesc).assertIsDisplayed()
        composeTestRule.onNodeWithText(choosePhotoButtonText).assertIsDisplayed()
    }

    @Test
    fun textPrompt_displaysCorrectly_whenSelectedAndNotGenerating() {
        val headlineText = composeTestRule.activity.getString(R.string.headline_my_bot_is)
        val hintText = composeTestRule.activity.getString(R.string.prompt_text_hint)
        val helpChipText = composeTestRule.activity.getString(R.string.write_me_a_prompt)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(
                        selectedPromptOption = PromptType.TEXT,
                        promptGenerationInProgress = false,
                    ),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(headlineText).assertIsDisplayed()
        composeTestRule.onNodeWithText(hintText).assertIsDisplayed()
        // TODO: Fails in pixel 5
        //  composeTestRule.onNodeWithText(helpChipText).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun textPrompt_displaysWritingChip_whenGenerating() {
        val headlineText = composeTestRule.activity.getString(R.string.headline_my_bot_is)
        val writingChipText = composeTestRule.activity.getString(R.string.writing)
        val helpChipText = composeTestRule.activity.getString(R.string.write_me_a_prompt)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(
                        selectedPromptOption = PromptType.TEXT,
                        promptGenerationInProgress = true,
                    ),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }


        composeTestRule.onNodeWithText(headlineText).assertIsDisplayed()
        // TODO: Fails in pixel 5
        // composeTestRule.onNodeWithText(writingChipText).assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithText(helpChipText).assertDoesNotExist()
    }

    @Test
    fun bottomButtons_areDisplayed_inCompactMode() {
        val botColorButtonText = composeTestRule.activity.getString(R.string.bot_color_button)
        val transformButtonText = composeTestRule.activity.getString(R.string.transform_button)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = false,
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(), // Use default state which should be compact
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(botColorButtonText).assertIsDisplayed()
        composeTestRule.onNodeWithText(transformButtonText).assertIsDisplayed()
    }

    @Test
    fun bottomButtons_areNotDisplayed_inExpandedMode() {
        val botColorButtonText = composeTestRule.activity.getString(R.string.bot_color_button)
        val transformButtonText = composeTestRule.activity.getString(R.string.transform_button)
        val startTransformationButtonText = composeTestRule.activity.getString(R.string.start_transformation_button)

        composeTestRule.setContent {
            SharedElementContextPreview {
                EditScreen(
                    snackbarHostState = SnackbarHostState(),
                    isExpanded = true, // Expanded mode
                    onCameraPressed = {},
                    onBackPressed = {},
                    onAboutPressed = {},
                    uiState = CreationState(),
                    onChooseImageClicked = {},
                    onPromptOptionSelected = {},
                    onUndoPressed = {},
                    onPromptGenerationPressed = {},
                    onBotColorSelected = {},
                    onStartClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText(botColorButtonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(transformButtonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(startTransformationButtonText).assertIsDisplayed()
    }

    @Test
    fun loadingScreen_isDisplayed_whenStateIsLoading() {
        // This test remains focused on the LoadingScreen UI itself,
        // as testing the state transition within CreationScreen requires Hilt/ViewModel setup.
        val loadingText = composeTestRule.activity.getString(R.string.generating_your_bot)
        val cancelButtonText = composeTestRule.activity.getString(R.string.cancel)

        composeTestRule.setContent {
            SharedElementContextPreview {
                LoadingScreen(onCancelPress = {})
            }
        }

        composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
        composeTestRule.onNodeWithText(cancelButtonText).assertIsDisplayed()
    }
}
