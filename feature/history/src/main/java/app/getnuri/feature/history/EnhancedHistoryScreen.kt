package app.getnuri.feature.history

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.getnuri.feature.history.model.TimelineEntry
import app.getnuri.feature.history.model.DayGroup
import app.getnuri.theme.components.ExpressiveCard
import app.getnuri.theme.components.ExpressiveGradientBackground
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHistoryScreen(
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: EnhancedHistoryViewModel = hiltViewModel()
) {
    val timelineData by viewModel.timelineData.collectAsState()

    ExpressiveGradientBackground {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Your Journey",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (timelineData.isEmpty()) {
                EmptyTimelineState(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timelineData.forEach { dayGroup ->
                        // Day header with expressive typography
                        item(key = "header_${dayGroup.date}") {
                            ExpressiveDayHeader(
                                displayName = dayGroup.displayName,
                                entryCount = dayGroup.entries.size,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Timeline entries for this day
                        items(
                            items = dayGroup.entries,
                            key = { entry -> 
                                when (entry) {
                                    is TimelineEntry.MealEntry -> "meal_${entry.meal.id}"
                                    is TimelineEntry.WellbeingEntry -> "wellbeing_${entry.feedback.id}"
                                }
                            }
                        ) { entry ->
                            TimelineEntryCard(
                                entry = entry,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Day separator
                        item(key = "separator_${dayGroup.date}") {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveDayHeader(
    displayName: String,
    entryCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$entryCount ${if (entryCount == 1) "entry" else "entries"}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TimelineEntryCard(
    entry: TimelineEntry,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "card_scale"
    )

    ExpressiveCard(
        modifier = modifier
            .scale(scale)
            .padding(vertical = 4.dp),
        cornerRadius = 20.dp,
        elevation = if (isExpanded) 12.dp else 6.dp,
        backgroundColor = when (entry) {
            is TimelineEntry.MealEntry -> MaterialTheme.colorScheme.surfaceContainer
            is TimelineEntry.WellbeingEntry -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        }
    ) {
        when (entry) {
            is TimelineEntry.MealEntry -> MealEntryContent(
                entry = entry,
                isExpanded = isExpanded,
                onToggleExpanded = { isExpanded = !isExpanded }
            )
            is TimelineEntry.WellbeingEntry -> WellbeingEntryContent(
                entry = entry,
                isExpanded = isExpanded,
                onToggleExpanded = { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
fun MealEntryContent(
    entry: TimelineEntry.MealEntry,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val meal = entry.meal
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with meal icon and summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (meal.inputType == "PHOTO") Icons.Filled.CameraAlt else Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeFormat.format(Date(meal.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onToggleExpanded) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expanded content
        if (isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Meal image if available
                if (meal.inputType == "PHOTO" && meal.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(meal.photoUri))
                                .build()
                        ),
                        contentDescription = "Meal photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Ingredients
                if (meal.rawExtractedIngredients.isNotEmpty()) {
                    EnhancedExpressiveDetailSection(
                        title = "Ingredients",
                        content = meal.rawExtractedIngredients.joinToString(", "),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Potential triggers
                if (meal.rawExtractedTriggers.isNotEmpty()) {
                    EnhancedExpressiveDetailSection(
                        title = "Potential Triggers",
                        content = meal.rawExtractedTriggers.joinToString(", "),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Notes
                meal.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        EnhancedExpressiveDetailSection(
                            title = "Notes",
                            content = notes,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WellbeingEntryContent(
    entry: TimelineEntry.WellbeingEntry,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val feedback = entry.feedback
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with feeling icon and summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = getFeelingColor(feedback.feelingDescription),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getFeelingIcon(feedback.feelingDescription),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeFormat.format(Date(feedback.feedbackTimestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onToggleExpanded) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expanded content
        if (isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Feeling details
                EnhancedExpressiveDetailSection(
                    title = "How you felt",
                    content = feedback.feelingDescription + 
                             (feedback.customFeeling?.let { " ($it)" } ?: ""),
                    color = getFeelingColor(feedback.feelingDescription)
                )
                
                // Notes
                feedback.feedbackNotes?.let { notes ->
                    if (notes.isNotBlank()) {
                        EnhancedExpressiveDetailSection(
                            title = "Notes",
                            content = notes,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                // Related meal info
                entry.relatedMeal?.let { meal ->
                    EnhancedExpressiveDetailSection(
                        title = "Related to",
                        content = meal.rawExtractedIngredients.take(3).joinToString(", ") +
                                if (meal.rawExtractedIngredients.size > 3) "..." else "",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedExpressiveDetailSection(
    title: String,
    content: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyTimelineState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ExpressiveCard(
            modifier = Modifier.padding(32.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Timeline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your journey starts here",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Log your first meal and track how you feel to begin building your wellness timeline.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper functions for feeling visualization
@Composable
fun getFeelingColor(feeling: String): Color {
    return when (feeling.lowercase()) {
        "great", "excellent", "amazing" -> Color(0xFF4CAF50) // Green
        "good", "fine", "okay" -> Color(0xFF2196F3) // Blue
        "bloated", "uncomfortable" -> Color(0xFFFF9800) // Orange
        "tired", "sluggish" -> Color(0xFF9C27B0) // Purple
        "bad", "awful", "sick" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.tertiary
    }
}

fun getFeelingIcon(feeling: String): ImageVector {
    return when (feeling.lowercase()) {
        "great", "excellent", "amazing" -> Icons.Filled.SentimentVerySatisfied
        "good", "fine" -> Icons.Filled.SentimentSatisfied
        "okay" -> Icons.Filled.SentimentNeutral
        "bloated", "uncomfortable" -> Icons.Filled.SentimentDissatisfied
        "tired", "sluggish", "bad", "awful", "sick" -> Icons.Filled.SentimentVeryDissatisfied
        else -> Icons.Filled.Mood
    }
} 