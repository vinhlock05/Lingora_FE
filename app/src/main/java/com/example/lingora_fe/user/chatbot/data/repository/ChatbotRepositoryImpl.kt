package com.example.lingora_fe.user.chatbot.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.chatbot.data.remote.api.ChatbotApiService
import com.example.lingora_fe.user.chatbot.data.remote.dto.ChatSendRequestDto
import com.example.lingora_fe.user.chatbot.data.remote.dto.toDomain
import com.example.lingora_fe.user.chatbot.domain.model.ChatConversation
import com.example.lingora_fe.user.chatbot.domain.model.ChatSession
import com.example.lingora_fe.user.chatbot.domain.model.ChatSessionDetail
import com.example.lingora_fe.user.chatbot.domain.repository.ChatbotRepository
import javax.inject.Inject

class ChatbotRepositoryImpl @Inject constructor(
    private val apiService: ChatbotApiService
) : ChatbotRepository {

    override suspend fun sendMessage(
        question: String,
        sessionId: String?
    ): Either<AppFailure, ChatConversation> {
        return Either.catch {
            val response = apiService.sendMessage(
                ChatSendRequestDto(
                    question = question,
                    sessionId = sessionId
                )
            )
            response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getSessions(): Either<AppFailure, List<ChatSession>> {
        return Either.catch {
            val response = apiService.getSessions()
            response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getSessionMessages(
        sessionId: String
    ): Either<AppFailure, ChatSessionDetail> {
        return Either.catch {
            val response = apiService.getSessionMessages(sessionId)
            response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteSession(sessionId: String): Either<AppFailure, String> {
        return Either.catch {
            val response = apiService.deleteSession(sessionId)
            response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
        }.mapLeft { it.toAppFailure() }
    }
}

