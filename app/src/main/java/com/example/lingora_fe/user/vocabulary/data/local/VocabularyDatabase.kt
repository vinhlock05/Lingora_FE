package com.example.lingora_fe.user.vocabulary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lingora_fe.user.vocabulary.data.local.dao.CategoryDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.CategoryTopicDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.TopicDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.UserCategoryProgressDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.UserTopicProgressDao
import com.example.lingora_fe.user.vocabulary.data.local.dao.WordDao
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryTopicEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.TopicEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserCategoryProgressEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserTopicProgressEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.WordEntity

@Database(
    entities = [
        CategoryEntity::class,
        TopicEntity::class,
        WordEntity::class,
        CategoryTopicEntity::class,
        UserCategoryProgressEntity::class,
        UserTopicProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VocabularyDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun topicDao(): TopicDao
    abstract fun wordDao(): WordDao
    abstract fun categoryTopicDao(): CategoryTopicDao
    abstract fun userCategoryProgressDao(): UserCategoryProgressDao
    abstract fun userTopicProgressDao(): UserTopicProgressDao

    companion object {
        const val DATABASE_NAME = "vocabulary_database"
    }
}

