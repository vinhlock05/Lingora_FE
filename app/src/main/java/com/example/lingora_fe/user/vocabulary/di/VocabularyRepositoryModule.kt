package com.example.lingora_fe.user.vocabulary.di

import com.example.lingora_fe.user.vocabulary.data.repository.CategoryRepositoryImpl
import com.example.lingora_fe.user.vocabulary.data.repository.ProgressRepositoryImpl
import com.example.lingora_fe.user.vocabulary.data.repository.TopicRepositoryImpl
import com.example.lingora_fe.user.vocabulary.data.repository.WordRepositoryImpl
import com.example.lingora_fe.user.vocabulary.domain.repository.CategoryRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.TopicRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VocabularyRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        topicRepositoryImpl: TopicRepositoryImpl
    ): TopicRepository

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl
    ): ProgressRepository
}

