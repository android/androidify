package com.android.developers.androidify.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

val LocalOcclusion = compositionLocalOf { mutableStateOf(false) }

