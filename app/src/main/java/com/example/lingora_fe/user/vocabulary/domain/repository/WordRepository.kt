package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress

interface WordRepository {
    // Words in topic with progress
    suspend fun getTopicWordsWithProgress(
        topicId: Int,
        limit: Int = 20,
        page: Int = 1,
        search: String? = null,
        hasLearned: Boolean? = null
    ): Either<String, Pair<TopicWordProgressMeta, List<WordWithProgress>>>
    
    // Words for review
    suspend fun getWordsForReview(
        limit: Int = 20,
        page: Int = 1
    ): Either<AppFailure, List<WordWithProgress>>
}

data class TopicWordProgressMeta(
    val topicId: Int,
    val totalWordsAll: Int,
    val learnedCountAll: Int,
    val completed: Boolean,
    val progressPercent: Float,
    val currentPage: Int,
    val totalPages: Int,
    val totalWordsFiltered: Int
)

data class ReviewWordsMeta(
    val page: Int,
    val limit: Int,
    val total: Int
)

