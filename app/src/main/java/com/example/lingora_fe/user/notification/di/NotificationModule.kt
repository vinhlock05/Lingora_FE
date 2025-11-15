package com.example.lingora_fe.user.notification.di

import com.example.lingora_fe.user.notification.data.remote.api.NotificationApiService
import com.example.lingora_fe.user.notification.data.repository.NotificationRepositoryImpl
import com.example.lingora_fe.user.notification.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationNetworkModule {

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }
}

