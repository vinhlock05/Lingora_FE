package com.example.lingora_fe.user.vocabulary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserTopicProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTopicProgressDao {
    @Query("SELECT * FROM user_topic_progress WHERE userId = :userId AND topicId = :topicId")
    fun observeByUserAndTopic(userId: Int, topicId: Int): Flow<UserTopicProgressEntity?>

    @Query("SELECT * FROM user_topic_progress WHERE userId = :userId AND topicId = :topicId")
    suspend fun getByUserAndTopic(userId: Int, topicId: Int): UserTopicProgressEntity?

    @Query("SELECT * FROM user_topic_progress WHERE userId = :userId")
    suspend fun getByUser(userId: Int): List<UserTopicProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserTopicProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<UserTopicProgressEntity>)

    @Update
    suspend fun update(progress: UserTopicProgressEntity)

    @Delete
    suspend fun delete(progress: UserTopicProgressEntity)

    @Query("DELETE FROM user_topic_progress WHERE userId = :userId")
    suspend fun deleteByUser(userId: Int)

    @Query("DELETE FROM user_topic_progress")
    suspend fun deleteAll()
}

