package app.getnuri.history.model

import app.getnuri.data.Meal
import app.getnuri.data.UserFeedback
import java.time.LocalDate

/**
 * Unified timeline entry that can represent either a meal or wellbeing log
 */
sealed class TimelineEntry {
    abstract val timestamp: Long
    abstract val date: LocalDate
    abstract val summary: String
    
    data class MealEntry(
        val meal: Meal,
        override val timestamp: Long,
        override val date: LocalDate,
        override val summary: String
    ) : TimelineEntry()
    
    data class WellbeingEntry(
        val feedback: UserFeedback,
        val relatedMeal: Meal?, // Optional reference to the meal this feedback is about
        override val timestamp: Long,
        override val date: LocalDate,
        override val summary: String
    ) : TimelineEntry()
}

/**
 * Groups timeline entries by calendar day with display formatting
 */
data class DayGroup(
    val date: LocalDate,
    val displayName: String, // "Today", "Yesterday", "Monday, January 15"
    val entries: List<TimelineEntry>
)

/**
 * Helper to determine relative day names
 */
fun LocalDate.toDisplayName(): String {
    val today = LocalDate.now()
    return when {
        this == today -> "Today"
        this == today.minusDays(1) -> "Yesterday"
        else -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")
            this.format(formatter)
        }
    }
} 