package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.local.VocabularyLocalDataSource
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.data.local.entity.TopicEntity
import com.example.lingora_fe.user.vocabulary.domain.model.Topic
import com.example.lingora_fe.user.vocabulary.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource
) : TopicRepository {

    override suspend fun getTopics(): Either<String, List<Topic>> {
        return try {
            val response = remoteDataSource.getTopics()
            val topics = response.data.map { it.toDomain() }
            
            // Cache in local database
            val entities = response.data.map { dto ->
                TopicEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    createdAt = java.sql.Timestamp.valueOf(dto.createdAt).time
                )
            }
            localDataSource.insertTopics(entities)
            
            Either.Right(topics)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localTopics = localDataSource.getTopics().map { it.toDomain() }
                Either.Right(localTopics)
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch topics")
            }
        }
    }

    override suspend fun getTopicById(topicId: Int): Either<String, Topic> {
        return try {
            val response = remoteDataSource.getTopicById(topicId)
            val topic = response.data.toDomain()
            
            // Cache in local database
            val entity = TopicEntity(
                id = response.data.id,
                name = response.data.name,
                description = response.data.description,
                createdAt = java.sql.Timestamp.valueOf(response.data.createdAt).time
            )
            localDataSource.insertTopic(entity)
            
            Either.Right(topic)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localTopic = localDataSource.getTopicById(topicId)?.toDomain()
                if (localTopic != null) {
                    Either.Right(localTopic)
                } else {
                    Either.Left("Topic not found")
                }
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch topic")
            }
        }
    }

    override suspend fun getTopicsByCategory(categoryId: Int): Either<String, List<Topic>> {
        return try {
            val response = remoteDataSource.getTopicsByCategory(categoryId)
            val topics = response.data.map { it.toDomain() }
            
            // Cache in local database
            val entities = response.data.map { dto ->
                TopicEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    createdAt = java.sql.Timestamp.valueOf(dto.createdAt).time
                )
            }
            localDataSource.insertTopics(entities)
            
            Either.Right(topics)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localTopics = localDataSource.getTopicsByCategory(categoryId).map { it.toDomain() }
                Either.Right(localTopics)
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch topics")
            }
        }
    }

    override fun observeTopicsByCategory(categoryId: Int): Flow<List<Topic>> {
        return localDataSource.observeTopicsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

