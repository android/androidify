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

package com.android.developers.androidify.results

import android.graphics.Bitmap
import android.net.Uri
import com.android.developers.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResultsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ResultsViewModel

    private val fakeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val fakePromptText = "Pink Hair, plaid shirt, jeans"
    private val originalFakeUri = Uri.parse("content://com.example.app/images/original.jpg")

    private val fakeUri = Uri.parse("content://test/image.jpg")

    @Before
    fun setup() {
        viewModel = ResultsViewModel(
            fakeUri,
            originalFakeUri,
            fakePromptText
        )
    }

    @Test
    fun stateInitialEmpty() = runTest {
        assertEquals(
            ResultState(),
            viewModel.state.value,
        )
    }

    @Test
    fun setArgumentsWithOriginalImage() = runTest {
        assertEquals(
            ResultState(
                resultImageUri = fakeUri,
                originalImageUrl = originalFakeUri,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun setArgumentsWithPrompt() = runTest {
        assertEquals(
            ResultState(
                resultImageUri = fakeUri,
                originalImageUrl = null,
                promptText = fakePromptText,
            ),
            viewModel.state.value,
        )
    }
}
