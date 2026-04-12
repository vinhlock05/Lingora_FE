package com.example.lingora_fe.user.conversation.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.conversation.data.remote.api.ConversationApiService
import com.example.lingora_fe.user.conversation.data.remote.dto.toDomain
import com.example.lingora_fe.user.conversation.domain.model.*
import com.example.lingora_fe.user.conversation.domain.repository.ConversationRepository
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val apiService: ConversationApiService
) : ConversationRepository {

    override suspend fun getContexts(
        page: Int?, limit: Int?, search: String?, sort: String?
    ): Either<AppFailure, PaginatedResponse<ConversationContext>> {
        return try {
            val response = apiService.getContexts(page, limit, search, sort)
            val metadata = response.metaData
            val domainList = metadata.contexts.map { it.toDomain() }
            PaginatedResponse(
                currentPage = metadata.currentPage,
                totalPages = metadata.totalPages,
                total = metadata.total,
                items = domainList
            ).right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun getTemplates(contextId: Int): Either<AppFailure, List<ConversationSuggestionTemplate>> {
        return try {
            val response = apiService.getTemplates(contextId)
            response.metaData.map { it.toDomain() }.right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun createSession(contextId: Int): Either<AppFailure, ConversationSession> {
        return try {
            val request = mapOf("contextId" to contextId)
            val response = apiService.createSession(request)
            response.metaData.toDomain().right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun sendMessage(
        sessionId: String,
        question: String
    ): Either<AppFailure, Pair<ConversationSession, List<ConversationMessage>>> {
        return try {
            val request = mapOf("question" to question)
            val response = apiService.sendMessage(sessionId, request)
            val session = response.metaData.session.toDomain()
            val messages = response.metaData.messages.map { it.toDomain() }
            Pair(session, messages).right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun endSession(sessionId: String): Either<AppFailure, ConversationSession> {
        return try {
            val response = apiService.endSession(sessionId)
            response.metaData.toDomain().right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun getSessions(
        page: Int?, limit: Int?, search: String?, sort: String?
    ): Either<AppFailure, PaginatedResponse<ConversationSession>> {
        return try {
            val response = apiService.getSessions(page, limit, search, sort)
            val metadata = response.metaData
            val domainList = metadata.sessions.map { it.toDomain() }
            PaginatedResponse(
                currentPage = metadata.currentPage,
                totalPages = metadata.totalPages,
                total = metadata.total,
                items = domainList
            ).right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun getSessionDetail(sessionId: String): Either<AppFailure, ConversationSession> {
        return try {
            val response = apiService.getSessionDetail(sessionId)
            response.metaData.toDomain().right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }

    override suspend fun deleteSession(sessionId: String): Either<AppFailure, Unit> {
        return try {
            apiService.deleteSession(sessionId)
            Unit.right()
        } catch (e: Exception) {
            AppFailure.NetworkError(e.message ?: "Unknown error").left()
        }
    }
}
