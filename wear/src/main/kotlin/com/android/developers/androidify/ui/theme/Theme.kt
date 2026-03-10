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
package com.android.developers.androidify.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val LimeGreen = Color(0xFFC6FF00)
val Primary80 = Color(0xFF6DDD81)
val Primary90 = Color(0xFF89FA9B)
val Blue = Color(0xFF4285F4)

// Primary colors
val Primary = Color(0xFF34A853)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF34A853)
val OnPrimaryContainer = Color(0xFF202124)

// Secondary colors
val Secondary = Color(0xFFE6F4EA)
val OnSecondary = Color(0xFF202124)
val SecondaryContainer = Color(0xFFE6F4EA)
val OnSecondaryContainer = Color(0xFF202124)

// Tertiary colors
val Tertiary = Color(0xFF202124)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF202124)
val OnTertiaryContainer = Color(0xFFFFFFFF)

// Error colors
val Error = Color(0xFFBA1A1A) // Red
val OnError = Color(0xFFFFFFFF) // White
val ErrorContainer = Color(0xFFFFDAD6) // Light Red
val OnErrorContainer = Color(0xFF93000A) // Dark Red

// Surface colors
val Surface = Color(0xFFF1F3F4) // White
val SurfaceBright = Color(0xFFE6F4EA) // Light Green
val InverseSurface = Color(0xFF313030) // Dark Gray
val InverseOnSurface = Color(0xFFF3F0EF) // Light gray
val SurfaceContainerLowest = Color(0xFFFFFFFF) // Off White
val SurfaceContainerLow = Color(0xFFEEF0F2) // Light gray
val SurfaceContainer = Color(0xFFE8EBED) // Gray
val SurfaceContainerHigh = Color(0xFFE5E9EB) // Dark Gray
val SurfaceContainerHighest = Color(0xFFE5E9EB) // Very dark Gray

// Others colors
val OnSurface = Color(0xFF202124) // Black
val OnSurfaceVariant = Color(0xFF434846) // Dark Gray
val Outline = Color(0xFF202124) // Dark Gray
val OutlineVariant = Color(0xFF313030) // Light Dark Gray
val Scrim = Color(0xFF000000) // Black
val Shadow = Color(0xFF000000) // Dark gray

private val wearColorScheme = ColorScheme(
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
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

@Composable
fun AndroidifyWearTheme(
    content: @Composable () -> Unit,
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        colorScheme = wearColorScheme,
        content = content,
    )
}
