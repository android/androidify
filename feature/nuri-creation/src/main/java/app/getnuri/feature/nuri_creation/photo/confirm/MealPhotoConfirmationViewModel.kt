package app.getnuri.feature.nuri_creation.photo.confirm

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.MealDao
import app.getnuri.data.NuriMealAnalyzer
import app.getnuri.data.Meal
import app.getnuri.data.model.MealAnalysisData
import androidx.work.Data as WorkManagerData // Aliasing to avoid name clash
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.getnuri.background.FeedbackReminderWorker
import app.getnuri.background.MEAL_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Extended UiState to hold imageUri and handle saving states
sealed interface MealPhotoConfirmUiState {
    data class Idle(val imageUri: Uri? = null) : MealPhotoConfirmUiState
    data class Loading(val imageUri: Uri) : MealPhotoConfirmUiState
    data class Success(val imageUri: Uri, val analysisData: MealAnalysisData) : MealPhotoConfirmUiState
    data class Error(val imageUri: Uri?, val message: String) : MealPhotoConfirmUiState
    data class Saving(val imageUri: Uri, val analysisData: MealAnalysisData) : MealPhotoConfirmUiState
    data object Saved : MealPhotoConfirmUiState
}

@HiltViewModel
class MealPhotoConfirmationViewModel @Inject constructor(
    private val nuriMealAnalyzer: NuriMealAnalyzer,
    private val mealDao: MealDao,
    private val workManager: WorkManager // Inject WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MealPhotoConfirmUiState>(MealPhotoConfirmUiState.Idle())
    val uiState: StateFlow<MealPhotoConfirmUiState> = _uiState.asStateFlow()

    fun analyzeMealPhoto(uri: Uri) {
        // Store the URI when analysis starts
        _uiState.update { MealPhotoConfirmUiState.Loading(uri) }
        viewModelScope.launch {
            Log.d("MealPhotoConfirmVM", "Analyzing photo: $uri")
            try {
                val result = nuriMealAnalyzer.analyzeMealFromImage(uri)
                result.fold(
                    onSuccess = { data ->
                        Log.d("MealPhotoConfirmVM", "Analysis success: $data")
                        _uiState.update { MealPhotoConfirmUiState.Success(uri, data) }
                    },
                    onFailure = { throwable ->
                        Log.e("MealPhotoConfirmVM", "Analysis failure", throwable)
                        _uiState.update { MealPhotoConfirmUiState.Error(uri, throwable.message ?: "Unknown error") }
                    }
                )
            } catch (e: Exception) {
                Log.e("MealPhotoConfirmVM", "Exception during analysis call", e)
                _uiState.update { MealPhotoConfirmUiState.Error(uri, e.message ?: "Exception occurred") }
            }
        }
    }

    fun saveMeal() {
        val currentState = _uiState.value
        if (currentState is MealPhotoConfirmUiState.Success) {
            _uiState.update { MealPhotoConfirmUiState.Saving(currentState.imageUri, currentState.analysisData) }
            viewModelScope.launch {
                try {
                    val meal = Meal(
                        id = 0,
                        timestamp = System.currentTimeMillis(),
                        inputType = "PHOTO",
                        photoUri = currentState.imageUri.toString(),
                        description = null, // No text description for photo input type initially
                        rawExtractedIngredients = currentState.analysisData.extractedIngredients,
                        rawExtractedTriggers = currentState.analysisData.potentialTriggers,
                        userConfirmedIngredients = currentState.analysisData.extractedIngredients, // Same as raw for now
                        userConfirmedTriggers = currentState.analysisData.potentialTriggers,
                        notes = null
                    )
                    val mealId = mealDao.insertMeal(meal) // Get the returned ID
                    _uiState.update { MealPhotoConfirmUiState.Saved }
                    Log.d("MealPhotoConfirmVM", "Meal saved successfully with ID: $mealId")

                    // Schedule reminder worker
                    scheduleFeedbackReminder(mealId)

                } catch (e: Exception) {
                    Log.e("MealPhotoConfirmVM", "Failed to save meal or schedule worker", e)
                    _uiState.update {
                        MealPhotoConfirmUiState.Error(
                            currentState.imageUri,
                            "Failed to save meal: ${e.message}"
                        )
                    }
                }
            }
        } else {
            Log.w("MealPhotoConfirmVM", "SaveMeal called in an invalid state: $currentState")
        }
    }

    private fun scheduleFeedbackReminder(mealId: Long) {
        val workData = WorkManagerData.Builder()
            .putLong(MEAL_ID_KEY, mealId)
            .build()

        val feedbackWorkRequest = OneTimeWorkRequestBuilder<FeedbackReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(30, TimeUnit.MINUTES) // 30 minutes
            .addTag("feedback-reminder-photo-$mealId") // Unique tag
            .build()

        workManager.enqueueUniqueWork(
            "feedback-reminder-for-meal-$mealId", // Unique work name
            ExistingWorkPolicy.REPLACE,
            feedbackWorkRequest
        )
        Log.d("MealPhotoConfirmVM", "Scheduled feedback reminder for meal ID: $mealId")
    }

    fun resetState(initialUri: Uri? = null) {
        _uiState.update { MealPhotoConfirmUiState.Idle(initialUri) }
    }

    fun setImageUri(uri: Uri) {
        _uiState.update {
            if (it is MealPhotoConfirmUiState.Idle) {
                MealPhotoConfirmUiState.Idle(uri)
            } else {
                MealPhotoConfirmUiState.Idle(uri)
            }
        }
    }
}
