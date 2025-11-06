package com.example.lingora_fe.admin.di

import com.example.lingora_fe.admin.category.data.remote.api.CategoryApiService
import com.example.lingora_fe.admin.category.data.repository.CategoryRepositoryImpl
import com.example.lingora_fe.admin.category.domain.repository.CategoryRepository
import com.example.lingora_fe.admin.topic.data.remote.api.TopicApiService
import com.example.lingora_fe.admin.topic.data.repository.TopicRepositoryImpl
import com.example.lingora_fe.admin.topic.domain.repository.TopicRepository
import com.example.lingora_fe.admin.word.data.remote.api.WordApiService
import com.example.lingora_fe.admin.word.data.repository.WordRepositoryImpl
import com.example.lingora_fe.admin.word.domain.repository.WordRepository
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

    // User Management
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

    // Category Management
    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService {
        return retrofit.create(CategoryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        apiService: CategoryApiService
    ): CategoryRepository {
        return CategoryRepositoryImpl(apiService)
    }

    // Topic Management
    @Provides
    @Singleton
    fun provideTopicApiService(retrofit: Retrofit): TopicApiService {
        return retrofit.create(TopicApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTopicRepository(
        apiService: TopicApiService
    ): TopicRepository {
        return TopicRepositoryImpl(apiService)
    }

    // Word Management
    @Provides
    @Singleton
    fun provideWordApiService(retrofit: Retrofit): WordApiService {
        return retrofit.create(WordApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        apiService: WordApiService
    ): WordRepository {
        return WordRepositoryImpl(apiService)
    }
}

