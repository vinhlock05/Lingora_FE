package com.example.lingora_fe.user.dictionary.di

import com.example.lingora_fe.user.dictionary.data.remote.api.TranslateApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TranslateNetworkModule {

    @Provides
    @Singleton
    fun provideTranslateApiService(retrofit: Retrofit): TranslateApiService {
        return retrofit.create(TranslateApiService::class.java)
    }
}



