package com.example.lingora_fe.user.vocabulary.data.datasource.remote

import com.example.lingora_fe.user.vocabulary.data.remote.api.VocabularyApiService
import com.example.lingora_fe.user.vocabulary.data.remote.dto.CategoriesResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.CategoryProgressListResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.CategoryProgressResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.CategoryResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.TopicProgressResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.TopicResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.TopicsResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.UpdateProgressRequest
import com.example.lingora_fe.user.vocabulary.data.remote.dto.WordResponse
import com.example.lingora_fe.user.vocabulary.data.remote.dto.WordsResponse
import javax.inject.Inject

class VocabularyRemoteDataSource @Inject constructor(
    private val apiService: VocabularyApiService
) {
    // Category operations
    suspend fun getCategories(): CategoriesResponse {
        return apiService.getCategories()
    }

    suspend fun getCategoryById(categoryId: Int): CategoryResponse {
        return apiService.getCategoryById(categoryId)
    }

    // Topic operations
    suspend fun getTopics(): TopicsResponse {
        return apiService.getTopics()
    }

    suspend fun getTopicById(topicId: Int): TopicResponse {
        return apiService.getTopicById(topicId)
    }

    suspend fun getTopicsByCategory(categoryId: Int): TopicsResponse {
        return apiService.getTopicsByCategory(categoryId)
    }

    // Word operations
    suspend fun getWordsByTopic(topicId: Int): WordsResponse {
        return apiService.getWordsByTopic(topicId)
    }

    suspend fun getWordById(wordId: Int): WordResponse {
        return apiService.getWordById(wordId)
    }

    // Progress operations
    suspend fun getCategoryProgress(userId: Int): CategoryProgressListResponse {
        return apiService.getCategoryProgress(userId)
    }

    suspend fun getCategoryProgressById(userId: Int, categoryId: Int): CategoryProgressResponse {
        return apiService.getCategoryProgressById(userId, categoryId)
    }

    suspend fun updateCategoryProgress(
        userId: Int,
        categoryId: Int,
        request: UpdateProgressRequest
    ): CategoryProgressResponse {
        return apiService.updateCategoryProgress(userId, categoryId, request)
    }

    suspend fun getTopicProgress(userId: Int, topicId: Int): TopicProgressResponse {
        return apiService.getTopicProgress(userId, topicId)
    }

    suspend fun updateTopicProgress(
        userId: Int,
        topicId: Int,
        request: UpdateProgressRequest
    ): TopicProgressResponse {
        return apiService.updateTopicProgress(userId, topicId, request)
    }
}

