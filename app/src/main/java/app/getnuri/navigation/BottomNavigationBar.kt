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
package app.getnuri.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.getnuri.theme.AndroidifyTheme
import app.getnuri.theme.PrimaryContainer
import kotlinx.coroutines.delay

enum class BottomNavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Meals("Meals", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    Wellbeing("Wellbeing", Icons.Filled.FavoriteBorder, Icons.Outlined.FavoriteBorder),
    History("History", Icons.Filled.History, Icons.Outlined.History),
    Results("Results", Icons.Filled.Analytics, Icons.Outlined.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.motionScheme

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        BottomNavTab.entries.forEachIndexed { index, tab ->
            val selected = selectedTab == tab
            
            // Material 3 expressive animations with proper easing curves and durations
            // Standard curve (FastOutSlowInEasing) for scale - 300ms duration as per Material specs
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "scale_animation"
            )
            
            // Deceleration curve (LinearOutSlowInEasing) for entering elements - 225ms
            val iconTint by animateColorAsState(
                targetValue = if (selected) 
                    Color.White  // White icons on purple highlight
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(
                    durationMillis = 225,
                    easing = LinearOutSlowInEasing
                ),
                label = "icon_tint_animation"
            )
            
            // Standard curve for text color transitions - 300ms
            val labelColor by animateColorAsState(
                targetValue = if (selected) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "label_color_animation"
            )
            
            // Additional expressive animation for text size with staggered delay
            val textSize by animateFloatAsState(
                targetValue = if (selected) 12f else 10f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * 50, // Staggered animation
                    easing = FastOutSlowInEasing
                ),
                label = "text_size_animation"
            )
            
            // Selection feedback with bounce effect
            val selectionBounce = remember { Animatable(1f) }
            LaunchedEffect(selected) {
                if (selected) {
                    // Quick bounce effect on selection - Material 3 expressive feedback
                    selectionBounce.animateTo(
                        targetValue = 1.2f,
                        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                    )
                    selectionBounce.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }
            }
            
            // Background bubble animation - only for icon area
            val backgroundAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "background_alpha_animation"
            )

            // Custom navigation item with icon-only background bubble
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClick = { onTabSelected(tab) },
                        role = Role.Tab
                    )
                    .padding(vertical = 8.dp)
            ) {
                // Icon with background bubble
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(scale)
                        .scale(selectionBounce.value)
                ) {
                    // Background bubble - only behind icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = PrimaryContainer.copy(alpha = backgroundAlpha),
                                shape = CircleShape
                            )
                    )
                    
                    // Icon
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Text label - separate from background
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = textSize.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = labelColor
                )
            }
        }
    }
}

// Extension function to map tabs to navigation routes
fun BottomNavTab.toNavigationRoute(): NavigationRoute = when (this) {
    BottomNavTab.Meals -> MealTrackingChoiceTab()
    BottomNavTab.Wellbeing -> WellbeingTab
    BottomNavTab.History -> MealHistoryTab
    BottomNavTab.Results -> ResultsTab
}

// Extension function to get tab from navigation route
fun NavigationRoute.toBottomNavTab(): BottomNavTab? = when (this) {
    is MealTrackingChoiceTab, is MealTrackingChoice -> BottomNavTab.Meals
    is WellbeingTab, Wellbeing -> BottomNavTab.Wellbeing
    is MealHistoryTab, MealHistory -> BottomNavTab.History
    is ResultsTab, Results -> BottomNavTab.Results
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    AndroidifyTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            BottomNavigationBar(
                selectedTab = BottomNavTab.Meals,
                onTabSelected = { }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarWellbeingSelectedPreview() {
    AndroidifyTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            BottomNavigationBar(
                selectedTab = BottomNavTab.Wellbeing,
                onTabSelected = { }
            )
        }
    }
} 