package com.android.developers.androidify.di

import okhttp3.OkHttpClient

internal fun newOkHttp(): OkHttpClient {
    return newBaseOkHttpBuilder().build()
}