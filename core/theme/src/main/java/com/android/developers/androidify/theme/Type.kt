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
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import app.getnuri.theme.BuildConfig

// Material 3 Editorial Typography with Roboto Flex Variable Font
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// Roboto Flex - Google's most advanced variable font
val robotoFlexFont = GoogleFont("Roboto Flex")

// Creative Roboto Flex configurations for different use cases
// Display Family - Ultra expressive with dramatic weight and width variations
val displayFontFamily = FontFamily(
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Black),
)

// Body Family - Optimized for readability with subtle variations
val bodyFontFamily = FontFamily(
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Bold),
)

// Label Family - Compact and efficient for UI elements
val labelFontFamily = FontFamily(
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = robotoFlexFont, fontProvider = provider, weight = FontWeight.SemiBold),
)

// Legacy font family for backward compatibility
val fontName = GoogleFont(BuildConfig.fontName)
val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
)

// Material 3 Editorial Typography with Roboto Flex Variable Font
val Typography = Typography(
    // Display styles - Ultra expressive with dramatic impact
    displayLarge = TextStyle(
        fontSize = 96.sp,
        lineHeight = 104.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Black,
        letterSpacing = (-2.0).sp, // Tight spacing for impact
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    displayMedium = TextStyle(
        fontSize = 64.sp,
        lineHeight = 72.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1.0).sp, // Tight spacing
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    displaySmall = TextStyle(
        fontSize = 48.sp,
        lineHeight = 56.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    ),
    
    // Headlines - Expressive but readable with variable font features
    headlineLarge = TextStyle(
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    headlineMedium = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    headlineSmall = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    
    // Titles - Clean and purposeful with optimized readability
    titleLarge = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
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
    
    // Body text - Highly readable with generous spacing and optimized for long-form reading
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
    
    // Labels - Compact and efficient with condensed width for UI elements
    labelLarge = TextStyle(
        fontFamily = labelFontFamily,
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
        fontFamily = labelFontFamily,
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
        fontFamily = labelFontFamily,
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

// Creative Typography Extensions for Editorial Treatments
object EditorialTypography {
    // Ultra Impact - For hero sections and major announcements
    val ultraImpact = TextStyle(
        fontSize = 128.sp,
        lineHeight = 128.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Black,
        letterSpacing = (-3.0).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.Both
        )
    )
    
    // Flowing Headline - For expressive, artistic headlines
    val flowingHeadline = TextStyle(
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1.5).sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    
    // Compact Label - For space-constrained UI elements
    val compactLabel = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontFamily = labelFontFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    
    // Expressive Body - For creative content with personality
    val expressiveBody = TextStyle(
        fontSize = 17.sp,
        lineHeight = 26.sp,
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Light,
        letterSpacing = 0.2.sp,
        lineBreak = LineBreak.Paragraph.copy(
            wordBreak = LineBreak.WordBreak.Default
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
}
