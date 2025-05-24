package app.getnuri.feature.nuri_creation.ingredient

import app.getnuri.data.model.MealAnalysisData

data class ExtractedIngredient(
    val name: String,
    val quantity: String,
    val unit: String = "",
    val isEditable: Boolean = true
)

data class MealInfo(
    val title: String,
    val imageUri: String?,
    val scheduledTime: String? = null
)

data class IngredientExtractionUiState(
    val mealInfo: MealInfo,
    val ingredients: List<ExtractedIngredient>,
    val isLoading: Boolean = false,
    val error: String? = null
)

fun MealAnalysisData.toExtractedIngredients(): List<ExtractedIngredient> {
    return extractedIngredients.map { ingredient ->
        // Simple parsing - you might want to make this more sophisticated
        val parts = ingredient.split("|", limit = 2)
        if (parts.size == 2) {
            val name = parts[0].trim()
            val quantityPart = parts[1].trim()
            
            // Extract quantity and unit (e.g., "120g" -> "120" and "g")
            val quantityRegex = Regex("(\\d+\\.?\\d*)\\s*([a-zA-Z]*)")
            val match = quantityRegex.find(quantityPart)
            
            if (match != null) {
                val quantity = match.groupValues[1]
                val unit = match.groupValues[2]
                ExtractedIngredient(name, quantity, unit)
            } else {
                ExtractedIngredient(name, quantityPart, "")
            }
        } else {
            ExtractedIngredient(ingredient, "", "")
        }
    }
} 