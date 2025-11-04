package com.example.lingora_fe.admin.di

import com.example.lingora_fe.admin.user.data.remote.api.UserManagementApiService
import com.example.lingora_fe.admin.user.data.repository.UserManagementRepositoryImpl
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideUserManagementApiService(retrofit: Retrofit): UserManagementApiService {
        return retrofit.create(UserManagementApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserManagementRepository(
        apiService: UserManagementApiService
    ): UserManagementRepository {
        return UserManagementRepositoryImpl(apiService)
    }
}

