package com.example.lingora_fe.user.vocabulary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryTopicEntity

@Dao
interface CategoryTopicDao {
    @Query("SELECT * FROM category_topics WHERE categoryId = :categoryId ORDER BY orderIndex")
    suspend fun getByCategory(categoryId: Int): List<CategoryTopicEntity>

    @Query("SELECT * FROM category_topics WHERE topicId = :topicId")
    suspend fun getByTopic(topicId: Int): List<CategoryTopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryTopic: CategoryTopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categoryTopics: List<CategoryTopicEntity>)

    @Delete
    suspend fun delete(categoryTopic: CategoryTopicEntity)

    @Query("DELETE FROM category_topics WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Int)

    @Query("DELETE FROM category_topics WHERE topicId = :topicId")
    suspend fun deleteByTopic(topicId: Int)

    @Query("DELETE FROM category_topics")
    suspend fun deleteAll()
}

