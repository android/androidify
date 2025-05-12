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
import androidx.compose.material3.SnackbarHostState
import com.android.developers.androidify.data.InsufficientInformationException
import com.android.developers.testing.data.TestFileProvider
import com.android.developers.testing.data.TestInternetConnectivityManager
import com.android.developers.testing.repository.FakeImageGenerationRepository
import com.android.developers.testing.repository.TestTextGenerationRepository
import com.android.developers.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CreationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CreationViewModel

    private val internetConnectivityManager = TestInternetConnectivityManager(true)
    private val imageGenerationRepository = FakeImageGenerationRepository()

    @Before
    fun setup() {
        viewModel = CreationViewModel(
            internetConnectivityManager,
            imageGenerationRepository,
            TestTextGenerationRepository(),
            TestFileProvider(),
            UnconfinedTestDispatcher(),
            context = RuntimeEnvironment.getApplication(),
        )
    }

    @Test
    fun stateInitialEdit() = runTest {
        assertEquals(
            ScreenState.EDIT,
            viewModel.uiState.value.screenState,
        )
        assertEquals(false, viewModel.uiState.value.promptGenerationInProgress)
        assertEquals(null, viewModel.uiState.value.imageUri)
    }

    @Test
    fun onImageSelected_updatesUiState() = runTest {
        val fakeUri = Uri.parse("content://test/image.jpg")
        viewModel.onImageSelected(fakeUri)

        assertEquals(fakeUri, viewModel.uiState.value.imageUri)
        assertEquals(PromptType.PHOTO, viewModel.uiState.value.selectedPromptOption)
    }

    @Test
    fun onBotColorChanged_updatesUiState() = runTest {
        val newBotColor = BotColor("Test Color", "test-color") // Assuming you can create a BotColor
        viewModel.onBotColorChanged(newBotColor)

        assertEquals(newBotColor, viewModel.uiState.value.botColor)
    }

    @Test
    fun onSelectedPromptOptionChanged_updatesUiState() = runTest {
        viewModel.onSelectedPromptOptionChanged(PromptType.TEXT)

        assertEquals(PromptType.TEXT, viewModel.uiState.value.selectedPromptOption)
    }

    @Test
    fun promptGenerationClicked() = runTest {
        viewModel.onPromptGenerationClicked()
        assertEquals("Test prompt", viewModel.uiState.value.generatedPrompt)
        assertEquals(false, viewModel.uiState.value.promptGenerationInProgress)
    }

    @Test
    fun startClicked_GenerateBotFromPhoto() = runTest {
        val screenStateValues = mutableListOf<ScreenState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {
                screenStateValues.add(it.screenState)
            }
        }

        viewModel.onImageSelected(Uri.parse("content://test/image.jpg"))
        viewModel.onSelectedPromptOptionChanged(PromptType.PHOTO)
        viewModel.startClicked()
        assertEquals(ScreenState.RESULT, viewModel.uiState.value.screenState)
        assertNotNull(viewModel.uiState.value.resultBitmap)
    }

    @Test
    fun startClicked_GenerateBotFromPhoto_NoPhotoSelected() = runTest {
        val values = mutableListOf<SnackbarHostState>()

        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.snackbarHostState.collect {
                values.add(it)
            }
        }

        viewModel.onSelectedPromptOptionChanged(PromptType.PHOTO)
        viewModel.startClicked()
        assertEquals(ScreenState.EDIT, viewModel.uiState.value.screenState)
        assertNotNull("Choose an image or use a prompt instead.", values.last().currentSnackbarData?.visuals?.message)
    }

    @Test
    fun startClicked_GenerateBotFromPrompt_TextEmpty() = runTest {
        val values = mutableListOf<SnackbarHostState>()

        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.snackbarHostState.collect {
                values.add(it)
            }
        }
        val screenStateValues = mutableListOf<ScreenState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {
                screenStateValues.add(it.screenState)
            }
        }
        imageGenerationRepository.exceptionToThrow = InsufficientInformationException()

        viewModel.onSelectedPromptOptionChanged(PromptType.TEXT)
        viewModel.startClicked()

        assertEquals(ScreenState.EDIT, screenStateValues[1])
        assertEquals(
            "Provide a more detailed description about your bot, include hair color, clothes and accessories.",
            values.last().currentSnackbarData?.visuals?.message,
        )
        imageGenerationRepository.exceptionToThrow = null
    }

    @Test
    fun startClicked_GenerateBotFromPrompt() = runTest {
        val screenStateValues = mutableListOf<ScreenState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {
                screenStateValues.add(it.screenState)
            }
        }
        viewModel.onSelectedPromptOptionChanged(PromptType.TEXT)
        viewModel.uiState.value.descriptionText.edit {
            "testing input description"
        }
        viewModel.startClicked()
        assertEquals(ScreenState.RESULT, viewModel.uiState.value.screenState)
        assertNotNull(viewModel.uiState.value.resultBitmap)
    }

    @Test
    fun startClicked_NoInternet_DisplaysError() = runTest {
        val values = mutableListOf<SnackbarHostState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.snackbarHostState.collect {
                values.add(it)
            }
        }
        internetConnectivityManager.internetAvailable = false
        viewModel.startClicked()
        advanceUntilIdle()
        assertEquals(ScreenState.EDIT, viewModel.uiState.value.screenState)
        assertEquals(
            "No internet connection, please check your connection and try again.",
            values.last().currentSnackbarData?.visuals?.message,
        )

        internetConnectivityManager.internetAvailable = true
    }

    @Test
    fun onUndoPressed() = runTest {
        viewModel.onImageSelected(Uri.parse("content://test/image.jpg"))
        viewModel.onUndoPressed()
        assertEquals(null, viewModel.uiState.value.imageUri)
    }
}
