package com.example.lingora_fe.user.vocabulary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lingora_fe.user.vocabulary.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE topicId = :topicId ORDER BY id")
    fun observeByTopic(topicId: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE topicId = :topicId ORDER BY id")
    suspend fun getByTopic(topicId: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getById(wordId: Int): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :wordId")
    suspend fun deleteById(wordId: Int)

    @Query("DELETE FROM words WHERE topicId = :topicId")
    suspend fun deleteByTopic(topicId: Int)

    @Query("DELETE FROM words")
    suspend fun deleteAll()
}

