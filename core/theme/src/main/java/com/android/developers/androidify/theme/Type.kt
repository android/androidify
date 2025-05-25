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
package app.getnuri.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import app.getnuri.theme.BuildConfig

// Material 3 Expressive Typography with flexible, variable fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// Use Outfit for expressive display text (variable font with dramatic weights)
val outfitFont = GoogleFont("Outfit")
val interFont = GoogleFont("Inter")

val displayFontFamily = FontFamily(
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = outfitFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

val bodyFontFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Bold),
)

// Legacy font family for backward compatibility
val fontName = GoogleFont(BuildConfig.fontName)
val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
)

// Material 3 Expressive Typography with flexible line heights and expressive scaling
val Typography = Typography(
    // Display styles - Expressive and dramatic
    displayLarge = TextStyle(
        fontSize = 64.sp,
        lineHeight = 72.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    displayMedium = TextStyle(
        fontSize = 52.sp,
        lineHeight = 60.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    displaySmall = TextStyle(
        fontSize = 42.sp,
        lineHeight = 50.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    
    // Headlines - Expressive but readable
    headlineLarge = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    headlineMedium = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    headlineSmall = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    
    // Titles - Clean and purposeful
    titleLarge = TextStyle(
        fontSize = 24.sp,
        lineHeight = TextUnit(1.2f, type = TextUnitType.Em),
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineBreak = LineBreak.Heading.copy(
            wordBreak = LineBreak.WordBreak.Phrase
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    titleMedium = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    titleSmall = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    
    // Body text - Highly readable with generous line height
    bodyLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineBreak = LineBreak.Paragraph.copy(
            wordBreak = LineBreak.WordBreak.Default
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
        lineBreak = LineBreak.Paragraph.copy(
            wordBreak = LineBreak.WordBreak.Default
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    bodySmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
        lineBreak = LineBreak.Paragraph.copy(
            wordBreak = LineBreak.WordBreak.Default
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    
    // Labels - Clear and actionable
    labelLarge = TextStyle(
        fontFamily = bodyFontFamily,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    labelMedium = TextStyle(
        fontFamily = bodyFontFamily,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    labelSmall = TextStyle(
        fontFamily = bodyFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
)
