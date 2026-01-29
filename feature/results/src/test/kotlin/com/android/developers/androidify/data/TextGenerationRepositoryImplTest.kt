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
package com.android.developers.androidify.data

import com.android.developers.testing.data.TestGeminiNanoDownloader
import com.android.developers.testing.data.TestGeminiNanoGenerationDataSource
import com.android.developers.testing.network.TestFirebaseAiDataSource
import com.android.developers.testing.network.TestRemoteConfigDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextGenerationRepositoryImplTest {

    @Test
    fun `Initial prompt generation`() = runTest {
        val output = "prompt"
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            output,
            TestGeminiNanoDownloader(false),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(emptyList())
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )
        // Test that the method generates a new list of prompts when
        // currentPrompts is null.
        val prompt = repository.getNextGeneratedBotPrompt()
        assertEquals(output, prompt)
    }

    @Test
    fun `Nano disabled uses VertexAI`() = runTest {
        val output = "prompt"
        val prompts = listOf("prompt1", "prompt2")
        val remoteConfigDataSource = TestRemoteConfigDataSource(false)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            output,
            TestGeminiNanoDownloader(false),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(prompts)
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )
        // Test that the method generates a new list of prompts when
        // currentPrompts is null.
        val prompt = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[0], prompt)
    }

    @Test
    fun `getNextGeneratedPrompt when currentPromptIndex at end`() = runTest {
        // Test that when `currentPromptIndex` equals `currentPrompts.size`,
        // a new prompt list is generated, and the first prompt from the new
        // list is returned.
        val prompts = listOf("prompt1", "prompt2")
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            null,
            TestGeminiNanoDownloader(false),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(prompts)
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )

        val prompt1 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[0], prompt1)
        val prompt2 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[1], prompt2)
        val prompt3 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[0], prompt3)
    }

    @Test
    fun `getNextGeneratedPrompt subsequent calls`() = runTest {
        // Test that subsequent calls to `getNextGeneratedPrompt()` return the next
        // prompt in the list
        val prompts = listOf("prompt1", "prompt2", "prompt3")
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            null,
            TestGeminiNanoDownloader(false),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(prompts)
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )

        val prompt1 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[0], prompt1)
        val prompt2 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[1], prompt2)
        val prompt3 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[2], prompt3)
    }

    @Test
    fun `generatePrompts Gemini Nano empty result`() = runTest {
        // Test that when `geminiNanoDataSource.generatePrompt()` returns an empty
        // result, the function falls back to `firebaseAiDataSource.generatePrompt()`.
        val prompts = listOf("prompt1", "prompt2", "prompt3")
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            null,
            TestGeminiNanoDownloader(true),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(prompts)
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )

        val prompt1 = repository.getNextGeneratedBotPrompt()
        assertEquals(prompts[0], prompt1)
    }

    @Test
    fun `generatePrompts Firebase VertexAI empty result`() = runTest {
        // Test that when both `geminiNanoDataSource` and
        // `firebaseAiDataSource` return empty or null results,
        // `generatePrompts()` returns null.
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        val geminiNanoDataSource = TestGeminiNanoGenerationDataSource(
            null,
            TestGeminiNanoDownloader(false),
        )
        val firebaseAiDataSource = TestFirebaseAiDataSource(emptyList())
        val repository =
            TextGenerationRepositoryImpl(
                remoteConfigDataSource,
                geminiNanoDataSource,
                firebaseAiDataSource,
            )

        val prompt = repository.getNextGeneratedBotPrompt()
        assertNull(prompt)
    }
}
