package com.example.lingora_fe.user.vocabulary.di

import android.content.Context
import androidx.room.Room
import com.example.lingora_fe.user.vocabulary.data.local.VocabularyDatabase
import com.example.lingora_fe.user.vocabulary.data.local.dao.CategoryDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.CategoryTopicDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.TopicDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.UserCategoryProgressDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.UserTopicProgressDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VocabularyDatabaseModule {

    @Provides
    @Singleton
    fun provideVocabularyDatabase(
        @ApplicationContext context: Context
    ): VocabularyDatabase {
        return Room.databaseBuilder(
            context,
            VocabularyDatabase::class.java,
            VocabularyDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: VocabularyDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTopicDao(database: VocabularyDatabase): TopicDao {
        return database.topicDao()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: VocabularyDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideCategoryTopicDao(database: VocabularyDatabase): CategoryTopicDao {
        return database.categoryTopicDao()
    }

    @Provides
    @Singleton
    fun provideUserCategoryProgressDao(database: VocabularyDatabase): UserCategoryProgressDao {
        return database.userCategoryProgressDao()
    }

    @Provides
    @Singleton
    fun provideUserTopicProgressDao(database: VocabularyDatabase): UserTopicProgressDao {
        return database.userTopicProgressDao()
    }
}

