package app.getnuri.data

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class StringListConverter @Inject constructor() { // Added @Inject for Hilt
    @TypeConverter
    fun fromString(value: String): List<String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return Json.encodeToString(list)
    }
}
