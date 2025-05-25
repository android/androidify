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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package app.getnuri.results

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.getnuri.theme.AndroidifyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WellbeingChartsSection(
    wellbeingData: WellbeingData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Your Wellbeing Journey",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Last 4 weeks",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Mood Chart
        ChartCard(
            title = "Mood Trends",
            subtitle = "Daily mood tracking",
            icon = "😊"
        ) {
            MoodLineChart(
                moodEntries = wellbeingData.moodEntries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        
        // Energy Chart
        ChartCard(
            title = "Energy Levels",
            subtitle = "Morning, afternoon & evening",
            icon = "⚡"
        ) {
            EnergyBarChart(
                energyEntries = wellbeingData.energyEntries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        
        // Symptoms Chart
        ChartCard(
            title = "Symptom Tracking",
            subtitle = "Intensity and frequency",
            icon = "🩺"
        ) {
            SymptomsScatterChart(
                symptomEntries = wellbeingData.symptomEntries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun MoodLineChart(
    moodEntries: List<MoodEntry>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(moodEntries) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = EaseOutBack
            )
        )
    }
    
    Canvas(modifier = modifier) {
        if (moodEntries.isEmpty()) return@Canvas
        
        val progress = animationProgress.value
        val chartWidth = size.width - 80.dp.toPx()
        val chartHeight = size.height - 80.dp.toPx()
        val startX = 40.dp.toPx()
        val startY = 40.dp.toPx()
        
        // Draw background grid
        drawMoodGrid(startX, startY, chartWidth, chartHeight)
        
        // Draw mood line with gradient
        drawMoodLine(moodEntries, startX, startY, chartWidth, chartHeight, progress)
        
        // Draw mood points
        drawMoodPoints(moodEntries, startX, startY, chartWidth, chartHeight, progress)
    }
}

private fun DrawScope.drawMoodGrid(
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    
    // Horizontal grid lines (mood levels)
    for (i in 0..10) {
        val y = startY + chartHeight - (i / 10f) * chartHeight
        drawLine(
            color = gridColor,
            start = Offset(startX, y),
            end = Offset(startX + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Vertical grid lines (weeks)
    for (i in 0..4) {
        val x = startX + (i / 4f) * chartWidth
        drawLine(
            color = gridColor,
            start = Offset(x, startY),
            end = Offset(x, startY + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawMoodLine(
    moodEntries: List<MoodEntry>,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    progress: Float
) {
    if (moodEntries.size < 2) return
    
    val path = Path()
    val gradientColors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899)  // Pink
    )
    
    val points = moodEntries.mapIndexed { index, entry ->
        val x = startX + (index.toFloat() / (moodEntries.size - 1)) * chartWidth
        val y = startY + chartHeight - ((entry.mood - 1f) / 9f) * chartHeight
        Offset(x, y)
    }
    
    // Create smooth curve through points
    path.moveTo(points[0].x, points[0].y)
    
    for (i in 1 until points.size) {
        val currentPoint = points[i]
        val previousPoint = points[i - 1]
        
        val controlPoint1X = previousPoint.x + (currentPoint.x - previousPoint.x) * 0.5f
        val controlPoint1Y = previousPoint.y
        val controlPoint2X = currentPoint.x - (currentPoint.x - previousPoint.x) * 0.5f
        val controlPoint2Y = currentPoint.y
        
        path.cubicTo(
            controlPoint1X, controlPoint1Y,
            controlPoint2X, controlPoint2Y,
            currentPoint.x, currentPoint.y
        )
    }
    
    // Draw gradient fill
    val fillPath = Path().apply {
        addPath(path)
        lineTo(points.last().x, startY + chartHeight)
        lineTo(points.first().x, startY + chartHeight)
        close()
    }
    
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = gradientColors.map { it.copy(alpha = 0.3f * progress) },
            startY = startY,
            endY = startY + chartHeight
        )
    )
    
    // Draw line stroke
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(gradientColors),
        style = Stroke(
            width = 4.dp.toPx() * progress,
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawMoodPoints(
    moodEntries: List<MoodEntry>,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    progress: Float
) {
    moodEntries.forEachIndexed { index, entry ->
        val x = startX + (index.toFloat() / (moodEntries.size - 1)) * chartWidth
        val y = startY + chartHeight - ((entry.mood - 1f) / 9f) * chartHeight
        
        val pointProgress = (progress * moodEntries.size - index).coerceIn(0f, 1f)
        
        if (pointProgress > 0f) {
            // Outer glow
            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.3f * pointProgress),
                radius = 12.dp.toPx() * pointProgress,
                center = Offset(x, y)
            )
            
            // Main point
            drawCircle(
                color = Color(0xFF6366F1),
                radius = 6.dp.toPx() * pointProgress,
                center = Offset(x, y)
            )
            
            // Inner highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 2.dp.toPx() * pointProgress,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun EnergyBarChart(
    energyEntries: List<EnergyEntry>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(energyEntries) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                delayMillis = 300,
                easing = EaseOutBack
            )
        )
    }
    
    Canvas(modifier = modifier) {
        if (energyEntries.isEmpty()) return@Canvas
        
        val progress = animationProgress.value
        val chartWidth = size.width - 80.dp.toPx()
        val chartHeight = size.height - 80.dp.toPx()
        val startX = 40.dp.toPx()
        val startY = 40.dp.toPx()
        
        // Group by date and time of day
        val groupedEntries = energyEntries.groupBy { it.date }
        val dates = groupedEntries.keys.sorted()
        
        val barWidth = chartWidth / (dates.size * 3 + dates.size - 1) // 3 bars per day + spacing
        val groupSpacing = barWidth
        
        dates.forEachIndexed { dateIndex, date ->
            val dayEntries = groupedEntries[date] ?: emptyList()
            val timeSlots = listOf("morning", "afternoon", "evening")
            val colors = listOf(
                Color(0xFFFBBF24), // Yellow for morning
                Color(0xFFF97316), // Orange for afternoon
                Color(0xFF7C3AED)  // Purple for evening
            )
            
            timeSlots.forEachIndexed { timeIndex, timeSlot ->
                val entry = dayEntries.find { it.timeOfDay == timeSlot }
                if (entry != null) {
                    val barHeight = ((entry.energy - 1f) / 9f) * chartHeight * progress
                    val x = startX + dateIndex * (barWidth * 3 + groupSpacing) + timeIndex * barWidth
                    val y = startY + chartHeight - barHeight
                    
                    // Draw bar with rounded corners
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors[timeIndex],
                                colors[timeIndex].copy(alpha = 0.7f)
                            )
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth * 0.8f, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun SymptomsScatterChart(
    symptomEntries: List<SymptomEntry>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(symptomEntries) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2000,
                delayMillis = 600,
                easing = EaseOutBack
            )
        )
    }
    
    Canvas(modifier = modifier) {
        if (symptomEntries.isEmpty()) return@Canvas
        
        val progress = animationProgress.value
        val chartWidth = size.width - 80.dp.toPx()
        val chartHeight = size.height - 80.dp.toPx()
        val startX = 40.dp.toPx()
        val startY = 40.dp.toPx()
        
        val uniqueSymptoms = symptomEntries.map { it.symptom }.distinct()
        val symptomColors = listOf(
            Color(0xFFEF4444), // Red
            Color(0xFFF97316), // Orange
            Color(0xFFFBBF24), // Yellow
            Color(0xFF22C55E), // Green
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF6B7280)  // Gray
        )
        
        val dateRange = symptomEntries.map { it.date }.let { dates ->
            dates.minOrNull()!! to dates.maxOrNull()!!
        }
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(dateRange.first, dateRange.second).toInt() + 1
        
        symptomEntries.forEachIndexed { index, entry ->
            val dayIndex = java.time.temporal.ChronoUnit.DAYS.between(dateRange.first, entry.date).toInt()
            val x = startX + (dayIndex.toFloat() / (daysDiff - 1)) * chartWidth
            val y = startY + chartHeight - ((entry.intensity - 1f) / 9f) * chartHeight
            
            val symptomIndex = uniqueSymptoms.indexOf(entry.symptom)
            val color = symptomColors[symptomIndex % symptomColors.size]
            val radius = (entry.intensity / 10f) * 16.dp.toPx() + 4.dp.toPx()
            
            val pointProgress = (progress * symptomEntries.size - index).coerceIn(0f, 1f)
            
            if (pointProgress > 0f) {
                // Pulsing effect
                val pulseRadius = radius * (1f + 0.3f * sin(System.currentTimeMillis() / 200f + index))
                
                // Outer glow
                drawCircle(
                    color = color.copy(alpha = 0.2f * pointProgress),
                    radius = pulseRadius,
                    center = Offset(x, y)
                )
                
                // Main circle
                drawCircle(
                    color = color.copy(alpha = 0.8f * pointProgress),
                    radius = radius * pointProgress,
                    center = Offset(x, y)
                )
                
                // Inner highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f * pointProgress),
                    radius = radius * 0.3f * pointProgress,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Preview
@Composable
private fun WellbeingChartsPreview() {
    AndroidifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            WellbeingChartsSection(
                wellbeingData = MockWellbeingDataGenerator.generateMockData()
            )
        }
    }
} 