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
package com.android.developers.androidify.data

import android.content.Context
import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.RemoteConfigDataSourceImpl
import com.android.developers.androidify.util.LocalFileProvider
import com.android.developers.androidify.util.LocalFileProviderImpl
import com.android.developers.androidify.vertexai.FirebaseAiDataSource
import com.android.developers.androidify.vertexai.FirebaseAiDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

/*
* Copyright 2025 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Named("IO")
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideLocalFileProvider(@ApplicationContext appContext: Context): LocalFileProvider =
        LocalFileProviderImpl(appContext)

    @Provides
    @Singleton
    fun provideRemoteConfigDataSource(): RemoteConfigDataSource = RemoteConfigDataSourceImpl()

    @Provides
    fun provideConfigProvider(remoteConfigDataSource: RemoteConfigDataSource): ConfigProvider =
        ConfigProvider(remoteConfigDataSource)

    @Provides
    @Singleton
    fun providesGeminiNanoDownloader(@ApplicationContext appContext: Context): GeminiNanoDownloader =
        GeminiNanoDownloader(appContext)

    @Provides
    @Singleton
    fun providesInternetConnectivityManager(@ApplicationContext appContext: Context): InternetConnectivityManager =
        InternetConnectivityManagerImpl(appContext)

    @Provides
    @Singleton
    fun providesFirebaseVertexAiDataSource(remoteConfigDataSource: RemoteConfigDataSource): FirebaseAiDataSource =
        FirebaseAiDataSourceImpl(remoteConfigDataSource)

    @Provides
    @Singleton
    fun providesTextGenerationRepository(
        remoteConfigDataSource: RemoteConfigDataSource,
        geminiNanoDataSource: GeminiNanoGenerationDataSource,
        firebaseAiDataSource: FirebaseAiDataSource,
    ): TextGenerationRepository =
        TextGenerationRepositoryImpl(
            remoteConfigDataSource,
            geminiNanoDataSource,
            firebaseAiDataSource,
        )

    @Provides
    @Singleton
    fun providesGeminiNanoDataSource(geminiNanoDownloader: GeminiNanoDownloader): GeminiNanoGenerationDataSource =
        GeminiNanoGenerationDataSourceImpl(geminiNanoDownloader)

    @Provides
    @Singleton
    fun imageGenerationRepository(
        remoteConfigDataSource: RemoteConfigDataSource,
        localFileProvider: LocalFileProvider,
        internetConnectivityManager: InternetConnectivityManager,
        firebaseAiDataSource: FirebaseAiDataSource,
        geminiNanoGenerationDataSource: GeminiNanoGenerationDataSource,
    ): ImageGenerationRepository = ImageGenerationRepositoryImpl(
        remoteConfigDataSource = remoteConfigDataSource,
        localFileProvider = localFileProvider,
        geminiNanoDataSource = geminiNanoGenerationDataSource,
        internetConnectivityManager = internetConnectivityManager,
        firebaseAiDataSource = firebaseAiDataSource,
    )
}
