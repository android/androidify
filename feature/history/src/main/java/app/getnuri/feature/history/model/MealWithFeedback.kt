package app.getnuri.feature.history.model

import app.getnuri.data.Meal
import app.getnuri.data.UserFeedback

data class MealWithFeedback(
    val meal: Meal,
    val feedback: List<UserFeedback>
)
