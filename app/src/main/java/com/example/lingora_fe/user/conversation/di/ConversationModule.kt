package com.example.lingora_fe.user.conversation.di

import com.example.lingora_fe.user.conversation.data.remote.api.ConversationApiService
import com.example.lingora_fe.user.conversation.data.repository.ConversationRepositoryImpl
import com.example.lingora_fe.user.conversation.domain.repository.ConversationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConversationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): ConversationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ConversationNetworkModule {

    @Provides
    @Singleton
    fun provideConversationApiService(
        retrofit: Retrofit
    ): ConversationApiService {
        return retrofit.create(ConversationApiService::class.java)
    }
}
