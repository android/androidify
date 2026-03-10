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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.core.net.toUri
import com.android.developers.androidify.results.R

@Composable
fun getPlaceholderBotUri(): Uri =
    ("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${LocalContext.current.packageName}/${R.drawable.placeholderbot}").toUri()

@Composable
fun getPlaceholderBotBitmap(): Bitmap =
    ImageBitmap.imageResource(id = R.drawable.placeholderbot).asAndroidBitmap()
