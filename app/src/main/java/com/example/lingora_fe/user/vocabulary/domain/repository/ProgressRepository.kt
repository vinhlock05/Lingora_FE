package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.UserCategoryProgress
import com.example.lingora_fe.user.vocabulary.domain.model.UserTopicProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    suspend fun getCategoryProgress(userId: Int): Either<String, List<UserCategoryProgress>>
    suspend fun getCategoryProgressById(userId: Int, categoryId: Int): Either<String, UserCategoryProgress>
    suspend fun updateCategoryProgress(progress: UserCategoryProgress): Either<String, UserCategoryProgress>
    suspend fun completeCategoryProgress(userId: Int, categoryId: Int): Either<String, Unit>
    
    suspend fun getTopicProgress(userId: Int, topicId: Int): Either<String, UserTopicProgress>
    suspend fun updateTopicProgress(progress: UserTopicProgress): Either<String, UserTopicProgress>
    suspend fun completeTopicProgress(userId: Int, topicId: Int): Either<String, Unit>
    
    fun observeCategoryProgress(userId: Int): Flow<List<UserCategoryProgress>>
    fun observeTopicProgress(userId: Int, topicId: Int): Flow<UserTopicProgress>
}

