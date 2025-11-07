package com.example.lingora_fe.admin.topic.data.remote.api

import com.example.lingora_fe.admin.topic.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface TopicApiService {

    // Get all topics (standalone - not filtered by category)
    @GET("topics")
    suspend fun getAllTopics(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("hasCategory") hasCategory: Boolean? = null
    ): ApiResponse<TopicListMetaData>

    // Get topics in a specific category (nested view)
    @GET("categories/{id}/topics")
    suspend fun getCategoryWithTopics(
        @Path("id") categoryId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<CategoryWithTopicsDto>

    // Get topic with words (for details/edit)
    @GET("topics/{id}/words")
    suspend fun getTopicById(
        @Path("id") topicId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): ApiResponse<TopicWithWordsDto>

    @POST("topics")
    suspend fun createTopic(
        @Body request: CreateTopicRequest
    ): ApiResponse<TopicDto>

    @PATCH("topics/{id}")
    suspend fun updateTopic(
        @Path("id") topicId: Int,
        @Body request: UpdateTopicRequest
    ): ApiResponse<TopicDto>

    @DELETE("topics/{id}")
    suspend fun deleteTopic(
        @Path("id") topicId: Int
    ): ApiResponse<Any>
}

