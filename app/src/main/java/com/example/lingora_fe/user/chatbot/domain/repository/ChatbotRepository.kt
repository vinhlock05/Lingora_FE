package com.example.lingora_fe.user.chatbot.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.chatbot.domain.model.ChatConversation
import com.example.lingora_fe.user.chatbot.domain.model.ChatSession
import com.example.lingora_fe.user.chatbot.domain.model.ChatSessionDetail

interface ChatbotRepository {
    suspend fun sendMessage(
        question: String,
        sessionId: String?
    ): Either<AppFailure, ChatConversation>

    suspend fun getSessions(): Either<AppFailure, List<ChatSession>>

    suspend fun getSessionMessages(sessionId: String): Either<AppFailure, ChatSessionDetail>

    suspend fun deleteSession(sessionId: String): Either<AppFailure, String>
}

