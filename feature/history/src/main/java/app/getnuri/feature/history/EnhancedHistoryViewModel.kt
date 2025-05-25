package app.getnuri.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.MealDao
import app.getnuri.data.UserFeedbackDao
import app.getnuri.data.Meal
import app.getnuri.data.UserFeedback
import app.getnuri.feature.history.model.TimelineEntry
import app.getnuri.feature.history.model.DayGroup
import app.getnuri.feature.history.model.toDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EnhancedHistoryViewModel @Inject constructor(
    private val mealDao: MealDao,
    private val userFeedbackDao: UserFeedbackDao
) : ViewModel() {

    val timelineData: StateFlow<List<DayGroup>> =
        combine(
            mealDao.getAllMeals(),
            userFeedbackDao.getAllFeedback()
        ) { meals, allFeedback ->
            createTimelineGroups(meals, allFeedback)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private fun createTimelineGroups(
        meals: List<Meal>,
        allFeedback: List<UserFeedback>
    ): List<DayGroup> {
        // Create meal entries
        val mealEntries = meals.map { meal ->
            TimelineEntry.MealEntry(
                meal = meal,
                timestamp = meal.timestamp,
                date = timestampToLocalDate(meal.timestamp),
                summary = generateMealSummary(meal)
            )
        }

        // Create wellbeing entries
        val wellbeingEntries = allFeedback.map { feedback ->
            val relatedMeal = meals.find { it.id == feedback.mealId }
            TimelineEntry.WellbeingEntry(
                feedback = feedback,
                relatedMeal = relatedMeal,
                timestamp = feedback.feedbackTimestamp,
                date = timestampToLocalDate(feedback.feedbackTimestamp),
                summary = generateWellbeingSummary(feedback, relatedMeal)
            )
        }

        // Combine and sort by timestamp (most recent first)
        val allEntries = (mealEntries + wellbeingEntries)
            .sortedByDescending { it.timestamp }

        // Group by date
        return allEntries
            .groupBy { it.date }
            .map { (date, entries) ->
                DayGroup(
                    date = date,
                    displayName = date.toDisplayName(),
                    entries = entries.sortedByDescending { it.timestamp }
                )
            }
            .sortedByDescending { it.date }
    }

    private fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun generateMealSummary(meal: Meal): String {
        val mealType = determineMealType(meal.timestamp)
        val ingredientCount = meal.rawExtractedIngredients.size
        
        return when {
            meal.inputType == "PHOTO" && meal.photoUri != null -> {
                val mainIngredient = meal.rawExtractedIngredients.firstOrNull()?.split("|")?.firstOrNull()?.trim()
                when {
                    mainIngredient != null && ingredientCount > 1 -> 
                        "$mealType: $mainIngredient + ${ingredientCount - 1} more"
                    mainIngredient != null -> 
                        "$mealType: $mainIngredient"
                    else -> 
                        "$mealType: Photo meal"
                }
            }
            meal.inputType == "TEXT" && meal.description != null -> {
                val description = meal.description!!
                val truncatedDescription = description.take(30) + if (description.length > 30) "..." else ""
                "$mealType: $truncatedDescription"
            }
            ingredientCount > 0 -> {
                val mainIngredient = meal.rawExtractedIngredients.firstOrNull()?.split("|")?.firstOrNull()?.trim()
                "$mealType: ${mainIngredient ?: "Custom meal"}"
            }
            else -> "$mealType: Meal logged"
        }
    }

    private fun generateWellbeingSummary(feedback: UserFeedback, relatedMeal: Meal?): String {
        val feeling = feedback.feelingDescription
        val customFeeling = feedback.customFeeling
        
        val feelingText = when {
            customFeeling != null -> "$feeling ($customFeeling)"
            else -> feeling
        }
        
        return when {
            relatedMeal != null -> {
                val mealType = determineMealType(relatedMeal.timestamp)
                "Feeling: $feelingText after $mealType"
            }
            else -> "Feeling: $feelingText"
        }
    }

    private fun determineMealType(timestamp: Long): String {
        val hour = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .hour
        
        return when (hour) {
            in 5..10 -> "Breakfast"
            in 11..14 -> "Lunch"
            in 15..17 -> "Snack"
            in 18..22 -> "Dinner"
            else -> "Meal"
        }
    }
} 