package com.example.lingora_fe.user.chatbot.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatDeleteSessionResponseDto
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatSendRequestDto
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatSendResponseDto
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatSessionMessagesResponseDto
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatSessionsResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatbotApiService {

    @POST("chat")
    suspend fun sendMessage(
        @Body request: ChatSendRequestDto
    ): ApiResponse<ChatSendResponseDto>

    @GET("chat/sessions")
    suspend fun getSessions(): ApiResponse<ChatSessionsResponseDto>

    @GET("chat/sessions/{sessionId}/messages")
    suspend fun getSessionMessages(
        @Path("sessionId") sessionId: String
    ): ApiResponse<ChatSessionMessagesResponseDto>

    @DELETE("chat/sessions/{sessionId}")
    suspend fun deleteSession(
        @Path("sessionId") sessionId: String
    ): ApiResponse<ChatDeleteSessionResponseDto>
}

