/*
 * Copyright 2026 The Android Open Source Project
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
package com.android.developers.androidify.data

import com.android.developers.testing.data.TestFileProvider
import com.android.developers.testing.data.TestGeminiNanoDownloader
import com.android.developers.testing.data.TestGeminiNanoGenerationDataSource
import com.android.developers.testing.data.TestInternetConnectivityManager
import com.android.developers.testing.network.TestFirebaseAiDataSource
import com.android.developers.testing.network.TestLocalSegmentationDataSource
import com.android.developers.testing.network.TestRemoteConfigDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ImageGenerationRepositoryTest {
    private lateinit var repository: ImageGenerationRepository

    @Test
    fun getDescriptionFromImage_NanoDisabled() = runTest {
        repository = ImageGenerationRepositoryImpl(
            TestFileProvider(),
            TestInternetConnectivityManager(true),
            TestGeminiNanoGenerationDataSource("", TestGeminiNanoDownloader(false)),
            TestFirebaseAiDataSource(listOf("")),
            TestRemoteConfigDataSource(false),
            TestLocalSegmentationDataSource(),
        )

        val description = repository.getDescriptionFromImage(File(""))
        assertEquals(description.userDescription, "Firebase description")
    }

    @Test
    fun getDescriptionFromImage_NanoEnabledAndDownloaded() = runTest {
        repository = ImageGenerationRepositoryImpl(
            TestFileProvider(),
            TestInternetConnectivityManager(true),
            TestGeminiNanoGenerationDataSource("", TestGeminiNanoDownloader(true)),
            TestFirebaseAiDataSource(listOf("")),
            TestRemoteConfigDataSource(true),
            TestLocalSegmentationDataSource(),
        )

        val description = repository.getDescriptionFromImage(File(""))
        assertEquals(description.userDescription, "Nano description")
    }

    @Test
    fun getDescriptionFromImage_NanoEnabledButNotDownloaded() = runTest {
        repository = ImageGenerationRepositoryImpl(
            TestFileProvider(),
            TestInternetConnectivityManager(true),
            TestGeminiNanoGenerationDataSource("", TestGeminiNanoDownloader(false)),
            TestFirebaseAiDataSource(listOf("")),
            TestRemoteConfigDataSource(true),
            TestLocalSegmentationDataSource(),
        )

        val description = repository.getDescriptionFromImage(File(""))
        assertEquals(description.userDescription, "Firebase description")
    }
}
