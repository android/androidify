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
package app.getnuri.feature.wellbeing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.getnuri.theme.AndroidifyTheme
import app.getnuri.theme.components.AndroidifyTopAppBar



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WellbeingScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = false,
                aboutEnabled = true,
                useNuriStyling = true,
                onAboutClicked = onBackPressed,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        // Display the tracking content directly without tabs
        TrackNowContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackNowContent(modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "How are you feeling after your meal?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            MoodTracker()
        }

        item {
            EnergyTracker()
        }

        item {
            SymptomTracker()
        }

        item {
            NotesSection()
        }

        item {
            Button(
                onClick = { /* Save entry */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Save Entry",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MoodTracker() {
    val moodEmojis = listOf("😔", "😞", "😐", "😊", "😄")
    val moodLabels = listOf("Poor", "Low", "Okay", "Good", "Great")
    var selectedMood by remember { mutableIntStateOf(-1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Mood",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                moodEmojis.forEachIndexed { index, emoji ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilterChip(
                            onClick = { selectedMood = index },
                            label = { 
                                Text(
                                    emoji,
                                    fontSize = 24.sp
                                )
                            },
                            selected = selectedMood == index,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            moodLabels[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedMood == index) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnergyTracker() {
    val energyIcons = listOf(
        Icons.Filled.BatteryAlert,
        Icons.Filled.Battery1Bar,
        Icons.Filled.Battery3Bar,
        Icons.Filled.Battery6Bar,
        Icons.Filled.BatteryFull
    )
    val energyLabels = listOf("Drained", "Low", "Okay", "Good", "Energized")
    var selectedEnergy by remember { mutableIntStateOf(-1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Energy Level",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                energyIcons.forEachIndexed { index, icon ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilterChip(
                            onClick = { selectedEnergy = index },
                            label = { 
                                Icon(
                                    icon,
                                    contentDescription = energyLabels[index],
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            selected = selectedEnergy == index,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            energyLabels[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedEnergy == index) 
                                MaterialTheme.colorScheme.secondary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomTracker() {
    val symptoms = listOf(
        "Bloated", "Tired", "Nauseous", "Headache", "Skin Issues",
        "Brain Fog", "Joint Pain", "Mood Changes", "Sleep Issues"
    )
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Symptoms",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                symptoms.forEach { symptom ->
                    FilterChip(
                        onClick = { 
                            selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                                selectedSymptoms - symptom
                            } else {
                                selectedSymptoms + symptom
                            }
                        },
                        label = { Text(symptom) },
                        selected = selectedSymptoms.contains(symptom),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesSection() {
    var notes by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Additional Notes",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("How are you feeling? Any other symptoms or observations?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun WellbeingScreenPreview() {
    AndroidifyTheme {
        WellbeingScreen()
    }
} 