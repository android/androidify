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

import org.junit.Test
import kotlin.test.assertTrue

class WellbeingDataTest {

    @Test
    fun `generateMockData creates valid wellbeing data`() {
        val data = MockWellbeingDataGenerator.generateMockData()
        
        // Verify mood entries
        assertTrue(data.moodEntries.isNotEmpty(), "Mood entries should not be empty")
        assertTrue(data.moodEntries.size >= 28, "Should have at least 28 days of mood data")
        data.moodEntries.forEach { entry ->
            assertTrue(entry.mood in 1.0f..10.0f, "Mood should be between 1.0 and 10.0")
        }
        
        // Verify energy entries
        assertTrue(data.energyEntries.isNotEmpty(), "Energy entries should not be empty")
        assertTrue(data.energyEntries.size >= 84, "Should have at least 84 energy entries (3 per day for 28 days)")
        data.energyEntries.forEach { entry ->
            assertTrue(entry.energy in 1.0f..10.0f, "Energy should be between 1.0 and 10.0")
            assertTrue(entry.timeOfDay in listOf("morning", "afternoon", "evening"), "Time of day should be valid")
        }
        
        // Verify symptom entries
        data.symptomEntries.forEach { entry ->
            assertTrue(entry.intensity in 1.0f..10.0f, "Symptom intensity should be between 1.0 and 10.0")
            assertTrue(entry.duration > 0, "Symptom duration should be positive")
        }
    }
} 