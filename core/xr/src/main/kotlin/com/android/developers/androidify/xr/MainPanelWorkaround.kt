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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.xr.compose.platform.LocalSession
import androidx.xr.scenecore.scene

/*
 * A composable that hides the mainPanel when it is disposed. When placed outside of an (Application)Subspace,
 * this composable will be disposed after the Subspace, hiding the mainPanelEntity again.
 *
 * This is a temporary workaround for b/440325404, that causes the mainPanelEntity to appear when an
 * ApplicationSubspace transitions out of the composition due to a race condition when transitioning
 * the two hierarchies.
 */
@Composable
fun MainPanelWorkaround() {
    val session = LocalSession.current ?: return
    DisposableEffect(session) {
        onDispose {
            session.scene.mainPanelEntity.setEnabled(false)
        }
    }
}
