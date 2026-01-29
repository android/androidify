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
package com.android.developers.androidify.util

import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.PIXEL_3A_XL
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7_PRO
import androidx.compose.ui.tooling.preview.Devices.PIXEL_FOLD
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview

@Preview(device = PIXEL_7_PRO, name = "Phone preview")
@Preview(widthDp = 891, heightDp = 411, name = "Phone landscape preview")
annotation class PhonePreview

@Preview(device = PIXEL_3A_XL, name = "Phone small preview", heightDp = 300, widthDp = 500)
annotation class SmallPhonePreview

@Preview(device = PIXEL_FOLD, name = "Foldable preview")
annotation class FoldablePreview

@Preview(device = PIXEL_TABLET, name = "Tablet preview")
annotation class TabletPreview

@Preview(device = DESKTOP, name = "Desktop preview")
annotation class DesktopPreview

@FoldablePreview
@TabletPreview
@DesktopPreview
annotation class LargeScreensPreview

@PhonePreview
@LargeScreensPreview
annotation class AdaptivePreview
