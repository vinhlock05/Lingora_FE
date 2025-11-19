package com.example.lingora_fe.user.studyset.di

import com.example.lingora_fe.user.studyset.data.remote.api.StudySetApiService
import com.example.lingora_fe.user.studyset.data.repository.StudySetRepositoryImpl
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StudySetRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStudySetRepository(
        studySetRepositoryImpl: StudySetRepositoryImpl
    ): StudySetRepository
}

@Module
@InstallIn(SingletonComponent::class)
object StudySetNetworkModule {

    @Provides
    @Singleton
    fun provideStudySetApiService(retrofit: Retrofit): StudySetApiService {
        return retrofit.create(StudySetApiService::class.java)
    }
}

