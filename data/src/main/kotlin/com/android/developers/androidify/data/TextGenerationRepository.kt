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

import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.vertexai.FirebaseAiDataSource
import javax.inject.Inject
import javax.inject.Singleton

interface TextGenerationRepository {
    suspend fun getNextGeneratedBotPrompt(): String?
}

@Singleton
class TextGenerationRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val geminiNanoDataSource: GeminiNanoGenerationDataSource,
    private val firebaseAiDataSource: FirebaseAiDataSource,
) : TextGenerationRepository {

    private var currentPrompts: List<String>? = null
    private var currentPromptIndex = 0

    override suspend fun getNextGeneratedBotPrompt(): String? {
        val prompts = currentPrompts
        if (prompts.isNullOrEmpty() || currentPromptIndex >= prompts.size) {
            currentPrompts = generateBotPrompts()
            if (currentPrompts.isNullOrEmpty()) {
                return null
            } else {
                // We finished our list of prompts, get new prompts
                return getNextGeneratedBotPrompt()
            }
        }
        val currentPrompt = prompts[currentPromptIndex]
        currentPromptIndex++
        return currentPrompt
    }

    /**
     * Generate a prompt for creating a bot.
     * If Gemini Nano is available, use it. Otherwise, use Vertex AI
     */
    private suspend fun generateBotPrompts(): List<String> {
        val prompt = remoteConfigDataSource.generateBotPrompt()
        // We're getting new set of prompts so resetting the prompt index
        currentPromptIndex = 0

        val nanoResult = if (remoteConfigDataSource.useGeminiNano()) {
            // If Gemini Nano is not available, it will return null
            geminiNanoDataSource.generatePrompt(prompt)
        } else {
            null
        }
        // If we're not getting a result from Nano, try with VertexAI
        if (nanoResult.isNullOrEmpty()) {
            val result = firebaseAiDataSource.generatePrompt(prompt).generatedPrompts
            // if we're still not getting results, just return and empty list
            return result ?: emptyList()
        } else {
            // use the Nano result
            return listOf(nanoResult)
        }
    }
}
