package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.local.VocabularyLocalDataSource
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserCategoryProgressEntity
import com.example.lingora_fe.user.vocabulary.data.local.entity.UserTopicProgressEntity
import com.example.lingora_fe.user.vocabulary.data.remote.dto.UpdateProgressRequest
import com.example.lingora_fe.user.vocabulary.domain.model.UserCategoryProgress
import com.example.lingora_fe.user.vocabulary.domain.model.UserTopicProgress
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource
) : ProgressRepository {

    override suspend fun getCategoryProgress(userId: Int): Either<String, List<UserCategoryProgress>> {
        return try {
            val response = remoteDataSource.getCategoryProgress(userId)
            val progressList = response.data.map { it.toDomain() }
            
            // Cache in local database
            val entities = response.data.map { dto ->
                UserCategoryProgressEntity(
                    id = dto.id,
                    userId = dto.userId,
                    categoryId = dto.categoryId,
                    totalTopics = dto.totalTopics,
                    progressPercent = dto.progressPercent,
                    completed = dto.completed
                )
            }
            localDataSource.insertCategoryProgressList(entities)
            
            Either.Right(progressList)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localProgress = localDataSource.getCategoryProgress(userId).map { it.toDomain() }
                Either.Right(localProgress)
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch category progress")
            }
        }
    }

    override suspend fun getCategoryProgressById(userId: Int, categoryId: Int): Either<String, UserCategoryProgress> {
        return try {
            val response = remoteDataSource.getCategoryProgressById(userId, categoryId)
            val progress = response.data.toDomain()
            
            // Cache in local database
            val entity = UserCategoryProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                categoryId = response.data.categoryId,
                totalTopics = response.data.totalTopics,
                progressPercent = response.data.progressPercent,
                completed = response.data.completed
            )
            localDataSource.insertCategoryProgress(entity)
            
            Either.Right(progress)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localProgress = localDataSource.getCategoryProgressById(userId, categoryId)?.toDomain()
                if (localProgress != null) {
                    Either.Right(localProgress)
                } else {
                    Either.Left("Category progress not found")
                }
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch category progress")
            }
        }
    }

    override suspend fun updateCategoryProgress(progress: UserCategoryProgress): Either<String, UserCategoryProgress> {
        return try {
            val request = UpdateProgressRequest(
                completed = progress.completed,
                progressPercent = progress.progressPercent
            )
            val response = remoteDataSource.updateCategoryProgress(
                progress.userId,
                progress.categoryId,
                request
            )
            val updatedProgress = response.data.toDomain()
            
            // Update local database
            val entity = UserCategoryProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                categoryId = response.data.categoryId,
                totalTopics = response.data.totalTopics,
                progressPercent = response.data.progressPercent,
                completed = response.data.completed
            )
            localDataSource.updateCategoryProgress(entity)
            
            Either.Right(updatedProgress)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to update category progress")
        }
    }

    override suspend fun completeCategoryProgress(userId: Int, categoryId: Int): Either<String, Unit> {
        return try {
            val request = UpdateProgressRequest(completed = true, progressPercent = 100f)
            val response = remoteDataSource.updateCategoryProgress(userId, categoryId, request)
            
            // Update local database
            val entity = UserCategoryProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                categoryId = response.data.categoryId,
                totalTopics = response.data.totalTopics,
                progressPercent = response.data.progressPercent,
                completed = response.data.completed
            )
            localDataSource.updateCategoryProgress(entity)
            
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to complete category progress")
        }
    }

    override suspend fun getTopicProgress(userId: Int, topicId: Int): Either<String, UserTopicProgress> {
        return try {
            val response = remoteDataSource.getTopicProgress(userId, topicId)
            val progress = response.data.toDomain()
            
            // Cache in local database
            val entity = UserTopicProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                topicId = response.data.topicId,
                totalWords = response.data.totalWords,
                completed = response.data.completed
            )
            localDataSource.insertTopicProgress(entity)
            
            Either.Right(progress)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localProgress = localDataSource.getTopicProgress(userId, topicId)?.toDomain()
                if (localProgress != null) {
                    Either.Right(localProgress)
                } else {
                    Either.Left("Topic progress not found")
                }
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch topic progress")
            }
        }
    }

    override suspend fun updateTopicProgress(progress: UserTopicProgress): Either<String, UserTopicProgress> {
        return try {
            val request = UpdateProgressRequest(completed = progress.completed)
            val response = remoteDataSource.updateTopicProgress(
                progress.userId,
                progress.topicId,
                request
            )
            val updatedProgress = response.data.toDomain()
            
            // Update local database
            val entity = UserTopicProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                topicId = response.data.topicId,
                totalWords = response.data.totalWords,
                completed = response.data.completed
            )
            localDataSource.updateTopicProgress(entity)
            
            Either.Right(updatedProgress)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to update topic progress")
        }
    }

    override suspend fun completeTopicProgress(userId: Int, topicId: Int): Either<String, Unit> {
        return try {
            val request = UpdateProgressRequest(completed = true)
            val response = remoteDataSource.updateTopicProgress(userId, topicId, request)
            
            // Update local database
            val entity = UserTopicProgressEntity(
                id = response.data.id,
                userId = response.data.userId,
                topicId = response.data.topicId,
                totalWords = response.data.totalWords,
                completed = response.data.completed
            )
            localDataSource.updateTopicProgress(entity)
            
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to complete topic progress")
        }
    }

    override fun observeCategoryProgress(userId: Int): Flow<List<UserCategoryProgress>> {
        return localDataSource.observeCategoryProgress(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeTopicProgress(userId: Int, topicId: Int): Flow<UserTopicProgress> {
        return localDataSource.observeTopicProgress(userId, topicId).map { entity ->
            entity?.toDomain() ?: UserTopicProgress(
                id = 0,
                userId = userId,
                topicId = topicId,
                totalWords = 0,
                completed = false
            )
        }
    }
}

