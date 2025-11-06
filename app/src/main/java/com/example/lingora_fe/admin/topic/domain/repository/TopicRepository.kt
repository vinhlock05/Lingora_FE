package com.example.lingora_fe.admin.topic.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.topic.domain.model.CategoryWithTopics
import com.example.lingora_fe.admin.topic.domain.model.CreateTopicData
import com.example.lingora_fe.admin.topic.domain.model.Topic
import com.example.lingora_fe.admin.topic.domain.model.TopicFilterOptions
import com.example.lingora_fe.admin.topic.domain.model.TopicListMetadata
import com.example.lingora_fe.admin.topic.domain.model.UpdateTopicData
import com.example.lingora_fe.core.error.AppFailure

interface TopicRepository {

    /**
     * Get all topics (standalone - not filtered by category)
     */
    suspend fun getAllTopics(
        token: String,
        filterOptions: TopicFilterOptions
    ): Either<AppFailure, TopicListMetadata>

    /**
     * Get category with its topics
     */
    suspend fun getCategoryWithTopics(
        token: String,
        categoryId: Int,
        filterOptions: TopicFilterOptions
    ): Either<AppFailure, CategoryWithTopics>

    /**
     * Get topic details by ID (with words)
     */
    suspend fun getTopicById(
        token: String,
        topicId: Int
    ): Either<AppFailure, Topic>

    /**
     * Create a new topic
     */
    suspend fun createTopic(
        token: String,
        topicData: CreateTopicData
    ): Either<AppFailure, Topic>

    /**
     * Update existing topic
     */
    suspend fun updateTopic(
        token: String,
        topicId: Int,
        topicData: UpdateTopicData
    ): Either<AppFailure, Topic>

    /**
     * Delete a topic
     */
    suspend fun deleteTopic(
        token: String,
        topicId: Int
    ): Either<AppFailure, Unit>
}

