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
package com.android.developers.androidify.customize

import androidx.compose.ui.geometry.Size
import com.android.developers.androidify.results.R

enum class CustomizeTool(val icon: Int, val displayName: String) {
    Size(R.drawable.size_tool_icon, "Size tool"),
    Background(R.drawable.outline_background_replace_24, "Background tool"),
}

interface ToolOption {
    val displayName: String
    val key: String
}

sealed class SizeOption(
    val aspectRatio: Float,
    val dimensions: Size,
    override val displayName: String,
    override val key: String,
) : ToolOption {

    object Square : SizeOption(1f, Size(1000f, 1000f), "1:1", "square")
    object Banner : SizeOption(4f, Size(4000f, 1000f), "Banner", "banner")
    object Wallpaper : SizeOption(9 / 16f, Size(900f, 1600f), "Wallpaper", "wallpaper")
    object SocialHeader : SizeOption(3f, Size(3000f, 1000f), "3:1", "social_header")
    object WallpaperTablet : SizeOption(1280 / 800f, Size(1280f, 800f), "Large wallpaper", "wallpaper_large")
}

sealed class BackgroundOption(
    override val displayName: String,
    override val key: String,
    val previewDrawableInt: Int?,
    val aiBackground: Boolean = false,
    val prompt: String? = null,
) : ToolOption {
    object None : BackgroundOption("None", "None", null)
    object Plain : BackgroundOption("Plain", "Plain", null)
    object Lightspeed : BackgroundOption(
        "Lightspeed",
        "Lightspeed",
        R.drawable.light_speed_dots,
    )
    object IO : BackgroundOption(
        "I/O",
        "IO",
        R.drawable.background_option_io,
    )
    object Yeehaw : BackgroundOption(
        "Yeehaw",
        "Yeehaw",
        R.drawable.yeehaw,
        aiBackground = true,
        prompt = "Add a background that has a vibe a vibrant and stylized 3D farm scene with soft colours, featuring a classic red barn set against rolling golden hills, with round hay bales in the foreground and puffy, cartoon-like trees and clouds.The overall aesthetic is very soft, clean, and cartoon-like, with a warm and inviting color palette dominated by reds, yellows, and pinks. The lighting is gentle, giving the entire scene a serene and idyllic feel. ",
    )
    object Island : BackgroundOption(
        "Island",
        "island",
        R.drawable.island,
        aiBackground = true,
        prompt =
        "Add a background that has a vibe of island, beach party style background. The style must be 3D collectable style. Vibrant and cheerful 3D digital illustration of an idyllic beach scene.\n" +
            "\n" +
            "The image features a collection of classic beach items on a sandy shore next to a calm blue ocean under a bright blue sky with a few puffy, cartoon-like clouds.The overall style is clean, bright, and whimsical, with smooth textures and a playful, holiday atmosphere.",
    )
    object Intergalactic : BackgroundOption(
        "Intergalactic",
        "Intergalactic",
        R.drawable.intergalatic,
        aiBackground = true,
        prompt =
        "Add a background that has a intergalactic vibe, A whimsical 3D alien landscape for an android bot to explore. The scene features rolling, soft-pink hills dotted with colorful, stylized mushroom-like and coral-like plants in shades of blue and orange. The background is a dreamy, pastel sky of pink and lavender, filled with soft nebulae and stars, with a large, dark planet with prominent golden rings hanging in the distance.",
    )
}
