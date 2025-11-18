package com.example.lingora_fe.user.forum.di

import com.example.lingora_fe.user.forum.data.remote.api.ForumApiService
import com.example.lingora_fe.user.forum.data.repository.ForumRepositoryImpl
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ForumNetworkModule {
    
    @Provides
    @Singleton
    fun provideForumApiService(retrofit: Retrofit): ForumApiService {
        return retrofit.create(ForumApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ForumRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindForumRepository(
        forumRepositoryImpl: ForumRepositoryImpl
    ): ForumRepository
}




