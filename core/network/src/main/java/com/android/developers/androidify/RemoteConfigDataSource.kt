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
package com.android.developers.androidify

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteConfigDataSource {
    fun isAppInactive(): Boolean
    fun textModelName(): String
    fun imageModelName(): String
    fun isBackgroundVibesFeatureEnabled(): Boolean
    fun promptTextVerify(): String
    fun promptImageValidation(): String
    fun promptImageValidationNano(): String
    fun promptImageDescription(): String
    fun promptImageDescriptionNano(): String
    fun useGeminiNano(): Boolean
    fun enabledGeminiNanoModelVersions(): String
    fun generateBotPrompt(): String
    fun promptImageGenerationWithSkinTone(): String

    fun getPromoVideoLink(): String

    fun getDancingDroidLink(): String

    fun useImagen(): Boolean

    fun getFineTunedModelName(): String

    fun getImageGenerationEditsModelName(): String

    fun getBotBackgroundInstructionPrompt(): String

    fun watchfaceFeatureEnabled(): Boolean

    fun isXrEnabled(): Boolean
}

@Singleton
class RemoteConfigDataSourceImpl @Inject constructor() : RemoteConfigDataSource {
    private val remoteConfig = Firebase.remoteConfig

    override fun isAppInactive(): Boolean {
        return remoteConfig.getBoolean("is_android_app_inactive")
    }

    override fun textModelName(): String {
        return remoteConfig.getString("text_model_name")
    }

    override fun imageModelName(): String {
        return remoteConfig.getString("image_model_name")
    }

    override fun isBackgroundVibesFeatureEnabled(): Boolean {
        return remoteConfig.getBoolean("background_vibes_feature_enabled")
    }

    override fun promptTextVerify(): String {
        return remoteConfig.getString("prompt_text_verify")
    }

    override fun promptImageValidation(): String {
        return remoteConfig.getString("prompt_image_validation")
    }

    override fun promptImageValidationNano(): String {
        return remoteConfig.getString("prompt_image_validation_nano")
    }

    override fun promptImageDescription(): String {
        return remoteConfig.getString("prompt_image_description")
    }

    override fun promptImageDescriptionNano(): String {
        return remoteConfig.getString("prompt_image_description_nano")
    }

    override fun useGeminiNano(): Boolean {
        return remoteConfig.getBoolean("use_gemini_nano")
    }

    override fun enabledGeminiNanoModelVersions(): String {
        return remoteConfig.getString("enabled_gemini_nano_model_versions")
    }

    override fun generateBotPrompt(): String {
        return remoteConfig.getString("generate_bot_prompt")
    }

    override fun promptImageGenerationWithSkinTone(): String {
        return remoteConfig.getString("prompt_image_generation_skin_tone")
    }

    override fun getPromoVideoLink(): String {
        return remoteConfig.getString("promo_video_link")
    }

    override fun getDancingDroidLink(): String {
        return remoteConfig.getString("dancing_droid_gif_link")
    }

    override fun useImagen(): Boolean {
        return remoteConfig.getBoolean("use_imagen")
    }
    override fun getFineTunedModelName(): String {
        return remoteConfig.getString("fine_tuned_model_name")
    }

    override fun getImageGenerationEditsModelName(): String {
        return remoteConfig.getString("image_generation_model_edits")
    }

    override fun getBotBackgroundInstructionPrompt(): String {
        return remoteConfig.getString("bot_background_instruction_prompt")
    }

    override fun watchfaceFeatureEnabled(): Boolean {
        return remoteConfig.getBoolean("watchface_feature_enabled")
    }

    override fun isXrEnabled(): Boolean {
        return remoteConfig.getBoolean("xr_feature_enabled")
    }
}
