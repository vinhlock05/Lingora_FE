package com.example.lingora_fe.user.vocabulary.data.remote.api

import com.example.lingora_fe.user.vocabulary.data.remote.dto.*
import retrofit2.http.*

interface VocabularyApiService {
    // Word Progress endpoints
    @POST("progress")
    suspend fun createWordProgress(
        @Body request: CreateWordProgressRequest
    ): CreateWordProgressResponse

    @PATCH("progress")
    suspend fun updateWordProgress(
        @Body request: UpdateWordProgressRequest
    ): CreateWordProgressResponse

    // Categories with progress
    @GET("progress/categories")
    suspend fun getCategoriesWithProgress(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null
    ): CategoryProgressListResponse

    // Topics in category with progress
    @GET("progress/categories/{id}/topics")
    suspend fun getCategoryTopicsWithProgress(
        @Path("id") categoryId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): CategoryTopicProgressResponse

    // Words for study
    @GET("progress/topics/{id}/study")
    suspend fun getWordsForStudy(
        @Path("id") topicId: Int,
        @Query("count") count: Int
    ): StudyWordsResponse

    // Words in topic with progress
    @GET("progress/topics/{id}/words")
    suspend fun getTopicWordsWithProgress(
        @Path("id") topicId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("hasLearned") hasLearned: Boolean? = null
    ): TopicWordProgressResponse

    // Words for review
    @GET("progress/review")
    suspend fun getWordsForReview(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): ReviewWordsResponse
}

