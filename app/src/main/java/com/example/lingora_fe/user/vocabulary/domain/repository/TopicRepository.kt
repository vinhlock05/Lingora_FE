package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.TopicProgress
import com.example.lingora_fe.user.vocabulary.domain.model.Word

interface TopicRepository {
    // Topics in category with progress
    suspend fun getCategoryTopicsWithProgress(
        categoryId: Int,
        limit: Int = 20,
        page: Int = 1,
        search: String? = null,
        sort: String? = null
    ): Either<String, Pair<CategoryTopicProgressMeta, List<TopicProgress>>>
    
    // Words for study
    suspend fun getWordsForStudy(
        topicId: Int,
        count: Int
    ): Either<String, List<Word>>
}

data class CategoryTopicProgressMeta(
    val categoryId: Int,
    val name: String,
    val description: String,
    val totalTopics: Int,
    val completedTopics: Int,
    val progressPercent: Float,
    val completed: Boolean,
    val currentPage: Int,
    val totalPages: Int
)

