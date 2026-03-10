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
package com.android.developers.testing.network

import com.android.developers.androidify.RemoteConfigDataSource

class TestRemoteConfigDataSource(private val useGeminiNano: Boolean) : RemoteConfigDataSource {
    override fun isAppInactive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun textModelName(): String {
        TODO("Not yet implemented")
    }

    override fun imageModelName(): String {
        TODO("Not yet implemented")
    }
    var backgroundVibeEnabled: Boolean = true
    override fun isBackgroundVibesFeatureEnabled(): Boolean {
        return backgroundVibeEnabled
    }

    override fun promptTextVerify(): String {
        TODO("Not yet implemented")
    }

    override fun promptImageValidation(): String {
        TODO("Not yet implemented")
    }

    override fun promptImageValidationNano(): String {
        TODO("Not yet implemented")
    }

    override fun promptImageDescription(): String {
        TODO("Not yet implemented")
    }

    override fun promptImageDescriptionNano(): String {
        TODO("Not yet implemented")
    }

    override fun useGeminiNano(): Boolean {
        return useGeminiNano
    }

    override fun enabledGeminiNanoModelVersions(): String {
        TODO("Not yet implemented")
    }

    override fun generateBotPrompt(): String {
        return "generateBotPrompt"
    }

    override fun promptImageGenerationWithSkinTone(): String {
        TODO("Not yet implemented")
    }
    override fun getPromoVideoLink(): String {
        TODO("Not yet implemented")
    }
    override fun getDancingDroidLink(): String {
        TODO("Not yet implemented")
    }

    override fun useImagen(): Boolean {
        return true
    }

    override fun getFineTunedModelName(): String {
        return "test-fine-tuned-model"
    }

    override fun getImageGenerationEditsModelName(): String {
        return "test_image_model"
    }

    override fun getBotBackgroundInstructionPrompt(): String {
        return "bot_background_instruction_prompt"
    }

    override fun watchfaceFeatureEnabled(): Boolean {
        return true
    }

    override fun isXrEnabled(): Boolean {
        return false
    }
}
