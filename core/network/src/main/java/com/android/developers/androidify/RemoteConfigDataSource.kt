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
package app.getnuri

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteConfigDataSource {
    fun isAppInactive(): Boolean
    fun textModelName(): String
    fun imageModelName(): String
    
    // Updated nutrition-focused methods
    fun promptMealPhotoValidation(): String
    fun promptMealAnalysis(): String
    fun promptIngredientExtraction(): String
    fun promptSymptomProcessing(): String
    fun promptFoodIntoleranceDetection(): String
    fun promptPatternAnalysis(): String
    fun promptVoiceInputProcessing(): String
    fun promptNutritionEstimation(): String
    fun promptHealthDataCorrelation(): String
    fun promptMealContextAnalysis(): String
    
    fun useGeminiNano(): Boolean
    fun enableAdvancedAnalytics(): Boolean
    
    fun getNutritionTipsGifLink(): String

    // Deprecated methods - kept for backward compatibility during transition
    @Deprecated("Use nutrition-focused methods instead")
    fun promptTextVerify(): String = promptSymptomProcessing()
    
    @Deprecated("Use promptMealPhotoValidation() instead")
    fun promptImageValidation(): String = promptMealPhotoValidation()
    
    @Deprecated("Use promptIngredientExtraction() instead") 
    fun promptImageDescription(): String = promptIngredientExtraction()
    
    @Deprecated("Use nutrition-focused methods instead")
    fun generateBotPrompt(): String = ""
    
    @Deprecated("Use nutrition-focused methods instead")
    fun promptImageGenerationWithSkinTone(): String = ""
    
    @Deprecated("Use nutrition-focused methods instead")
    fun getPromoVideoLink(): String = ""
    
    @Deprecated("Use getNutritionTipsGifLink() instead")
    fun getDancingDroidLink(): String = getNutritionTipsGifLink()
}

@Singleton
class RemoteConfigDataSourceImpl @Inject constructor() : RemoteConfigDataSource {
    private val remoteConfig = Firebase.remoteConfig

    override fun isAppInactive(): Boolean {
        return remoteConfig.getBoolean("is_app_active").not()
    }

    override fun textModelName(): String {
        return remoteConfig.getString("text_model_name")
    }

    override fun imageModelName(): String {
        return remoteConfig.getString("image_model_name")
    }

    override fun promptMealPhotoValidation(): String {
        return remoteConfig.getString("prompt_meal_photo_validation")
    }

    override fun promptMealAnalysis(): String {
        return remoteConfig.getString("prompt_meal_analysis")
    }

    override fun promptIngredientExtraction(): String {
        return remoteConfig.getString("prompt_ingredient_extraction")
    }

    override fun promptSymptomProcessing(): String {
        return remoteConfig.getString("prompt_symptom_processing")
    }

    override fun promptFoodIntoleranceDetection(): String {
        return remoteConfig.getString("prompt_food_intolerance_detection")
    }

    override fun promptPatternAnalysis(): String {
        return remoteConfig.getString("prompt_pattern_analysis")
    }

    override fun promptVoiceInputProcessing(): String {
        return remoteConfig.getString("prompt_voice_input_processing")
    }

    override fun promptNutritionEstimation(): String {
        return remoteConfig.getString("prompt_nutrition_estimation")
    }

    override fun promptHealthDataCorrelation(): String {
        return remoteConfig.getString("prompt_health_data_correlation")
    }

    override fun promptMealContextAnalysis(): String {
        return remoteConfig.getString("prompt_meal_context_analysis")
    }

    override fun useGeminiNano(): Boolean {
        return remoteConfig.getBoolean("use_gemini_nano")
    }

    override fun enableAdvancedAnalytics(): Boolean {
        return remoteConfig.getBoolean("enable_advanced_analytics")
    }

    override fun getNutritionTipsGifLink(): String {
        return remoteConfig.getString("nutrition_tips_gif_link")
    }
}
