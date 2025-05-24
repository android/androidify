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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.getnuri.theme.AndroidifyTheme

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
        BottomNavTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            
            // Material 3 expressive animations
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1.0f,
                animationSpec = motionScheme.defaultEffectsSpec(),
                label = "scale_animation"
            )
            
            val iconTint by animateColorAsState(
                targetValue = if (selected) 
                    MaterialTheme.colorScheme.onSecondaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = motionScheme.defaultEffectsSpec(),
                label = "icon_tint_animation"
            )
            
            val labelColor by animateColorAsState(
                targetValue = if (selected) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = motionScheme.defaultEffectsSpec(),
                label = "label_color_animation"
            )

            NavigationBarItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(scale)
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        // Expressive label that appears on selection
                        if (selected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = labelColor
                            )
                        }
                    }
                },
                selected = selected,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                label = null // We handle the label custom in the icon for expressive animation
            )
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