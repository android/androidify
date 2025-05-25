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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Performance optimization utilities for navigation based on Material Design 3 Expressive guidelines
 */

/**
 * Material 3 Expressive motion specs optimized for instant feedback
 * Based on: https://m3.material.io/foundations/interaction/motion/applying-motion
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object NavigationMotionSpecs {
    /**
     * Ultra-fast transition for immediate user feedback (50ms)
     * Use for: Navigation bar selections, instant UI feedback
     */
    val instantResponse: AnimationSpec<Float> = tween(
        durationMillis = 50,
        easing = FastOutSlowInEasing
    )
    
    /**
     * Fast transition for page switching (100ms)
     * Use for: Main navigation transitions, tab switching
     */
    val fastTransition: AnimationSpec<Float> = tween(
        durationMillis = 100,
        easing = FastOutSlowInEasing
    )
    
    /**
     * Quick transition for secondary animations (150ms)
     * Use for: Secondary UI elements, supporting animations
     */
    val quickTransition: AnimationSpec<Float> = tween(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    )
}

/**
 * Memoized navigation state cache to prevent unnecessary recompositions
 */
@Stable
class NavigationStateCache {
    private var cachedCurrentTab by mutableStateOf<BottomNavTab?>(null)
    private var cachedRoute by mutableStateOf<NavigationRoute?>(null)
    
    fun updateCurrentTab(tab: BottomNavTab?) {
        if (cachedCurrentTab != tab) {
            cachedCurrentTab = tab
        }
    }
    
    fun updateCurrentRoute(route: NavigationRoute?) {
        if (cachedRoute != route) {
            cachedRoute = route
        }
    }
    
    fun getCurrentTab(): BottomNavTab? = cachedCurrentTab
    fun getCurrentRoute(): NavigationRoute? = cachedRoute
    
    fun shouldUpdateNavigation(newTab: BottomNavTab): Boolean {
        return cachedCurrentTab != newTab
    }
}

/**
 * Optimized backstack manager that reduces unnecessary operations
 */
@Stable
class OptimizedBackStackManager(
    private val backStack: SnapshotStateList<NavigationRoute>
) {
    
    /**
     * Smart navigation that only performs operations when necessary
     */
    fun navigateToTab(tab: BottomNavTab, force: Boolean = false) {
        val targetRoute = tab.toNavigationRoute()
        val currentRoute = backStack.lastOrNull()
        
        // Only navigate if it's a different tab or forced
        if (force || currentRoute?.toBottomNavTab() != tab) {
            // Check if we already have this tab in history
            val existingIndex = backStack.indexOfFirst { it.toBottomNavTab() == tab }
            
            if (existingIndex != -1) {
                // Remove existing and add to end to maintain history
                backStack.removeAt(existingIndex)
            }
            
            backStack.add(targetRoute)
        }
    }
    
    /**
     * Optimized back navigation
     */
    fun navigateBack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeLastOrNull()
            true
        } else {
            false
        }
    }
    
    /**
     * Smart route clearing that preserves important navigation state
     */
    fun clearAndNavigateTo(route: NavigationRoute) {
        val currentTab = backStack.lastOrNull()?.toBottomNavTab()
        backStack.clear()
        backStack.add(route)
        
        // Preserve tab context if relevant
        if (route is MainTabRoute && currentTab != null) {
            // Additional context preservation logic can be added here
        }
    }
    
    /**
     * Batch operations for better performance
     */
    fun batchUpdate(operations: () -> Unit) {
        operations()
    }
}

/**
 * Performance-optimized composable for managing navigation state
 */
@Composable
fun rememberOptimizedNavigationState(): NavigationStateCache {
    return remember { NavigationStateCache() }
}

/**
 * Performance-optimized backstack manager
 */
@Composable
fun rememberOptimizedBackStackManager(
    initialRoute: NavigationRoute = MealTrackingChoiceTab()
): OptimizedBackStackManager {
    val backStack = remember { mutableStateListOf(initialRoute) }
    return remember(backStack) { OptimizedBackStackManager(backStack) }
}

/**
 * Material 3 Expressive animation constants for consistent performance
 */
object NavigationAnimationConstants {
    // Elevation values optimized for performance
    val DefaultElevation: Dp = 0.dp
    val SelectedElevation: Dp = 2.dp
    
    // Scale values for subtle feedback
    val DefaultScale = 1.0f
    val SelectedScale = 1.05f  // Reduced from 1.08f for subtler effect
    val PressedScale = 0.98f
    
    // Alpha values for state indication
    val InactiveAlpha = 0.7f
    val ActiveAlpha = 1.0f
    val DisabledAlpha = 0.4f
    
    // Color transition optimization
    val InstantColorTransition = NavigationMotionSpecs.instantResponse
    val FastScaleTransition = NavigationMotionSpecs.fastTransition
}

/**
 * Extension functions for optimized Material 3 motion specs
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MotionScheme.optimizedFastSpec(): AnimationSpec<Float> {
    return NavigationMotionSpecs.instantResponse
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MotionScheme.optimizedDefaultSpec(): AnimationSpec<Float> {
    return NavigationMotionSpecs.fastTransition
}

/**
 * Performance monitoring utilities for navigation
 */
object NavigationPerformanceMonitor {
    private var navigationStartTime = 0L
    private var lastTransitionDuration = 0L
    
    fun startNavigationTiming() {
        navigationStartTime = System.currentTimeMillis()
    }
    
    fun endNavigationTiming() {
        lastTransitionDuration = System.currentTimeMillis() - navigationStartTime
    }
    
    fun getLastTransitionDuration(): Long = lastTransitionDuration
    
    /**
     * Log performance metrics for navigation
     * Can be expanded to integrate with performance monitoring tools
     */
    fun logPerformanceMetrics(action: String) {
        if (lastTransitionDuration > 100) {
            // Log slow navigation for optimization
            println("NAVIGATION_PERFORMANCE: $action took ${lastTransitionDuration}ms")
        }
    }
} 