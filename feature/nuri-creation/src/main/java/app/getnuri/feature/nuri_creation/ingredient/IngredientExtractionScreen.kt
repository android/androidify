package app.getnuri.feature.nuri_creation.ingredient

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.getnuri.data.model.MealAnalysisData
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IngredientExtractionScreen(
    mealTitle: String,
    mealImageUri: String?,
    analysisData: MealAnalysisData?,
    onBackPressed: () -> Unit = {},
    onNextPressed: (List<String>) -> Unit = {},
    onEditIngredient: (Int, ExtractedIngredient) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: IngredientExtractionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize the viewmodel with data
    LaunchedEffect(mealTitle, mealImageUri, analysisData) {
        val scheduledTime = "Tuesday, May 6pm" // Mock data - you can make this dynamic
        viewModel.initializeWithData(mealTitle, mealImageUri, analysisData, scheduledTime)
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Nutrition",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Expressive Next button - large, prominent, strategically colored
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Button(
                        onClick = { 
                            onNextPressed(viewModel.getIngredientsForSaving())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp), // Large tap target per M3 Expressive
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(28.dp) // More expressive shape
                    ) {
                        Text(
                            "Next",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Meal Info Card - Expressive hero section
            item {
                MealInfoCard(
                    mealInfo = uiState.mealInfo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Ingredients Section Header
            item {
                Text(
                    text = "Ingredients (${uiState.ingredients.size})",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Ingredients List
            itemsIndexed(uiState.ingredients) { index, ingredient ->
                IngredientCard(
                    ingredient = ingredient,
                    onEditClick = { 
                        onEditIngredient(index, ingredient)
                    },
                    onIngredientChanged = { newIngredient ->
                        viewModel.updateIngredient(index, newIngredient)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Add some bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
} 