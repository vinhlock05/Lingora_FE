package com.example.lingora_fe.user.dictionary.di

import com.example.lingora_fe.user.dictionary.data.repository.TranslateRepositoryImpl
import com.example.lingora_fe.user.dictionary.domain.repository.TranslateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslateRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTranslateRepository(
        impl: TranslateRepositoryImpl
    ): TranslateRepository
}



