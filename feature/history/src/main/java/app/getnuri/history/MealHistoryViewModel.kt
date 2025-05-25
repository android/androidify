package app.getnuri.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.getnuri.data.MealDao
import app.getnuri.data.UserFeedbackDao
import app.getnuri.history.model.MealWithFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MealHistoryViewModel @Inject constructor(
    private val mealDao: MealDao,
    private val userFeedbackDao: UserFeedbackDao
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val mealHistory: StateFlow<List<MealWithFeedback>> =
        mealDao.getAllMeals() // Assuming this returns Flow<List<Meal>> sorted by timestamp DESC
            .flatMapLatest { meals ->
                if (meals.isEmpty()) {
                    // Return mock data when database is empty
                    val mockMeals = createMockMeals()
                    val mockFeedback = createMockFeedback()
                    val flows: List<Flow<MealWithFeedback>> = mockMeals.map { meal ->
                        val relatedFeedback = mockFeedback.filter { it.mealId == meal.id }
                        flowOf(MealWithFeedback(meal, relatedFeedback))
                    }
                    combine(flows) { arrayOfMealWithFeedback ->
                        arrayOfMealWithFeedback.toList()
                    }
                } else {
                    // Ensure meals are sorted if not already by DAO (important for consistent display)
                    // val sortedMeals = meals.sortedByDescending { it.timestamp }
                    // Using 'meals' directly assuming DAO sorts it.

                    val flows: List<Flow<MealWithFeedback>> = meals.map { meal ->
                        userFeedbackDao.getFeedbackForMeal(meal.id)
                            .map { feedbackList ->
                                MealWithFeedback(meal, feedbackList)
                            }
                    }
                    // The combine operator will emit a new list whenever any of the inner flows emit.
                    combine(flows) { arrayOfMealWithFeedback ->
                        // The result of combine is an Array, convert it to List
                        arrayOfMealWithFeedback.toList()
                            // Since getAllMeals is sorted by timestamp desc, and we map in that order,
                            // the combined list should also maintain this order.
                    }
                }
            }
            .distinctUntilChanged() // Only emit when the list content actually changes
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    
    private fun createMockMeals(): List<app.getnuri.data.Meal> {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneHourMs = 60 * 60 * 1000L
        
        return listOf(
            // Today
            app.getnuri.data.Meal(
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
            app.getnuri.data.Meal(
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
            app.getnuri.data.Meal(
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
            app.getnuri.data.Meal(
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
            app.getnuri.data.Meal(
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
            app.getnuri.data.Meal(
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
    
    private fun createMockFeedback(): List<app.getnuri.data.UserFeedback> {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneHourMs = 60 * 60 * 1000L
        
        return listOf(
            // Feedback for today's meals
            app.getnuri.data.UserFeedback(
                id = 1,
                mealId = 1, // Grilled chicken meal
                feedbackTimestamp = now - (1 * oneHourMs), // 1 hour after meal
                feelingDescription = "Great",
                customFeeling = null,
                feedbackNotes = "Feeling energized and satisfied. No digestive issues."
            ),
            app.getnuri.data.UserFeedback(
                id = 2,
                mealId = 2, // Greek yogurt meal
                feedbackTimestamp = now - (3 * oneHourMs), // 2 hours after meal
                feelingDescription = "Good",
                customFeeling = null,
                feedbackNotes = "Light and refreshing breakfast. Perfect start to the day."
            ),
            
            // Feedback for yesterday's meals
            app.getnuri.data.UserFeedback(
                id = 3,
                mealId = 3, // Salmon meal
                feedbackTimestamp = now - oneDayMs - (2 * oneHourMs), // 1 hour after meal
                feelingDescription = "Excellent",
                customFeeling = null,
                feedbackNotes = "This was amazing! Felt full but not heavy. Great energy."
            ),
            app.getnuri.data.UserFeedback(
                id = 4,
                mealId = 4, // Avocado toast
                feedbackTimestamp = now - oneDayMs - (6 * oneHourMs), // 2 hours after meal
                feelingDescription = "Bloated",
                customFeeling = null,
                feedbackNotes = "Started feeling uncomfortable about an hour after eating. Might be the bread."
            ),
            
            // Feedback for day before yesterday
            app.getnuri.data.UserFeedback(
                id = 5,
                mealId = 5, // Beef stir-fry
                feedbackTimestamp = now - (2 * oneDayMs) + (1 * oneHourMs), // 2 hours after meal
                feelingDescription = "Uncomfortable",
                customFeeling = "Gassy",
                feedbackNotes = "Stomach felt upset and gassy. Think it was the onions - they always bother me."
            ),
            app.getnuri.data.UserFeedback(
                id = 6,
                mealId = 6, // Mediterranean bowl
                feedbackTimestamp = now - (2 * oneDayMs) - (4 * oneHourMs), // 2 hours after meal
                feelingDescription = "Tired",
                customFeeling = "Sluggish",
                feedbackNotes = "Felt really full and sluggish afterwards. Takes forever to digest these heavy meals."
            )
        )
    }
}
