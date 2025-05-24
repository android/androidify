package app.getnuri.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Meal::class, UserFeedback::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class NuriDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun userFeedbackDao(): UserFeedbackDao
}
