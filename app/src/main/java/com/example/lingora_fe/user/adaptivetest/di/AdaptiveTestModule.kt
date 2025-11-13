package com.example.lingora_fe.user.adaptivetest.di

import com.example.lingora_fe.user.adaptivetest.data.remote.api.AdaptiveTestApiService
import com.example.lingora_fe.user.adaptivetest.data.repository.AdaptiveTestRepositoryImpl
import com.example.lingora_fe.user.adaptivetest.domain.repository.AdaptiveTestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdaptiveTestModule {
    
    @Provides
    @Singleton
    fun provideAdaptiveTestApiService(retrofit: Retrofit): AdaptiveTestApiService {
        return retrofit.create(AdaptiveTestApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAdaptiveTestRepository(
        apiService: AdaptiveTestApiService
    ): AdaptiveTestRepository {
        return AdaptiveTestRepositoryImpl(apiService)
    }
}

