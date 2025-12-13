package com.example.lingora_fe.user.exam.di

import com.example.lingora_fe.user.exam.data.remote.api.ExamApiService
import com.example.lingora_fe.user.exam.data.repository.ExamRepositoryImpl
import com.example.lingora_fe.user.exam.domain.repository.ExamRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExamRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExamRepository(impl: ExamRepositoryImpl): ExamRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ExamNetworkModule {
    @Provides
    @Singleton
    fun provideExamApiService(retrofit: Retrofit): ExamApiService {
        return retrofit.create(ExamApiService::class.java)
    }
}

