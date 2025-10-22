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
import kotlin.reflect.KClass

interface RemoteConfigDataSource {

    @Deprecated(
        "Use generic get method instead",
        ReplaceWith("get(RemoteConfigDataSource.Key.IsAppInactive())"),
    )
    fun isAppInactive(): Boolean = get(Key.IsAppInactive())

    fun textModelName(): String
    fun imageModelName(): String
    fun isBackgroundVibesFeatureEnabled(): Boolean
    fun promptTextVerify(): String
    fun promptImageValidation(): String
    fun promptImageDescription(): String
    fun useGeminiNano(): Boolean
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

    public fun <T : Any> get(key: Key<T>): T

    public abstract class Key<T : Any>(
        val id: String,
        val keyType: KClass<T>,
    ) {

        public class IsAppInactive : Key<Boolean>("is_android_app_inactive", Boolean::class)
        public class Custom<T : Any>(
            id: String,
            keyType: KClass<T>,
        ) : Key<T>(id, keyType)

        override fun toString(): String {
            return "Key(id='$id')"
        }
    }
}

class RemoteConfigDataSourceImpl @Inject constructor() : RemoteConfigDataSource {
    private val remoteConfig = Firebase.remoteConfig

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: RemoteConfigDataSource.Key<T>): T = when (key.keyType) {
        Boolean::class -> Firebase.remoteConfig.getBoolean(key.id)
        String::class -> Firebase.remoteConfig.getString(key.id)
        Long::class -> Firebase.remoteConfig.getLong(key.id)
        Double::class -> Firebase.remoteConfig.getDouble(key.id)
        Int::class -> Firebase.remoteConfig.getLong(key.id).toInt()
        else -> throw IllegalArgumentException("Unsupported key type: ${key.keyType}")
    } as T

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

    override fun promptImageDescription(): String {
        return remoteConfig.getString("prompt_image_description")
    }

    override fun useGeminiNano(): Boolean {
        return remoteConfig.getBoolean("use_gemini_nano")
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
