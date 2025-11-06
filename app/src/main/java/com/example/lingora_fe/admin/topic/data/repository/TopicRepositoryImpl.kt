package com.example.lingora_fe.admin.topic.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.topic.data.remote.api.TopicApiService
import com.example.lingora_fe.admin.topic.data.remote.dto.toDomain
import com.example.lingora_fe.admin.topic.data.remote.dto.toDto
import com.example.lingora_fe.admin.topic.domain.model.*
import com.example.lingora_fe.admin.topic.domain.repository.TopicRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val apiService: TopicApiService
) : TopicRepository {

    override suspend fun getAllTopics(
        token: String,
        filterOptions: TopicFilterOptions
    ): Either<AppFailure, TopicListMetadata> {
        return Either.catch {
            val response = apiService.getAllTopics(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                sort = filterOptions.sort,
                hasCategory = filterOptions.hasCategory
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getCategoryWithTopics(
        token: String,
        categoryId: Int,
        filterOptions: TopicFilterOptions
    ): Either<AppFailure, CategoryWithTopics> {
        return Either.catch {
            val response = apiService.getCategoryWithTopics(
                categoryId = categoryId,
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getTopicById(
        token: String,
        topicId: Int
    ): Either<AppFailure, Topic> {
        return Either.catch {
            val response = apiService.getTopicById(
                topicId = topicId,
                limit = 1, // Only need topic info, not words
                page = 1
            )
            // Extract only topic info from TopicWithWordsDto
            val topicWithWords = response.metaData?.toDomain() 
                ?: throw Exception(response.message)
            
            Topic(
                id = topicWithWords.id,
                name = topicWithWords.name,
                description = topicWithWords.description,
                category = topicWithWords.category,
                totalWords = topicWithWords.totalWords,
                createdAt = topicWithWords.createdAt
            )
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createTopic(
        token: String,
        topicData: CreateTopicData
    ): Either<AppFailure, Topic> {
        return Either.catch {
            val response = apiService.createTopic(topicData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateTopic(
        token: String,
        topicId: Int,
        topicData: UpdateTopicData
    ): Either<AppFailure, Topic> {
        return Either.catch {
            val response = apiService.updateTopic(topicId, topicData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteTopic(
        token: String,
        topicId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteTopic(topicId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}

