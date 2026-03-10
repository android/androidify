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

import com.android.developers.androidify.RemoteConfigDataSource
import com.android.developers.androidify.RemoteConfigDataSourceImpl
import com.android.developers.androidify.util.LocalFileProvider
import com.android.developers.androidify.util.LocalFileProviderImpl
import com.android.developers.androidify.vertexai.FirebaseAiDataSource
import com.android.developers.androidify.vertexai.FirebaseAiDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    companion object {
        @Provides
        @Named("IO")
        fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }

    @Binds
    @Singleton
    abstract fun bindsLocalFileProvider(impl: LocalFileProviderImpl): LocalFileProvider

    @Binds
    @Singleton
    abstract fun bindsRemoteConfigDataSource(impl: RemoteConfigDataSourceImpl): RemoteConfigDataSource

    @Binds
    @Singleton
    abstract fun bindsGeminiNanoDataSource(impl: GeminiNanoGenerationDataSourceImpl): GeminiNanoGenerationDataSource

    @Binds
    @Singleton
    abstract fun bindsInternetConnectivityManager(impl: InternetConnectivityManagerImpl): InternetConnectivityManager

    @Binds
    @Singleton
    abstract fun bindsFirebaseVertexAiDataSource(impl: FirebaseAiDataSourceImpl): FirebaseAiDataSource

    @Binds
    @Singleton
    abstract fun bindsTextGenerationRepository(impl: TextGenerationRepositoryImpl): TextGenerationRepository

    @Binds
    @Singleton
    abstract fun bindsImageGenerationRepository(impl: ImageGenerationRepositoryImpl): ImageGenerationRepository

    @Binds
    @Singleton
    abstract fun bindsDropBehaviourFactory(impl: DropBehaviourFactoryImpl): DropBehaviourFactory
}
