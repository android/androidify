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
package app.getnuri.vertexai

import android.graphics.Bitmap
import app.getnuri.RemoteConfigDataSource
import app.getnuri.model.GeneratedPrompt
import app.getnuri.model.ImageValidationError
import app.getnuri.model.ValidatedDescription
import app.getnuri.model.ValidatedImage
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

interface FirebaseAiDataSource {
    // Nutrition-focused methods
    suspend fun validateMealPhoto(image: Bitmap): ValidatedImage
    suspend fun analyzeMealFromImage(image: Bitmap): ValidatedDescription
    suspend fun processSymptomInput(inputPrompt: String): ValidatedDescription
    suspend fun generateNutritionPrompt(prompt: String): GeneratedPrompt
    
    // Legacy methods for backward compatibility
    @Deprecated("Use validateMealPhoto() instead")
    suspend fun validatePromptHasEnoughInformation(inputPrompt: String): ValidatedDescription
    @Deprecated("Use validateMealPhoto() instead") 
    suspend fun validateImageHasEnoughInformation(image: Bitmap): ValidatedImage
    @Deprecated("Use analyzeMealFromImage() instead")
    suspend fun generateDescriptivePromptFromImage(image: Bitmap): ValidatedDescription
    @Deprecated("Not applicable for nutrition tracking")
    suspend fun generateImageFromPromptAndSkinTone(prompt: String, skinTone: String): Bitmap
    @Deprecated("Use generateNutritionPrompt() instead")
    suspend fun generatePrompt(prompt: String): GeneratedPrompt
}

