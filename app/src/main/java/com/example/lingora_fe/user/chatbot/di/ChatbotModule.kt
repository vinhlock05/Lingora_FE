package com.example.lingora_fe.user.chatbot.di

import com.example.lingora_fe.user.chatbot.data.remote.api.ChatbotApiService
import com.example.lingora_fe.user.chatbot.data.repository.ChatbotRepositoryImpl
import com.example.lingora_fe.user.chatbot.domain.repository.ChatbotRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatbotRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatbotRepository(
        impl: ChatbotRepositoryImpl
    ): ChatbotRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ChatbotNetworkModule {

    @Provides
    @Singleton
    fun provideChatbotApiService(
        retrofit: Retrofit
    ): ChatbotApiService {
        return retrofit.create(ChatbotApiService::class.java)
    }
}

