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
            if (meals.isEmpty() && allFeedback.isEmpty()) {
                // Return mock data when database is empty
                createTimelineGroups(createMockMeals(), createMockFeedback())
            } else {
                createTimelineGroups(meals, allFeedback)
            }
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
    
    private fun createMockMeals(): List<Meal> {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneHourMs = 60 * 60 * 1000L
        
        return listOf(
            // Today
            Meal(
                id = 1,
                timestamp = now - (2 * oneHourMs), // 2 hours ago
                inputType = "PHOTO",
                photoUri = "content://media/external/images/media/123",
                rawExtractedIngredients = listOf("Grilled chicken | 150g", "Brown rice | 1 cup", "Steamed broccoli | 1 cup", "Olive oil | 1 tsp"),
                rawExtractedTriggers = listOf("Garlic | High FODMAP"),
                userConfirmedIngredients = listOf("Grilled chicken | 150g", "Brown rice | 1 cup", "Steamed broccoli | 1 cup"),
                userConfirmedTriggers = listOf("Garlic | High FODMAP"),
                notes = "Feeling good about this healthy choice!"
            ),
            Meal(
                id = 2,
                timestamp = now - (5 * oneHourMs), // 5 hours ago
                inputType = "TEXT",
                description = "Greek yogurt with honey, granola, and fresh berries",
                rawExtractedIngredients = listOf("Greek yogurt | 1 cup", "Honey | 2 tbsp", "Granola | 1/4 cup", "Mixed berries | 1/2 cup"),
                rawExtractedTriggers = emptyList(),
                userConfirmedIngredients = listOf("Greek yogurt | 1 cup", "Honey | 2 tbsp", "Granola | 1/4 cup", "Mixed berries | 1/2 cup"),
                userConfirmedTriggers = emptyList(),
                notes = null
            ),
            
            // Yesterday
            Meal(
                id = 3,
                timestamp = now - oneDayMs - (3 * oneHourMs), // Yesterday dinner
                inputType = "PHOTO",
                photoUri = "content://media/external/images/media/124", 
                rawExtractedIngredients = listOf("Salmon fillet | 180g", "Quinoa | 1 cup", "Asparagus | 8 spears", "Lemon | 1 wedge"),
                rawExtractedTriggers = emptyList(),
                userConfirmedIngredients = listOf("Salmon fillet | 180g", "Quinoa | 1 cup", "Asparagus | 8 spears"),
                userConfirmedTriggers = emptyList(),
                notes = "Perfectly cooked salmon"
            ),
            Meal(
                id = 4,
                timestamp = now - oneDayMs - (8 * oneHourMs), // Yesterday lunch
                inputType = "TEXT",
                description = "Avocado toast with tomatoes and a side salad",
                rawExtractedIngredients = listOf("Sourdough bread | 2 slices", "Avocado | 1 whole", "Cherry tomatoes | 1/2 cup", "Mixed greens | 2 cups", "Balsamic vinegar | 1 tbsp"),
                rawExtractedTriggers = listOf("Wheat | Gluten"),
                userConfirmedIngredients = listOf("Sourdough bread | 2 slices", "Avocado | 1 whole", "Cherry tomatoes | 1/2 cup", "Mixed greens | 2 cups"),
                userConfirmedTriggers = listOf("Wheat | Gluten"),
                notes = "Noticed some bloating after - might be the bread"
            ),
            
            // Day before yesterday
            Meal(
                id = 5,
                timestamp = now - (2 * oneDayMs) - (1 * oneHourMs), // 2 days ago dinner
                inputType = "PHOTO",
                photoUri = "content://media/external/images/media/125",
                rawExtractedIngredients = listOf("Beef stir-fry | 200g", "Bell peppers | 1 cup", "Onions | 1/2 cup", "Soy sauce | 2 tbsp", "Jasmine rice | 1 cup"),
                rawExtractedTriggers = listOf("Onions | High FODMAP", "Soy sauce | High sodium"),
                userConfirmedIngredients = listOf("Beef stir-fry | 200g", "Bell peppers | 1 cup", "Onions | 1/2 cup", "Jasmine rice | 1 cup"),
                userConfirmedTriggers = listOf("Onions | High FODMAP"),
                notes = "Delicious but noticed stomach upset later"
            ),
            Meal(
                id = 6,
                timestamp = now - (2 * oneDayMs) - (6 * oneHourMs), // 2 days ago lunch
                inputType = "TEXT",
                description = "Mediterranean bowl with hummus, falafel, and vegetables",
                rawExtractedIngredients = listOf("Hummus | 1/4 cup", "Falafel | 4 pieces", "Cucumber | 1/2 cup", "Red onion | 2 tbsp", "Pita bread | 1 piece", "Tahini | 1 tbsp"),
                rawExtractedTriggers = listOf("Chickpeas | High FODMAP", "Wheat | Gluten", "Red onion | High FODMAP"),
                userConfirmedIngredients = listOf("Hummus | 1/4 cup", "Falafel | 4 pieces", "Cucumber | 1/2 cup", "Pita bread | 1 piece"),
                userConfirmedTriggers = listOf("Chickpeas | High FODMAP", "Wheat | Gluten"),
                notes = "Love Mediterranean food but always makes me feel heavy"
            )
        )
    }
    
    private fun createMockFeedback(): List<UserFeedback> {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneHourMs = 60 * 60 * 1000L
        
        return listOf(
            // Feedback for today's meals
            UserFeedback(
                id = 1,
                mealId = 1, // Grilled chicken meal
                feedbackTimestamp = now - (1 * oneHourMs), // 1 hour after meal
                feelingDescription = "Great",
                customFeeling = null,
                feedbackNotes = "Feeling energized and satisfied. No digestive issues."
            ),
            UserFeedback(
                id = 2,
                mealId = 2, // Greek yogurt meal
                feedbackTimestamp = now - (3 * oneHourMs), // 2 hours after meal
                feelingDescription = "Good",
                customFeeling = null,
                feedbackNotes = "Light and refreshing breakfast. Perfect start to the day."
            ),
            
            // Feedback for yesterday's meals
            UserFeedback(
                id = 3,
                mealId = 3, // Salmon meal
                feedbackTimestamp = now - oneDayMs - (2 * oneHourMs), // 1 hour after meal
                feelingDescription = "Excellent",
                customFeeling = null,
                feedbackNotes = "This was amazing! Felt full but not heavy. Great energy."
            ),
            UserFeedback(
                id = 4,
                mealId = 4, // Avocado toast
                feedbackTimestamp = now - oneDayMs - (6 * oneHourMs), // 2 hours after meal
                feelingDescription = "Bloated",
                customFeeling = null,
                feedbackNotes = "Started feeling uncomfortable about an hour after eating. Might be the bread."
            ),
            
            // Feedback for day before yesterday
            UserFeedback(
                id = 5,
                mealId = 5, // Beef stir-fry
                feedbackTimestamp = now - (2 * oneDayMs) + (1 * oneHourMs), // 2 hours after meal
                feelingDescription = "Uncomfortable",
                customFeeling = "Gassy",
                feedbackNotes = "Stomach felt upset and gassy. Think it was the onions - they always bother me."
            ),
            UserFeedback(
                id = 6,
                mealId = 6, // Mediterranean bowl
                feedbackTimestamp = now - (2 * oneDayMs) - (4 * oneHourMs), // 2 hours after meal
                feelingDescription = "Tired",
                customFeeling = "Sluggish",
                feedbackNotes = "Felt really full and sluggish afterwards. Takes forever to digest these heavy meals."
            ),
            
            // Additional standalone wellbeing entries
            UserFeedback(
                id = 7,
                mealId = 0, // No related meal - general wellbeing check
                feedbackTimestamp = now - (12 * oneHourMs), // This morning
                feelingDescription = "Good",
                customFeeling = null,
                feedbackNotes = "Woke up feeling good today. Energy levels are stable."
            )
        )
    }
} 