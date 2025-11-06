package com.example.lingora_fe.admin.word.data.remote.api

import com.example.lingora_fe.admin.word.data.remote.dto.WordDto
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

data class WordListMetaData(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val totalWords: Int? = null, // For topic/{id}/words response
    val words: List<WordDto>
)

data class CreateWordRequest(
    val word: String,
    val meaning: String,
    val phonetic: String?,
    val cefrLevel: String,
    val type: String,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val topicId: Int?
)

data class UpdateWordRequest(
    val word: String? = null,
    val meaning: String? = null,
    val phonetic: String? = null,
    val cefrLevel: String? = null,
    val type: String? = null,
    val example: String? = null,
    val exampleTranslation: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val topicId: Int? = null
)

interface WordApiService {
    // Standalone list of words
    @GET("word")
    suspend fun getAllWords(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("hasTopic") hasTopic: Boolean? = null,
        @Query("cefrLevel") cefrLevel: String? = null,
        @Query("type") type: String? = null
    ): ApiResponse<WordListMetaData>

    // Words in a topic
    @GET("topic/{id}/words")
    suspend fun getTopicWords(
        @Path("id") topicId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("cefrLevel") cefrLevel: String? = null,
        @Query("type") type: String? = null
    ): ApiResponse<WordListMetaData>

    @GET("word/{id}")
    suspend fun getWordById(@Path("id") wordId: Int): ApiResponse<WordDto>

    @POST("word")
    suspend fun createWord(@Body request: CreateWordRequest): ApiResponse<WordDto>

    @PATCH("word/{id}")
    suspend fun updateWord(@Path("id") wordId: Int, @Body request: UpdateWordRequest): ApiResponse<WordDto>

    @DELETE("word/{id}")
    suspend fun deleteWord(@Path("id") wordId: Int): ApiResponse<Any>
}