@OptIn(PublicPreviewAPI::class)
@Singleton
class FirebaseAiDataSourceImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
) : FirebaseAiDataSource {
    private fun createGenerativeTextModel(jsonSchema: Schema, temperature: Float? = null): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel(
            modelName = remoteConfigDataSource.textModelName(),
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = jsonSchema
                this.temperature = temperature
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.CIVIC_INTEGRITY, HarmBlockThreshold.LOW_AND_ABOVE),
            ),
        )
    }

    private fun createGenerativeImageModel(): ImagenModel {
        return Firebase.ai(backend = GenerativeBackend.vertexAI()).imagenModel(
            remoteConfigDataSource.imageModelName(),
            safetySettings =
            ImagenSafetySettings(
                ImagenSafetyFilterLevel.BLOCK_LOW_AND_ABOVE,
                personFilterLevel = ImagenPersonFilterLevel.ALLOW_ALL,
            ),
        )
    }

    // New nutrition-focused methods
    override suspend fun validateMealPhoto(image: Bitmap): ValidatedImage {
        val jsonSchema = Schema.obj(
            properties = mapOf(
                "success" to Schema.boolean(),
                "error" to Schema.enumeration(
                    values = ImageValidationError.entries.map { it.description },
                    description = "Error message",
                    nullable = true,
                ),
            ),
            optionalProperties = listOf("error"),
        )
        val generativeModel = createGenerativeTextModel(jsonSchema)

        return executeImageValidation(
            generativeModel,
            remoteConfigDataSource.promptMealPhotoValidation(),
            image,
        )
    }

    override suspend fun analyzeMealFromImage(image: Bitmap): ValidatedDescription {
        val jsonSchema = Schema.obj(
            properties = mapOf(
                "success" to Schema.boolean(),
                "user_description" to Schema.string(),
            ),
            optionalProperties = listOf("user_description"),
        )
        val generativeModel = createGenerativeTextModel(jsonSchema)

        return executeImageDescriptionGeneration(
            generativeModel,
            remoteConfigDataSource.promptIngredientExtraction(),
            image,
        )
    }

    override suspend fun processSymptomInput(inputPrompt: String): ValidatedDescription {
        val jsonSchema = Schema.obj(
            mapOf("success" to Schema.boolean(), "user_description" to Schema.string()),
            optionalProperties = listOf("user_description"),
        )
        val generativeModel = createGenerativeTextModel(jsonSchema)

        return executeTextValidation(
            generativeModel,
            "${remoteConfigDataSource.promptSymptomProcessing()}. The input is as follows: `$inputPrompt`.",
        )
    }

    override suspend fun generateNutritionPrompt(prompt: String): GeneratedPrompt {
        val jsonSchema = Schema.obj(
            properties = mapOf(
                "success" to Schema.boolean(),
                "generated_prompt" to Schema.array(Schema.string()),
            ),
            optionalProperties = listOf("generated_prompt"),
        )
        val generativeModel = createGenerativeTextModel(jsonSchema, temperature = 0.75f)
        return executePromptGeneration(generativeModel, prompt)
    }

    // Legacy methods for backward compatibility
    override suspend fun validatePromptHasEnoughInformation(inputPrompt: String): ValidatedDescription {
        return processSymptomInput(inputPrompt)
    }

    override suspend fun validateImageHasEnoughInformation(image: Bitmap): ValidatedImage {
        return validateMealPhoto(image)
    }

    override suspend fun generateDescriptivePromptFromImage(image: Bitmap): ValidatedDescription {
        return analyzeMealFromImage(image)
    }

    override suspend fun generateImageFromPromptAndSkinTone(prompt: String, skinTone: String): Bitmap {
        // Not applicable for nutrition tracking - return a placeholder or throw
        throw UnsupportedOperationException("Image generation not supported in nutrition tracking mode")
    }

    override suspend fun generatePrompt(prompt: String): GeneratedPrompt {
        return generateNutritionPrompt(prompt)
    }

    // Private helper methods remain the same
    private suspend fun executeTextValidation(
        generativeModel: GenerativeModel,
        prompt: String,
    ): ValidatedDescription {
        val response = generativeModel.generateContent(prompt)
        val jsonResponse = Json.parseToJsonElement(response.text!!)
        val isSuccess = jsonResponse.jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true
        val userDescription = jsonResponse.jsonObject["user_description"]?.jsonPrimitive?.content
        return ValidatedDescription(isSuccess, userDescription)
    }

    private suspend fun executeImageValidation(
        generativeModel: GenerativeModel,
        prompt: String,
        image: Bitmap,
    ): ValidatedImage {
        val response = generativeModel.generateContent(
            content {
                text(prompt)
                image(image)
            },
        )
        val jsonResponse = Json.parseToJsonElement(response.text!!)
        val isSuccess = jsonResponse.jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true
        val error = jsonResponse.jsonObject["error"]?.jsonPrimitive?.content
        val errorEnum = ImageValidationError.entries.find { it.description == error }
        return ValidatedImage(isSuccess, errorEnum)
    }

    private suspend fun executeImageDescriptionGeneration(
        generativeModel: GenerativeModel,
        prompt: String,
        image: Bitmap,
    ): ValidatedDescription {
        val response = generativeModel.generateContent(
            content {
                text(prompt)
                image(image)
            },
        )
        val jsonResponse = Json.parseToJsonElement(response.text!!)
        val isSuccess = jsonResponse.jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true
        val userDescription = jsonResponse.jsonObject["user_description"]?.jsonPrimitive?.content
        return ValidatedDescription(isSuccess, userDescription)
    }

    private suspend fun executeImageGeneration(
        generativeModel: ImagenModel,
        prompt: String,
    ): Bitmap {
        val response = generativeModel.generateImages(prompt)
        return response.images.first().asBitmap()
    }

    private suspend fun executePromptGeneration(
        generativeModel: GenerativeModel,
        prompt: String,
    ): GeneratedPrompt {
        val response = generativeModel.generateContent(
            content {
                text(prompt)
            },
        )
        val jsonResponse = Json.parseToJsonElement(response.text!!)
        val isSuccess = jsonResponse.jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true
        val content = jsonResponse.jsonObject["generated_prompt"]
        val generatedPrompts = if (content != null) {
            Json.decodeFromJsonElement<List<String>>(content)
        } else {
            null
        }
        return GeneratedPrompt(isSuccess, generatedPrompts)
    }
}
