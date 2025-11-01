package com.example.lingora_fe.user.vocabulary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lingora_fe.user.vocabulary.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics ORDER BY createdAt DESC")
    suspend fun getAll(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getById(topicId: Int): TopicEntity?

    @Query("""
        SELECT t.* FROM topics t
        INNER JOIN category_topics ct ON t.id = ct.topicId
        WHERE ct.categoryId = :categoryId
        ORDER BY ct.orderIndex
    """)
    fun observeByCategory(categoryId: Int): Flow<List<TopicEntity>>

    @Query("""
        SELECT t.* FROM topics t
        INNER JOIN category_topics ct ON t.id = ct.topicId
        WHERE ct.categoryId = :categoryId
        ORDER BY ct.orderIndex
    """)
    suspend fun getByCategory(categoryId: Int): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>)

    @Update
    suspend fun update(topic: TopicEntity)

    @Delete
    suspend fun delete(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteById(topicId: Int)

    @Query("DELETE FROM topics")
    suspend fun deleteAll()
}

