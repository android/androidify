package app.getnuri.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // milliseconds since epoch
    val inputType: String, // "PHOTO", "TEXT"
    val photoUri: String? = null,
    val description: String? = null,
    val rawExtractedIngredients: List<String>,
    val rawExtractedTriggers: List<String>,
    val userConfirmedIngredients: List<String>,
    val userConfirmedTriggers: List<String>,
    val notes: String? = null
)

@Entity(
    tableName = "user_feedback",
    foreignKeys = [
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserFeedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val mealId: Long,
    val feedbackTimestamp: Long,
    val feelingDescription: String, // e.g., "Good", "Bloated", "Energized"
    val customFeeling: String? = null,
    val feedbackNotes: String? = null
)
