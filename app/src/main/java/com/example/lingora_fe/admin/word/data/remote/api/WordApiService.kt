package com.example.lingora_fe.admin.word.data.remote.api

import com.example.lingora_fe.admin.word.data.remote.dto.CreateWordRequest
import com.example.lingora_fe.admin.word.data.remote.dto.UpdateWordRequest
import com.example.lingora_fe.admin.word.data.remote.dto.WordDto
import com.example.lingora_fe.admin.word.data.remote.dto.WordListMetaData
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface WordApiService {
    // Standalone list of words
    @GET("words")
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
    @GET("topics/{id}/words")
    suspend fun getTopicWords(
        @Path("id") topicId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("cefrLevel") cefrLevel: String? = null,
        @Query("type") type: String? = null
    ): ApiResponse<WordListMetaData>

    @GET("words/{id}")
    suspend fun getWordById(@Path("id") wordId: Int): ApiResponse<WordDto>

    @POST("words")
    suspend fun createWord(@Body request: CreateWordRequest): ApiResponse<WordDto>

    @PATCH("words/{id}")
    suspend fun updateWord(@Path("id") wordId: Int, @Body request: UpdateWordRequest): ApiResponse<WordDto>

    @DELETE("words/{id}")
    suspend fun deleteWord(@Path("id") wordId: Int): ApiResponse<Any>

    // Dictionary lookup (public)
    @GET("words/dictionary")
    suspend fun lookupWord(
        @Query("term") term: String
    ): ApiResponse<WordDto>

    // Suggestions for autocomplete (public)
    @GET("words/suggest")
    suspend fun suggestWords(
        @Query("term") term: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<WordDto>>
}


