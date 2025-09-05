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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.developers.androidify.customize

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.developers.testing.data.TestFileProvider
import com.android.developers.testing.data.bitmapSample
import com.android.developers.testing.network.TestRemoteConfigDataSource
import com.android.developers.testing.repository.FakeImageGenerationRepository
import com.android.developers.testing.repository.FakeWatchFaceInstallationRepository
import com.android.developers.testing.util.FakeComposableBitmapRenderer
import com.android.developers.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CustomizeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CustomizeExportViewModel
    private val originalFakeUri = Uri.parse("content://com.example.app/images/original.jpg")

    private val fakeUri = Uri.parse("content://com.example.app/images/original.jpg")

    @Before
    fun setup() {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = false
        viewModel = CustomizeExportViewModel(
            fakeUri,
            originalFakeUri,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            watchfaceInstallationRepository = FakeWatchFaceInstallationRepository(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = remoteConfigDataSource,
        )
    }

    @Test
    fun stateResultUri_NotNull() = runTest {
        val initialState = viewModel.state.value

        val newState = viewModel.state.first { it != initialState }
        assertNotNull(
            viewModel.state.value.exportImageCanvas.imageUri,
        )
    }

    @Test
    fun setArgumentsWithOriginalImage() = runTest {
        val initialState = viewModel.state.value

        // Ensure state has changed - view model uses combine to combine state flows so state
        // update is not immediate
        val newState = viewModel.state.first { it != initialState }

        assertEquals(
            CustomizeExportState(
                exportImageCanvas = ExportImageCanvas(imageUri = fakeUri, imageBitmap = bitmapSample),
                originalImageUrl = originalFakeUri,
            ),
            newState,
        )
    }

    @Test
    fun setArgumentsWithPrompt() = runTest {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = false
        val initialState = viewModel.state.value

        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            watchfaceInstallationRepository = FakeWatchFaceInstallationRepository(),
            remoteConfigDataSource = remoteConfigDataSource,
        )

        // Ensure state has changed - view model uses combine to combine state flows so state
        // update is not immediate
        val newState = viewModel.state.first { it != initialState }

        assertEquals(
            CustomizeExportState(
                exportImageCanvas = ExportImageCanvas(imageUri = fakeUri, imageBitmap = bitmapSample),
                originalImageUrl = null,
            ),
            newState,
        )
    }

    @Test
    fun downloadClicked() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }

        viewModel.downloadClicked()
        assertNotNull(values.last().externalOriginalSavedUri)
        assertEquals(
            originalFakeUri,
            values.last().externalOriginalSavedUri,
        )
    }

    @Test
    fun shareClicked() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.shareClicked()
        // Ensure all coroutines on the test scheduler complete
        advanceUntilIdle()
        assertNotNull(values.last().savedUri)
    }

    @Test
    fun changeBackground_NotNull() = runTest {
        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            watchfaceInstallationRepository = FakeWatchFaceInstallationRepository(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = TestRemoteConfigDataSource(false),
        )
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.selectedToolStateChanged(
            BackgroundToolState(
                selectedToolOption = BackgroundOption.Chef,
                options = listOf(
                    BackgroundOption.None,
                    BackgroundOption.IO,
                    BackgroundOption.Chef,
                ),
            ),
        )
        advanceUntilIdle()
        assertFalse { values[values.lastIndex].showImageEditProgress }
        // assertTrue(values.any { it.showImageEditProgress })
        assertNotNull(values.last().exportImageCanvas.imageWithEdit)
    }

    @Test
    fun changeBackground_None() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.selectedToolStateChanged(
            BackgroundToolState(
                selectedToolOption = BackgroundOption.None,
                options = listOf(
                    BackgroundOption.None,
                    BackgroundOption.IO,
                    BackgroundOption.Chef,
                ),
            ),
        )
        advanceUntilIdle()
        assertTrue { !values[values.lastIndex].showImageEditProgress }
        assertNull(values.last().exportImageCanvas.imageWithEdit)
    }

    @Test
    fun remoteConfigDataSource_BackgroundVibesFeatureEnabled_ContainsVibeList() = runTest {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = true
        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            watchfaceInstallationRepository = FakeWatchFaceInstallationRepository(),
            remoteConfigDataSource = remoteConfigDataSource,
        )

        val initialState = viewModel.state.value
        val newState = viewModel.state.first { it != initialState }

        val toolState = newState.toolState[CustomizeTool.Background] as BackgroundToolState

        assertTrue(toolState.options.size > 5)
        assertTrue(toolState.options.any { it.aiBackground })
    }
}
