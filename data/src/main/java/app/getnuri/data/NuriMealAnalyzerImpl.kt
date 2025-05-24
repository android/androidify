package app.getnuri.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import app.getnuri.data.model.MealAnalysisData
import app.getnuri.util.LocalFileProvider
import app.getnuri.vertexai.FirebaseAiDataSource // Corrected import path
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NuriMealAnalyzerImpl @Inject constructor(
    private val firebaseAiDataSource: FirebaseAiDataSource,
    private val localFileProvider: LocalFileProvider,
    @ApplicationContext private val context: Context
) : NuriMealAnalyzer {

    override suspend fun analyzeMealFromText(description: String): Result<MealAnalysisData> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Craft the prompt for Gemini
                val prompt = """
                Analyze the following meal description to identify key ingredients and potential common dietary triggers (like gluten, dairy, lactose, nuts, soy, eggs).
                Meal Description: "$description"
                Return the ingredients as a comma-separated list under a heading "Ingredients:" and triggers as a comma-separated list under a heading "Triggers:".
                Provide ONLY the Ingredients and Triggers sections. Do not include any other conversational text or explanations.
                Example:
                Ingredients: chicken, rice, broccoli, soy sauce
                Triggers: soy, gluten
                """

                // 2. Call FirebaseAiDataSource
                val aiResponse = firebaseAiDataSource.generatePrompt(prompt)
                val responseText = aiResponse.generatedPrompts?.firstOrNull()

                if (responseText.isNullOrBlank()) {
                    Result.failure(Exception("AI response was empty or null for text analysis."))
                } else {
                    // 3. Parse the responseText
                    val ingredients = parseList(responseText, "Ingredients:")
                    val triggers = parseList(responseText, "Triggers:")
                    Result.success(MealAnalysisData(ingredients, triggers))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed during text analysis: ${e.message}", e))
            }
        }
    }

    override suspend fun analyzeMealFromImage(imageUri: Uri): Result<MealAnalysisData> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Convert Uri to File, then to Bitmap
                val imageFile: File? = try {
                    if (imageUri.scheme == "file") {
                        imageUri.path?.let { File(it) }
                    } else {
                        // If it's a content URI, copy to a temporary cache file
                        val tempFile = localFileProvider.createCacheFile("temp_image_for_analysis.jpg")
                        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                            tempFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        tempFile
                    }
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Failed to access image file from URI: ${e.message}", e))
                }

                if (imageFile == null || !imageFile.exists()) {
                     return@withContext Result.failure(Exception("Image file not found or not accessible from URI."))
                }
                
                val bitmap: Bitmap? = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap == null) {
                    // Attempt to delete the temp file if it was created by localFileProvider
                    if (imageUri.scheme != "file" && imageFile.name.startsWith("temp_image_for_analysis")) {
                        imageFile.delete()
                    }
                    return@withContext Result.failure(Exception("Failed to decode bitmap from image file."))
                }

                // 2. Get image description from AI
                val imageDescResponse = firebaseAiDataSource.generateDescriptivePromptFromImage(bitmap)
                val description = imageDescResponse.userDescription 

                // Clean up the temp file if it was created
                if (imageUri.scheme != "file" && imageFile.name.startsWith("temp_image_for_analysis")) {
                    imageFile.delete()
                }
                
                if (!imageDescResponse.success || description.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Failed to get description from image. AI indicated failure or empty description."))
                }

                // 3. Analyze the generated description for ingredients and triggers (reuse text analysis logic)
                // Pass the description to the text analysis function.
                // The result of this call (which is Result<MealAnalysisData>) is returned directly.
                analyzeMealFromText(description)
            } catch (e: Exception) {
                Result.failure(Exception("Failed during image analysis: ${e.message}", e))
            }
        }
    }

    private fun parseList(text: String, heading: String): List<String> {
        return text.lines()
            .find { it.trim().startsWith(heading, ignoreCase = true) }
            ?.substringAfter(heading)
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }
}
