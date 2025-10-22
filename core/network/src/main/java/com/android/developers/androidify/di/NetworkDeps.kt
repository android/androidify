package com.android.developers.androidify.di

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal fun newBaseOkHttpBuilder(
    timeoutSeconds: Long = 120L,
): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
}