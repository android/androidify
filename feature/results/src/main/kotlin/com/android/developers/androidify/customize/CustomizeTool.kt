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

    object Square : SizeOption(1f, Size(2000f, 2000f), "1:1", "square")
    object Banner : SizeOption(4f, Size(4000f, 1000f), "Banner", "banner")
    object Wallpaper : SizeOption(9 / 16f, Size(900f, 1600f), "Wallpaper", "wallpaper")
    object SocialHeader : SizeOption(3f, Size(3000f, 1000f), "3:1", "social_header")
    object WallpaperTablet : SizeOption(1280 / 800f, Size(1280f, 800f), "Large wallpaper", "wallpaper_large")

    object Sticker : SizeOption(1f, Size(2000f, 2000f), "Sticker", "sticker")
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
    object MusicLover : BackgroundOption(
        "Music lover",
        "music",
        R.drawable.background_option_music_lover,
        aiBackground = true,
        prompt = """
            This is a soft, vibrant 3D illustration of a minimalist outdoor DJ stage setup, rendered with a meticulous blend of realism and rounded, toy-like objects, creating a clean aesthetic. The entire scene is characterized by subtle glossiness, and soft, even lighting that casts dynamic shadows, beautifully emphasizing the 3D form and depth of the objects. In the far distance, behind the stage, is rows of stadium seats and cheering crowds that appear blurry. The scene is of a night show.

     The centerpiece is a DJ stage constructed from a metallic truss system, forming a rectangular frame that supports a black canopy overhead. Two large, colorful speaker stacks are standing on either side of the stage. The front of the stage features a large, vibrant LED screen displaying abstract patterns. The DJ booth is empty.

     The scene appears to be at night, with lasers and a strobe lights illuminating the scene. A shallow depth of field keeps the stage and its equipment in sharp focus. The foreground is a deliberate blank space, with only the clean, simple ground surface visible, offering an open area for a future character or object to be added. The overall atmosphere is quiet and calm, with warm, natural light creating long, dynamic shadows that enhance the 3D rendering.

     Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

     Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )
    object PoolMaven : BackgroundOption(
        "Pool maven",
        "pool",
        R.drawable.background_option_pool,
        aiBackground = true,
        prompt = """
            A vibrant, soft 3D illustration of a serene and sun-drenched minimalist swimming pool. The entire scene is rendered with a meticulous blend of realism and rounded, toy-like objects, creating a clean, minimalist aesthetic. Every element is characterized by smooth surfaces, subtle glossiness, and soft, even lighting that creates dynamic shadows, beautifully emphasizing the 3D form and depth of the objects. The scene features an array of vibrant colors, and has a whimsical, playful feel to it.

            The centerpiece is a bean-shaped swimming pool, where the water shimmers with a tranquil, light blue hue, its surface rendered with soft forms and gentle ripples. The tiles around the pool are a light pink color. Floating nearby is a bright, rounded, playful pool floaty that is shaped like an animal. On the sleek, polished tiled deck, a pair of oversized, stylized swimming goggles. There is a playful slide that is on one side of the pool.

            In the extremely blurred background, we can see only vague, indistinct forms of minimalist elements, such as a sleek lounge chair, a simple side table, and a potted palm plant with smooth, rounded leaves. The scene is illuminated by a warm, natural light from the sun, creating a peaceful and inviting atmosphere. A horizontal horizon line distinctly separates the pool scene from the blank blue sky above.

            A shallow depth of field is intensely applied, bringing the pool and props into razor-sharp focus while the background elements appear maximally blurred, almost abstract. The foreground is a deliberate blank space, with just the polished tile floor visible, offering a clean, open area for a future character or object to be added to the scene.

            Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition. The foreground has the same pink tiles that surround the pool.

            Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )

    object SoccerFanatic : BackgroundOption(
        "Soccer fanatic",
        "soccer",
        R.drawable.background_option_soccer,
        aiBackground = true,
        prompt = """
            A 3D illustration of a minimalist/clean soccer stadium, depicted from the center of the field looking towards a goal. The entire scene is a 3D rendering where all elements are crafted with soft forms, rounded edges, and smooth surfaces.

  The field itself is a lush, vibrant green. A perfectly spherical, classic black and white soccer ball, with subtle glossiness, rests in the composition.

  In the midground, the white lines of the penalty box and the soccer goal are simplified and rounded. The goal's frame and netting are also rendered with soft forms and a gentle sheen.

  The stadium stands in the background are abstract and softly formed, with many brights colors and flags, as if it is full of excited fans. There is intentional blurring due to a shallow depth of field, creating a pleasing bokeh effect.

  There is blank blue sky visible at the top of the image, a horizontal horizon line is viaible in the middle of the image, created by where the edge of the pitch meets the stadium.

  Dynamic shadows are strategically placed beneath the ball and goal, adding visual interest and grounding these objects within the space, despite the soft lighting. In the immediate foreground, a deliberate blank space of grass if left, perfectly framed and inviting, ready for an object to be placed there. The overall aesthetic is a compelling mixture of realism in its precise rendering and the whimsical, toy-like style suggested by the keywords.

  Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

  Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )
    object StarGazer : BackgroundOption(
        "StarGazer",
        "star",
        R.drawable.background_option_stargazer,
        aiBackground = true,
        prompt = """
            A vibrant, soft 3D illustration of a minimalist stargazing setup. The scene is a mixture of realism and rounded, toy-like objects, rendered in a smooth, clean, and minimalist style. The space is characterized by smooth surfaces, subtle glossiness, and soft, even lighting that casts dynamic shadows, beautifully emphasizing the 3D form and depth.

  The is a small, stylized telescope with soft forms and rounded edges, resting on a clean, grassy hill. Next to it, a few props—such as a cozy blanket and a thermos—are arranged neatly. The atmosphere is tranquil and peaceful, with a palette of deep, vibrant colors against the night sky. There are stars and nebula in the nights sky, the night sky looks like something you might see from the Hubble space telescope.

 A shallow depth of field is used to keep the telescope and props in sharp focus. The foreground is a blank space, with only the grass visible, ready for a character or another object to be added later.

   Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

   Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )

    object FitnessBuff : BackgroundOption(
        "Fitness buff",
        "fitness",
        R.drawable.background_option_fitness,
        aiBackground = true,
        prompt = """
            A soft, vibrant 3D illustration depicts a simplified, whimsical Synthwave Sweat Sanctuary fitness studio scene. Rounded, brightly colored objects define the smooth, clean, and minimalist aesthetic. The scene has an overall whimsical and playful feeling. The lighting is highly dynamic and dramatic, similar to a Pixar film, with strong, directional key lights creating crisp highlights and deep, expressive shadows that beautifully sculpt the 3D forms and emphasize depth. Contrasting colors in the lighting scheme add visual interest and warmth to the scene.

    The centerpiece is a clean, oversized aerobics stage ts surface stretching expansively into the background. Scattered minimally on its surface are iridescent, futuristic leg warmers and shimmering sweatbands, along with an oversized, bouncy boomboxes with built-in light shows and glowing protein shakers. Alongside these, a few sleek, brightly colored chrome dumbbells with integrated LED light strips and vibrant exercise step platforms. A shallow depth of field keeps these items sharp, while the background is a softly blurred, solid colored wall resembling a giant, glowing synthwave grid (e.g., deep purple to electric blue) a gentle backdrop, subtly illuminated by the same dynamic lighting.

    The immediate foreground is a blank, clean section of the glowing neon aerobics stage's surface, where the play of light and shadow creates intriguing patterns.

    Captured from a very low, zoomed-out angle, the scene emphasizes the vast aerobics stage and its contents, creating profound depth. Objects appear much smaller relative to the wide composition, subtly placed at the edges to leave the center foreground clear. Make sure that no characters appear in the scene, and that no objects are given eyes and mouths. There should be a clear horizon line close to the center of the composition where the floor meets the back wall.

    Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the aerobics stage and its contents. This low perspective emphasizes the expanse of the stage's surface, which appears to stretch far into the distance, creating a profound sense of depth and length. Place the scene items towards the edges of the composition, ensuring that the vast middle foreground remains clear and open.
        """.trimIndent(),
    )

    object Fandroid : BackgroundOption(
        "Fandroid",
        "fandroid",
        R.drawable.background_option_fandroid,
        aiBackground = true,
        prompt = """
            Get ready for a burst of energy with this soft, vibrant 3D illustration of a minimalist Google headquarters entrance scene to a beautiful large office park building. The composition, with rounded, toy-like objects, features chairs and android statues with a big Google logo sign on a manicured grassy and sidewalk area. Subtle glossiness and soft, even lighting cast dynamic shadows, beautifully emphasizing the 3D form and depth of the space and environment. The scene has an overall and playful feeling to it, objects in the scene are brightly colored and elaborate, almost unreal.

       The star of the show is the grass flooring. This isn't just any entrance area; its fun an overly large almost giant.

        A super shallow depth of field keeps all the elements in razor-sharp focus. The foreground is like a welcoming blank canvas of statues and entrance structures on a mix of grass and sidewalk.

    This 3D illustration portrays a whimsical outdoor scene, animated by a collection of toy-like, rounded Android mascots and dessert-themed figures, all rendered with vibrant colors, soft forms, and smooth surfaces. The 3D rendering imbues them with a subtle glossiness and a minimalist/clean, polished appearance.

     The scene is bathed in soft, even lighting, which, coupled with dynamic shadows, effectively emphasizes the 3D form and depth of each figure. Among the playful sculptures, from left to right, are a towering white soft-serve ice cream swirl with colorful berries at its base, a large brown donut adorned with sprinkles, a gingerbread man-like figure, a large orange and yellow archway resembling headphones (with a honeycomb pattern visible within), and another brown Android statue. In the foreground on the right, a prominent bright green Android robot holds a white, marshmallow-like object.

     These figures are set on a meticulously grass, while the background, composed of lush trees and subtle building outlines, is depicted with a gentle depth of field, appearing slightly blurry to direct focus onto the foreground figures. A deliberate blank grass space in the immediate foreground suggests an inviting spot for an additional character or object, seamlessly integrating into this charming blend of realism and 3D illustration.

     Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

       Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )

    object GreenThumb : BackgroundOption(
        displayName = "Green thumb",
        key = "green_thumb",
        previewDrawableInt = R.drawable.background_option_greenthumb,
        aiBackground = true,
        prompt = """
            A vibrant, 3D illustration of a vibrant outdoor garden with fun plants. The flowers in this scene have a alien-like quality to them, and are brightly colored. The entire scene is rendered with a meticulous mixture of rounded, toy-like objects, creating a clean, minimalist aesthetic. Every element is characterized by smooth surfaces, subtle glossiness, and bright lighting that casts dynamic shadows, except for the ground which is covered in grass, beautifully emphasizing their 3D form and depth.

  There are rounded pots, smooth ceramic planters with cascading colorful unnatural looking greenery. A few gardening tools with toy-like appearances—like a small watering can with a soft form and a miniature trowel. There are only a few items in the scene, giving it a restrained minimal feel.  There is a white picket fence in the distance.

  A shallow depth of field is used to keep the central grouping of plants and gardening tools in sharp focus, while the abundant plants and subtle room details in the background are slightly blurry. The foreground is a deliberate blank space, with only the clean, simple floor visible, offering a clean, open area for a future character or object to be added.

  Crucially, the scene is captured from a very low camera angle, almost at ground level, significantly zoomed out to showcase a much wider view of the space. This low perspective emphasizes the expanse of the ground, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

  Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )

    object Gamer : BackgroundOption(
        displayName = "Gamer",
        key = "gamer",
        previewDrawableInt = R.drawable.background_option_gamer,
        aiBackground = true,
        prompt = """
            Craft a vibrant 3D rendering of a pixelated, retro-futuristic video gaming setup scene that is both minimalist and clean, with a toy-like aesthetic. The composition should feature a living room corner with a gaming chair made of giant, soft-edged pixels, a headset, and various oversized snacks, all constructed with soft forms, rounded edges, and smooth surfaces. Use a palette of vibrant, neon arcade colors (electric blues, vivid purples, glowing greens) with a subtle glossiness to emphasize the 3D form and depth of the gaming equipment and items.

   Include various devices with different screen sizes, like a computer screen, tablet and mobile phone.

    The scene appears to be at night with the lights off, light coming from screens displaying shimmering, low-resolution landscapes, with laser beams forming geometric patterns and strobe lights pulsating like an old CRT television refresh rate illuminating the scene. Illumination should be soft, even lighting, creating dynamic shadows that further highlight the depth of the scene. The overall look should feel smooth and polished. The 3D illustration should have a depth of field, with the background, a blurry, glitching digital landscape, slightly blurry to draw focus. Leave a blank space in the foreground, as if a character or object could be placed there (but leave it blank for now). The style should be a compelling mixture of realism and the described artistic keywords.

    Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the room/setup. This low perspective emphasizes the expanse of the floor, which appears to stretch far into the distance, creating a profound sense of depth and length. The objects in the scene should appear much smaller in relation to the overall composition.

    Place the scene items, to the edges of the composition, so that when an object is placed in the middle of the foreground, it does not completely cover what's behind it.
        """.trimIndent(),
    )
    object Jetsetter : BackgroundOption(
        displayName = "Jetsetter",
        key = "jetsetter",
        previewDrawableInt = R.drawable.background_option_jetsetter,
        aiBackground = true,
        prompt = """
            A soft, vibrant 3D illustration depicts a simplified scene featuring a whimsical, but not fantastical scene of 2 planes on the tarmac with luggage. They are facing each other with golden hour, warm, ethereal lighting that bathes the cloud station’s interior. TIt feels like a dream meticulously designed for explorers of the sky, where cloud-ships always depart on time.

  The tarmac and its surroundings are rendered with a meticulous blend of fluffy realism and rounded, toy-like objects, creating a clean aesthetic. The entire scene is characterized by subtle glossiness and soft, even lighting that casts dynamic shadows, beautifully emphasizing the 3D form and depth of the area Polished, dew-kissed surfaces reflect the warm, ethereal sunlight.

  A collection of brightly colored, stylized luggage — including vibrant bags, deep blue cloud-shaped carry-ons, and sunny yellow star-shaped duffel bags — sit neatly arranged. The foreground is a deliberate blank space, with only the clean, subtly glowing cloud floor visible, offering an open area for a future character or object to be added, perhaps a traveler waiting for their destination. The overall atmosphere is serene and anticipatory, with the warm, ethereal light creating long, dynamic shadows that enhance the 3D rendering.

  Crucially, the scene is captured from a very low camera angle, almost at ground level, significantly zoomed out to showcase a much wider view of the waiting area. This low perspective emphasizes the expansive cloud station, creating a profound sense of depth and scale. The area above the horizon line is clean, open, and uncluttered, emphasizing the vastness of the sky. The items and the colorful luggage appear smaller in relation to the overall composition, positioned slightly to the edges to allow the sweeping view of the planes and sky above, ensuring that the middle foreground remains clear and open.
        """.trimIndent(),
    )

    object Chef : BackgroundOption(
        "Masterchef",
        "chef",
        R.drawable.background_option_chef,
        aiBackground = true,
        prompt = """
            A soft, vibrant 3D illustration depicts a surreal, bubbling pasta scene in a bright kitchen. Rounded, brightly colored, toy-like objects define the smooth, clean, and minimalist aesthetic. The scene has an overall whimsical and playful feeling. The lighting is highly dynamic, with strong, directional key lights creating crisp highlights and expressive shadows that beautifully sculpt the 3D forms and emphasize depth. Bright colors in the lighting scheme add visual interest and warmth to the scene.

 On its surface are cartoon shaped meat, vibrant and playful pancakes, along with a giant block of cheese, colored pots and pans and a banana and pineapple. There is a single cupcake in the scene. A shallow depth of field keeps these items sharp, while the background is a gentle backdrop, subtly illuminated by the same dynamic lighting. Remember to only include a few items to keep the scene simple.  There shouldn't be too many items or clutter in the scene. It feels restrained.

           The immediate foreground is a blank, clean section of marble-like work surface, where the play of light and shadow creates intriguing patterns.

           Captured from a very low, zoomed-out angle, the scene emphasizes the vast work surface and its contents, creating profound depth. Objects appear much smaller relative to the wide composition, subtly placed at the edges to leave the center foreground and upper area clear, creating a parting in the middle of the scene. No characters appear, and no objects are given eyes and mouths.

           Crucially, the scene is captured from a very low camera angle, almost at floor level, significantly zoomed out to showcase a much wider view of the work surface and its contents. This low perspective emphasizes the expanse of the surface, which appears to stretch far into the distance, creating a profound sense of depth and length. Place the scene items towards the edges of the composition, ensuring that the vast middle foreground remains clear and open.
        """.trimIndent(),
    )
}
