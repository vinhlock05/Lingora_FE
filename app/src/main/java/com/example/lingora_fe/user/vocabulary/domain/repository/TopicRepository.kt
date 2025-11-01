package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    suspend fun getTopics(): Either<String, List<Topic>>
    suspend fun getTopicById(topicId: Int): Either<String, Topic>
    suspend fun getTopicsByCategory(categoryId: Int): Either<String, List<Topic>>
    fun observeTopicsByCategory(categoryId: Int): Flow<List<Topic>>
}

