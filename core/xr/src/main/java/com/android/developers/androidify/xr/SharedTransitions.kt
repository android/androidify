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
package com.android.developers.androidify.xr

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Placeable
import com.android.developers.androidify.theme.LocalSharedTransitionScope

/**
 * On Android XR in Full Space Mode, spatial panels inflate into a different Window.
 * A layout using a SharedTransitionScope will fail to find shared elements across different
 * spatial panels.
 *
 * This composable replaces the LocalSharedTransitionScope with a no-op SharedTransitionScope,
 * effectively disabling the shared transitions.
 */
@Composable
fun DisableSharedTransition(content: @Composable (() -> Unit)) {
    @OptIn(ExperimentalSharedTransitionApi::class)
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides shimSharedTransitionScope(this)) {
            content()
        }
    }
}

/** A fake implementation of SharedTransitionScope that disables registering shared transitions. */
@ExperimentalSharedTransitionApi
private fun shimSharedTransitionScope(original: SharedTransitionScope): SharedTransitionScope {
    return object : SharedTransitionScope {
        override val isTransitionActive: Boolean
            get() = false

        override fun Modifier.skipToLookaheadSize(enabled: () -> Boolean) = this

        override fun Modifier.renderInSharedTransitionScopeOverlay(
            zIndexInOverlay: Float,
            renderInOverlay: () -> Boolean,
        ) = this

        override fun Modifier.sharedElement(
            sharedContentState: SharedTransitionScope.SharedContentState,
            animatedVisibilityScope: AnimatedVisibilityScope,
            boundsTransform: BoundsTransform,
            placeholderSize: SharedTransitionScope.PlaceholderSize,
            renderInOverlayDuringTransition: Boolean,
            zIndexInOverlay: Float,
            clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip,
        ) = this

        override fun Modifier.sharedBounds(
            sharedContentState: SharedTransitionScope.SharedContentState,
            animatedVisibilityScope: AnimatedVisibilityScope,
            enter: EnterTransition,
            exit: ExitTransition,
            boundsTransform: BoundsTransform,
            resizeMode: SharedTransitionScope.ResizeMode,
            placeholderSize: SharedTransitionScope.PlaceholderSize,
            renderInOverlayDuringTransition: Boolean,
            zIndexInOverlay: Float,
            clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip,
        ) = this

        override fun Modifier.sharedElementWithCallerManagedVisibility(
            sharedContentState: SharedTransitionScope.SharedContentState,
            visible: Boolean,
            boundsTransform: BoundsTransform,
            placeholderSize: SharedTransitionScope.PlaceholderSize,
            renderInOverlayDuringTransition: Boolean,
            zIndexInOverlay: Float,
            clipInOverlayDuringTransition: SharedTransitionScope.OverlayClip,
        ) = this

        override fun OverlayClip(clipShape: Shape): SharedTransitionScope.OverlayClip =
            original.OverlayClip(clipShape)

        override val Placeable.PlacementScope.lookaheadScopeCoordinates: LayoutCoordinates
            get() = with(original) { lookaheadScopeCoordinates }

        override fun LayoutCoordinates.toLookaheadCoordinates(): LayoutCoordinates {
            with(original) {
                return toLookaheadCoordinates()
            }
        }
    }
}
