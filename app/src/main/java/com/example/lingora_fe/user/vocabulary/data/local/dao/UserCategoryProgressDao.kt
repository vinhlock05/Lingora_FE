package com.example.lingora_fe.user.vocabulary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserCategoryProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCategoryProgressDao {
    @Query("SELECT * FROM user_category_progress WHERE userId = :userId")
    fun observeByUser(userId: Int): Flow<List<UserCategoryProgressEntity>>

    @Query("SELECT * FROM user_category_progress WHERE userId = :userId")
    suspend fun getByUser(userId: Int): List<UserCategoryProgressEntity>

    @Query("SELECT * FROM user_category_progress WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getByUserAndCategory(userId: Int, categoryId: Int): UserCategoryProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserCategoryProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<UserCategoryProgressEntity>)

    @Update
    suspend fun update(progress: UserCategoryProgressEntity)

    @Delete
    suspend fun delete(progress: UserCategoryProgressEntity)

    @Query("DELETE FROM user_category_progress WHERE userId = :userId")
    suspend fun deleteByUser(userId: Int)

    @Query("DELETE FROM user_category_progress")
    suspend fun deleteAll()
}

