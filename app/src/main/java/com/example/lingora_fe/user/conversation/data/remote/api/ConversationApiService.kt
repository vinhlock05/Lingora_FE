// data/remote/api/ConversationApiService.kt
package com.example.lingora_fe.user.conversation.data.remote.api

import com.example.lingora_fe.user.conversation.data.remote.dto.*
import retrofit2.http.*

interface ConversationApiService {

    @GET("conversation/contexts")
    suspend fun getContexts(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): ContextsResponseDto

    @GET("conversation/contexts/{id}/templates")
    suspend fun getTemplates(
        @Path("id") contextId: Int
    ): TemplatesResponseDto

    @POST("conversation/sessions")
    suspend fun createSession(
        @Body request: Map<String, Int> // { "contextId": id }
    ): SessionResponseDto

    @POST("conversation/sessions/{id}/messages")
    suspend fun sendMessage(
        @Path("id") sessionId: String,
        @Body request: Map<String, String> // { "question": text }
    ): SendMessageResponseDto

    @PATCH("conversation/sessions/{id}/end")
    suspend fun endSession(
        @Path("id") sessionId: String
    ): SessionResponseDto

    @GET("conversation/sessions")
    suspend fun getSessions(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): SessionsResponseDto

    @GET("conversation/sessions/{id}")
    suspend fun getSessionDetail(
        @Path("id") sessionId: String
    ): SessionDetailResponseDto

    @DELETE("conversation/sessions/{id}")
    suspend fun deleteSession(
        @Path("id") sessionId: String
    )
}
