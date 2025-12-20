package com.example.lingora_fe.user.withdrawal.di

import com.example.lingora_fe.user.withdrawal.data.remote.api.WithdrawalApiService
import com.example.lingora_fe.user.withdrawal.data.repository.WithdrawalRepositoryImpl
import com.example.lingora_fe.user.withdrawal.domain.repository.WithdrawalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt DI module for User Withdrawal repository bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WithdrawalRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWithdrawalRepository(
        impl: WithdrawalRepositoryImpl
    ): WithdrawalRepository
}

/**
 * Hilt DI module for User Withdrawal network dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WithdrawalNetworkModule {

    @Provides
    @Singleton
    fun provideWithdrawalApiService(retrofit: Retrofit): WithdrawalApiService {
        return retrofit.create(WithdrawalApiService::class.java)
    }
}
