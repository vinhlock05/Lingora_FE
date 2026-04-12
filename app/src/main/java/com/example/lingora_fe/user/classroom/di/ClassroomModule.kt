package com.example.lingora_fe.user.classroom.di

import com.example.lingora_fe.user.classroom.data.remote.api.ClassroomApiService
import com.example.lingora_fe.user.classroom.data.repository.ClassroomRepositoryImpl
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClassroomNetworkModule {

    @Provides
    @Singleton
    fun provideClassroomApiService(retrofit: Retrofit): ClassroomApiService {
        return retrofit.create(ClassroomApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassroomRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindClassroomRepository(
        classroomRepositoryImpl: ClassroomRepositoryImpl
    ): ClassroomRepository
}
