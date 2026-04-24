package com.example.lingora_fe.user.ranking.di

import com.example.lingora_fe.user.ranking.data.remote.api.RankingApiService
import com.example.lingora_fe.user.ranking.data.repository.RankingRepositoryImpl
import com.example.lingora_fe.user.ranking.domain.repository.RankingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RankingRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRankingRepository(
        impl: RankingRepositoryImpl
    ): RankingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RankingNetworkModule {

    @Provides
    @Singleton
    fun provideRankingApiService(retrofit: Retrofit): RankingApiService {
        return retrofit.create(RankingApiService::class.java)
    }
}
