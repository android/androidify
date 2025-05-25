package app.getnuri.history

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.getnuri.history.model.MealWithFeedback
import app.getnuri.theme.Primary
import app.getnuri.theme.Secondary
import app.getnuri.theme.EditorialTypography
import app.getnuri.theme.LocalSharedTransitionScope
import app.getnuri.theme.SharedElementKey
import app.getnuri.theme.components.AndroidifyTopAppBar
import app.getnuri.theme.components.ExpressiveCard
import app.getnuri.theme.displayFontFamily
import app.getnuri.theme.sharedBoundsWithDefaults
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// Navigation comment:
// Enhanced meal history screen with "nuri" branding, expressive day grouping, and Material 3 shape principles.
// Features heart shapes for wellbeing and bun shapes for meals with unified timeline grouping.

// Timeline entry types for unified display
sealed class TimelineEntry {
    abstract val timestamp: Long
    abstract val date: LocalDate
    
    data class MealEntry(
        val mealWithFeedback: MealWithFeedback,
        override val timestamp: Long,
        override val date: LocalDate
    ) : TimelineEntry()
    
    data class WellbeingEntry(
        val description: String,
        val feeling: String,
        val notes: String?,
        override val timestamp: Long,
        override val date: LocalDate
    ) : TimelineEntry()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MealHistoryScreen(
    onBackPressed: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MealHistoryViewModel = hiltViewModel()
) {
    val mealHistory by viewModel.mealHistory.collectAsState()
    
    // Create unified timeline with meals and mock wellbeing entries
    val timelineEntries = remember(mealHistory) {
        val entries = mutableListOf<TimelineEntry>()
        
        // Add meal entries
        mealHistory.forEach { mealWithFeedback ->
            entries.add(
                TimelineEntry.MealEntry(
                    mealWithFeedback = mealWithFeedback,
                    timestamp = mealWithFeedback.meal.timestamp,
                    date = timestampToLocalDate(mealWithFeedback.meal.timestamp)
                )
            )
            
            // Add wellbeing entries from feedback
            mealWithFeedback.feedback.forEach { feedback ->
                entries.add(
                    TimelineEntry.WellbeingEntry(
                        description = feedback.feelingDescription,
                        feeling = feedback.feelingDescription,
                        notes = feedback.feedbackNotes,
                        timestamp = feedback.feedbackTimestamp,
                        date = timestampToLocalDate(feedback.feedbackTimestamp)
                    )
                )
            }
        }
        
        // Group by date and sort
        entries.groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Primary,
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = false,
                aboutEnabled = true,
                isMediumWindowSize = false,
                useNuriStyling = true,
                customTitle = "nuri",
                onAboutClicked = onBackPressed,
            )
        }
    ) { contentPadding ->
        if (timelineEntries.isEmpty()) {
            MealHistoryEmptyState(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timelineEntries.forEach { (date, entriesForDate) ->
                    // Expressive day header with enhanced Roboto Flex
                    item(key = "header_$date") {
                        ExpressiveDayHeader(
                            date = date,
                            entryCount = entriesForDate.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Timeline entries for this day
                    items(
                        items = entriesForDate.sortedByDescending { it.timestamp },
                        key = { entry -> 
                            when (entry) {
                                is TimelineEntry.MealEntry -> "meal_${entry.mealWithFeedback.meal.id}"
                                is TimelineEntry.WellbeingEntry -> "wellbeing_${entry.timestamp}"
                            }
                        }
                    ) { entry ->
                        when (entry) {
                            is TimelineEntry.MealEntry -> {
                                ExpressiveMealTimelineItem(
                                    mealWithFeedback = entry.mealWithFeedback,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            is TimelineEntry.WellbeingEntry -> {
                                ExpressiveWellbeingTimelineItem(
                                    description = entry.description,
                                    feeling = entry.feeling,
                                    notes = entry.notes,
                                    timestamp = entry.timestamp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    
                    // Day separator
                    item(key = "separator_$date") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun ExpressiveDayHeader(
    date: LocalDate,
    entryCount: Int,
    modifier: Modifier = Modifier
) {
    val displayName = getDisplayNameForDate(date)
    val isToday = date == LocalDate.now()
    val isYesterday = date == LocalDate.now().minusDays(1)
    
    Column(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Enhanced day name with ultra-expressive Roboto Flex
        Text(
            text = displayName,
            style = EditorialTypography.flowingHeadline.copy(
                fontSize = if (isToday) 48.sp else if (isYesterday) 44.sp else 40.sp,
                fontWeight = if (isToday) FontWeight.Black else FontWeight.ExtraBold,
                letterSpacing = if (isToday) (-2.4).sp else if (isYesterday) (-2.0).sp else (-1.6).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Enhanced date and entry count
        if (!isToday && !isYesterday) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = displayFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Entry count with expressive styling
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "$entryCount ${if (entryCount == 1) "entry" else "entries"}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

// Heart shape for wellbeing entries
val HeartShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    
    // Heart shape path
    moveTo(width * 0.5f, height * 0.25f)
    
    // Left curve
    cubicTo(
        width * 0.2f, height * 0.1f,
        width * 0.1f, height * 0.4f,
        width * 0.5f, height * 0.8f
    )
    
    // Right curve  
    cubicTo(
        width * 0.9f, height * 0.4f,
        width * 0.8f, height * 0.1f,
        width * 0.5f, height * 0.25f
    )
    
    close()
}

// Bun shape for meal entries (rounded rectangle with subtle curves)
val BunShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val cornerRadius = width * 0.25f
    
    // Rounded rectangle with extra curves for bun-like appearance
    addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            left = 0f,
            top = height * 0.1f,
            right = width,
            bottom = height * 0.9f,
            radiusX = cornerRadius,
            radiusY = cornerRadius
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpressiveMealTimelineItem(
    mealWithFeedback: MealWithFeedback,
    modifier: Modifier = Modifier
) {
    val meal = mealWithFeedback.meal
    val feedbackList = mealWithFeedback.feedback
    var isExpanded by remember { mutableStateOf(false) }
    val sharedTransitionScope = LocalSharedTransitionScope.current

    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val mealType = determineMealType(meal.timestamp)
    
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "card_scale"
    )

    with(sharedTransitionScope) {
        ExpressiveCard(
            modifier = modifier
                .scale(scale)
                .padding(vertical = 4.dp)
                .clickable { isExpanded = !isExpanded },
            cornerRadius = 24.dp,
            elevation = if (isExpanded) 12.dp else 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Bun-shaped meal icon on the left
                Surface(
                    shape = BunShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (meal.inputType == "PHOTO") Icons.Filled.Image else Icons.Filled.Fastfood,
                            contentDescription = meal.inputType,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Content area
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header with meal type and timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mealType,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dateFormat.format(Date(meal.timestamp)),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Expansion indicator
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meal content preview
                    if (meal.inputType == "PHOTO" && meal.photoUri != null) {
                        AsyncImage(
                            ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(meal.photoUri))
                                .crossfade(300)
                                .build(),
                            contentDescription = "Meal photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .sharedBoundsWithDefaults(
                                    rememberSharedContentState(SharedElementKey.MealHistoryPhoto(meal.id.toString()))
                                )
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (meal.inputType == "TEXT" && meal.description != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${meal.description}\"",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Expanded content
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Show ingredients, triggers, notes, and feedback
                        if (meal.rawExtractedIngredients.isNotEmpty()) {
                            ExpressiveDetailSection(
                                title = "Ingredients",
                                content = meal.rawExtractedIngredients.joinToString(", "),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (meal.rawExtractedTriggers.isNotEmpty()) {
                            ExpressiveDetailSection(
                                title = "Potential Triggers",
                                content = meal.rawExtractedTriggers.joinToString(", "),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        meal.notes?.let { notes ->
                            if (notes.isNotBlank()) {
                                ExpressiveDetailSection(
                                    title = "Notes",
                                    content = notes,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveWellbeingTimelineItem(
    description: String,
    feeling: String,
    notes: String?,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var isExpanded by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "wellbeing_card_scale"
    )

    ExpressiveCard(
        modifier = modifier
            .scale(scale)
            .padding(vertical = 4.dp)
            .clickable { isExpanded = !isExpanded },
        cornerRadius = 24.dp,
        elevation = if (isExpanded) 12.dp else 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Heart-shaped wellbeing icon on the left
            Surface(
                shape = HeartShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Wellbeing",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header with feeling and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Feeling: $feeling",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(Date(timestamp)),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Expansion indicator
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Expanded content
                if (isExpanded && !notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveDetailSection(
    title: String,
    content: String,
    color: Color
) {
    if (content.isNotBlank()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = color
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MealHistoryEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ExpressiveCard(
            cornerRadius = 32.dp,
            elevation = 8.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Text(
                    "Your nuri journey starts here",
                    style = EditorialTypography.flowingHeadline.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    "Log your first meal to begin tracking your nutrition and wellness journey.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper functions
private fun timestampToLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun getDisplayNameForDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
    }
}

private fun determineMealType(timestamp: Long): String {
    val hour = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .hour
    
    return when (hour) {
        in 5..10 -> "Breakfast"
        in 11..14 -> "Lunch"
        in 15..17 -> "Snack"
        in 18..22 -> "Dinner"
        else -> "Meal"
    }
}
