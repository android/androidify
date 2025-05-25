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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)

package app.getnuri.theme

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// Material 3 Expressive Color Scheme with rich purples and gradients
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    onSecondary = OnSecondary,

    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    onTertiary = OnTertiary,

    error = Error,
    onError = OnError,
    onErrorContainer = OnErrorContainer,
    errorContainer = ErrorContainer,
    
    // Updated surface colors for expressive theme
    surface = Surface,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    
    // Enhanced surface containers with purple tints
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    
    // Improved text and outline colors
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
)

// Expressive shapes with larger corner radiuses and organic feeling
val shapes = Shapes(
    // Small components - subtle roundness
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    
    // Medium components - more expressive curves  
    medium = RoundedCornerShape(16.dp),
    
    // Large components - bold, flowing shapes
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AndroidifyTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = LightColorScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        motionScheme = MotionScheme.expressive(),
        content = {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    content()
                }
            }
        },
    )
}
