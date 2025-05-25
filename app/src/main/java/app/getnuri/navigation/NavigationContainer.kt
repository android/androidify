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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.getnuri.theme.Primary

/**
 * Container that wraps content with bottom navigation when appropriate.
 * Shows navigation bar only on main tab routes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavigationContainer(
    currentRoute: NavigationRoute,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val shouldShowBottomNav = currentRoute is MainTabRoute
    val selectedTab = currentRoute.toBottomNavTab()
    val motionScheme = MaterialTheme.motionScheme

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomNav && selectedTab != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = motionScheme.defaultEffectsSpec()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = motionScheme.defaultEffectsSpec()
                )
            ) {
                if (selectedTab != null) {
                    BottomNavigationBar(
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected
                    )
                }
            }
        },
        containerColor = Primary
    ) { paddingValues ->
        content(paddingValues)
    }
}

/**
 * Helper function to determine if a route should display bottom navigation
 */
fun shouldShowBottomNavigation(route: NavigationRoute): Boolean {
    return when (route) {
        is MainTabRoute -> true
        // Legacy routes that should be considered main tabs
        is MealTrackingChoice -> true
        is MealHistory -> true
        Wellbeing -> true
        Results -> true
        else -> false
    }
}

/**
 * Helper function to get the default tab when navigation starts
 */
fun getDefaultTab(): BottomNavTab = BottomNavTab.Meals 