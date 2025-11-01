package com.example.lingora_fe.user.vocabulary.di

import com.example.lingora_fe.user.vocabulary.data.remote.api.VocabularyApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VocabularyNetworkModule {

    @Provides
    @Singleton
    fun provideVocabularyApiService(retrofit: Retrofit): VocabularyApiService {
        return retrofit.create(VocabularyApiService::class.java)
    }
}

