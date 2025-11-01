package com.example.lingora_fe.user.vocabulary.data.remote.api

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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface VocabularyApiService {
    // Category endpoints
    @GET("api/vocabulary/categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("api/vocabulary/categories/{id}")
    suspend fun getCategoryById(@Path("id") categoryId: Int): CategoryResponse

    // Topic endpoints
    @GET("api/vocabulary/topics")
    suspend fun getTopics(): TopicsResponse

    @GET("api/vocabulary/topics/{id}")
    suspend fun getTopicById(@Path("id") topicId: Int): TopicResponse

    @GET("api/vocabulary/categories/{categoryId}/topics")
    suspend fun getTopicsByCategory(@Path("categoryId") categoryId: Int): TopicsResponse

    // Word endpoints
    @GET("api/vocabulary/topics/{topicId}/words")
    suspend fun getWordsByTopic(@Path("topicId") topicId: Int): WordsResponse

    @GET("api/vocabulary/words/{id}")
    suspend fun getWordById(@Path("id") wordId: Int): WordResponse

    // Progress endpoints
    @GET("api/vocabulary/users/{userId}/category-progress")
    suspend fun getCategoryProgress(@Path("userId") userId: Int): CategoryProgressListResponse

    @GET("api/vocabulary/users/{userId}/categories/{categoryId}/progress")
    suspend fun getCategoryProgressById(
        @Path("userId") userId: Int,
        @Path("categoryId") categoryId: Int
    ): CategoryProgressResponse

    @PUT("api/vocabulary/users/{userId}/categories/{categoryId}/progress")
    suspend fun updateCategoryProgress(
        @Path("userId") userId: Int,
        @Path("categoryId") categoryId: Int,
        @Body request: UpdateProgressRequest
    ): CategoryProgressResponse

    @GET("api/vocabulary/users/{userId}/topics/{topicId}/progress")
    suspend fun getTopicProgress(
        @Path("userId") userId: Int,
        @Path("topicId") topicId: Int
    ): TopicProgressResponse

    @PUT("api/vocabulary/users/{userId}/topics/{topicId}/progress")
    suspend fun updateTopicProgress(
        @Path("userId") userId: Int,
        @Path("topicId") topicId: Int,
        @Body request: UpdateProgressRequest
    ): TopicProgressResponse
}

