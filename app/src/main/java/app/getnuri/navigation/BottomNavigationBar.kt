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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.getnuri.theme.AndroidifyTheme
import app.getnuri.theme.Primary
import app.getnuri.theme.PrimaryContainer
import app.getnuri.theme.Secondary

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
    
    // Simplified gradient background - remove dynamic calculations
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Primary.copy(alpha = 0.98f),
            Primary.copy(alpha = 0.92f)
        )
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Static background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        )
        
        // Simplified border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Secondary.copy(alpha = 0.6f))
        )
        
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            BottomNavTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                
                // OPTIMIZED: Single scale animation using fast motion specs for instant response
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.08f else 1.0f,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "scale_animation"
                )
                
                // OPTIMIZED: Single color animation with fast specs
                val iconTint by animateColorAsState(
                    targetValue = if (selected) 
                        Color.Black.copy(alpha = 0.8f)
                    else 
                        Color.White.copy(alpha = 0.7f),
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "icon_tint_animation"
                )
                
                // OPTIMIZED: Simplified label color - remove text size animation
                val labelColor by animateColorAsState(
                    targetValue = if (selected) 
                        Color.Black.copy(alpha = 0.9f)
                    else 
                        Color.White.copy(alpha = 0.8f),
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "label_color_animation"
                )
                
                // OPTIMIZED: Single background animation with fast response
                val backgroundAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "background_alpha_animation"
                )

                // OPTIMIZED: Simplified navigation item without complex bounce effects
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
                    // OPTIMIZED: Single transformation with background bubble
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.scale(scale)
                    ) {
                        // Background bubble with instant response
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    color = Secondary.copy(alpha = backgroundAlpha * 0.9f),
                                    shape = CircleShape
                                )
                        )
                        
                        // Icon with simplified state
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // OPTIMIZED: Static text label - remove size animations
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = labelColor
                    )
                }
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
            color = Primary // Use primary color background to match app theme
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
            color = Primary // Use primary color background to match app theme
        ) {
            BottomNavigationBar(
                selectedTab = BottomNavTab.Wellbeing,
                onTabSelected = { }
            )
        }
    }
} 