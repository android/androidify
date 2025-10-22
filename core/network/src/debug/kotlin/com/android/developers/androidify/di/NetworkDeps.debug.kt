package com.android.developers.androidify.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun newOkHttp(): OkHttpClient {
    return newBaseOkHttpBuilder()
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                },
        )
        .build()
}