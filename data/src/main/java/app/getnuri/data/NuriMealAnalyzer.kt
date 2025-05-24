package app.getnuri.data

import android.net.Uri
import app.getnuri.data.model.MealAnalysisData

interface NuriMealAnalyzer {
    suspend fun analyzeMealFromText(description: String): Result<MealAnalysisData>
    suspend fun analyzeMealFromImage(imageUri: Uri): Result<MealAnalysisData>
}
