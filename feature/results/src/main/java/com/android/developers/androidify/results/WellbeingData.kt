/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.getnuri.results

import java.time.LocalDate
import kotlin.random.Random

data class MoodEntry(
    val date: LocalDate,
    val mood: Float, // 1.0 to 10.0
    val note: String? = null
)

data class EnergyEntry(
    val date: LocalDate,
    val energy: Float, // 1.0 to 10.0
    val timeOfDay: String // "morning", "afternoon", "evening"
)

data class SymptomEntry(
    val date: LocalDate,
    val symptom: String,
    val intensity: Float, // 1.0 to 10.0
    val duration: Int // minutes
)

data class WellbeingData(
    val moodEntries: List<MoodEntry>,
    val energyEntries: List<EnergyEntry>,
    val symptomEntries: List<SymptomEntry>
)

object MockWellbeingDataGenerator {
    
    private val symptoms = listOf(
        "Headache", "Fatigue", "Anxiety", "Stress", "Back Pain", 
        "Sleep Issues", "Digestive Issues", "Joint Pain"
    )
    
    private val moodNotes = listOf(
        "Feeling great today!", "Had a productive morning", "Feeling a bit down",
        "Excited about weekend plans", "Work stress getting to me", "Beautiful weather lifted my spirits",
        "Good workout session", "Family time was wonderful"
    )
    
    fun generateMockData(): WellbeingData {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(28) // 4 weeks
        
        val moodEntries = mutableListOf<MoodEntry>()
        val energyEntries = mutableListOf<EnergyEntry>()
        val symptomEntries = mutableListOf<SymptomEntry>()
        
        // Generate mood data (daily entries)
        var currentDate = startDate
        var previousMood = 6.0f // Start with neutral mood
        
        while (!currentDate.isAfter(endDate)) {
            // Create realistic mood progression with some randomness
            val moodChange = Random.nextFloat() * 2f - 1f // -1 to 1
            val newMood = (previousMood + moodChange).coerceIn(1.0f, 10.0f)
            
            moodEntries.add(
                MoodEntry(
                    date = currentDate,
                    mood = newMood,
                    note = if (Random.nextFloat() < 0.3) moodNotes.random() else null
                )
            )
            
            previousMood = newMood
            currentDate = currentDate.plusDays(1)
        }
        
        // Generate energy data (3 times per day)
        currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val timeSlots = listOf("morning", "afternoon", "evening")
            val baseEnergy = Random.nextFloat() * 3f + 5f // 5-8 base range
            
            timeSlots.forEach { timeSlot ->
                val energyVariation = when (timeSlot) {
                    "morning" -> Random.nextFloat() * 2f + 1f // Higher in morning
                    "afternoon" -> Random.nextFloat() * 1.5f // Moderate
                    "evening" -> Random.nextFloat() * 1f - 0.5f // Lower in evening
                    else -> 0f
                }
                
                energyEntries.add(
                    EnergyEntry(
                        date = currentDate,
                        energy = (baseEnergy + energyVariation).coerceIn(1.0f, 10.0f),
                        timeOfDay = timeSlot
                    )
                )
            }
            currentDate = currentDate.plusDays(1)
        }
        
        // Generate symptom data (random occurrences)
        currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            // 40% chance of having symptoms on any given day
            if (Random.nextFloat() < 0.4) {
                val numSymptoms = Random.nextInt(1, 3) // 1-2 symptoms per day
                val selectedSymptoms = symptoms.shuffled().take(numSymptoms)
                
                selectedSymptoms.forEach { symptom ->
                    symptomEntries.add(
                        SymptomEntry(
                            date = currentDate,
                            symptom = symptom,
                            intensity = Random.nextFloat() * 7f + 1f, // 1-8 intensity
                            duration = Random.nextInt(15, 240) // 15 minutes to 4 hours
                        )
                    )
                }
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return WellbeingData(
            moodEntries = moodEntries,
            energyEntries = energyEntries,
            symptomEntries = symptomEntries
        )
    }
} 