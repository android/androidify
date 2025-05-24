package app.getnuri.feature.history

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.getnuri.feature.history.model.MealWithFeedback
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*

// Navigation comment:
// This screen displays the list of meals and their feedback.
// Interactions like navigating to a meal detail screen or feedback entry screen
// would be handled via callbacks or NavController.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealHistoryScreen(
    // onNavigateToMealDetail: (Long) -> Unit, // Example for navigation
    // onNavigateToFeedbackEntry: (Long) -> Unit, // Example for navigation
    modifier: Modifier = Modifier,
    viewModel: MealHistoryViewModel = hiltViewModel()
) {
    val mealHistory by viewModel.mealHistory.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Meal History") })
        }
    ) { paddingValues ->
        if (mealHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No meals logged yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mealHistory, key = { it.meal.id }) { item ->
                    MealHistoryItem(mealWithFeedback = item)
                }
            }
        }
    }
}

@Composable
fun MealHistoryItem(mealWithFeedback: MealWithFeedback) {
    val meal = mealWithFeedback.meal
    val feedbackList = mealWithFeedback.feedback

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (meal.inputType == "PHOTO") Icons.Filled.Image else Icons.Filled.Fastfood,
                    contentDescription = meal.inputType,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meal - ${meal.inputType}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = dateFormat.format(Date(meal.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (meal.inputType == "PHOTO" && meal.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(meal.photoUri))
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Meal photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else if (meal.inputType == "TEXT" && meal.description != null) {
                Text(
                    text = "\"${meal.description}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            DetailRow("Ingredients:", meal.rawExtractedIngredients.joinToString(", "))
            DetailRow("Triggers:", meal.rawExtractedTriggers.joinToString(", "))

            if (meal.notes != null && meal.notes.isNotBlank()) {
                DetailRow("Meal Notes:", meal.notes)
            }
            
            if (feedbackList.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Feedback Logged:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                feedbackList.forEach { feedback ->
                    Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Text(
                            text = "Feeling: ${feedback.feelingDescription}" +
                                   (feedback.customFeeling?.let { " ($it)" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!feedback.feedbackNotes.isNullOrBlank()) {
                            Text(
                                text = "Notes: ${feedback.feedbackNotes}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Logged: ${dateFormat.format(Date(feedback.feedbackTimestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Use theme color
                        )
                    }
                }
            } else {
                Text(
                    "No feedback logged for this meal yet.",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(100.dp) // Fixed width for label
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
