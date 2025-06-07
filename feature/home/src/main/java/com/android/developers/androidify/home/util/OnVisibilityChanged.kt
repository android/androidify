package com.android.developers.androidify.home.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onLayoutRectChanged

/**
 * A modifier that monitors the visibility state of a composable within specified container bounds.
 *
 * This modifier observes layout changes and determines whether the composable is fully visible
 * within the given container dimensions. The visibility check is performed by ensuring all edges
 * of the composable are within the container boundaries.
 *
 * @param containerWidth The width of the container in pixels. The composable must be fully
 *                       within this horizontal boundary to be considered visible.
 * @param containerHeight The height of the container in pixels. The composable must be fully
 *                        within this vertical boundary to be considered visible.
 * @param onChanged A callback function that is invoked whenever the visibility state changes.
 *                  Receives `true` when the composable becomes fully visible within the container
 *                  bounds, and `false` when it moves outside these bounds.
 *
 * @return A [Modifier] that can be chained with other modifiers.
 *
 * **Visibility Criteria:**
 * A composable is considered visible when:
 * - Its top edge is greater than 0
 * - Its bottom edge is less than the container height
 * - Its left edge is greater than 0
 * - Its right edge is less than the container width
 *
 * **Performance Notes:**
 * - Layout changes are throttled to 100ms to optimize performance
 * - No debouncing is applied (debounceMillis = 0)
 *
 * **Example Usage:**
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .onVisibilityChanged(
 *             containerWidth = screenWidth,
 *             containerHeight = screenHeight
 *         ) { isVisible ->
 *             if (isVisible) {
 *                 // Handle when item becomes visible
 *             }
 *         }
 * )
 * ```
 */
internal fun Modifier.onVisibilityChanged(
    containerWidth: Int,
    containerHeight: Int,
    onChanged: (visible: Boolean) -> Unit,
) = this then Modifier.onLayoutRectChanged(
    throttleMillis = 100,
    debounceMillis = 0,
) { relativeLayoutBounds ->
    onChanged(
        relativeLayoutBounds.boundsInRoot.top > 0 &&
                relativeLayoutBounds.boundsInRoot.bottom < containerHeight &&
                relativeLayoutBounds.boundsInRoot.left > 0 &&
                relativeLayoutBounds.boundsInRoot.right < containerWidth,
    )
}