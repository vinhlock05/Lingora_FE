// domain/repository/ConversationRepository.kt
package com.example.lingora_fe.user.conversation.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.conversation.domain.model.*

interface ConversationRepository {
    suspend fun getContexts(page: Int?, limit: Int?, search: String?, sort: String?): Either<AppFailure, PaginatedResponse<ConversationContext>>
    
    suspend fun getTemplates(contextId: Int): Either<AppFailure, List<ConversationSuggestionTemplate>>
    
    suspend fun createSession(contextId: Int): Either<AppFailure, ConversationSession>
    
    suspend fun sendMessage(
        sessionId: String, 
        question: String
    ): Either<AppFailure, Pair<ConversationSession, List<ConversationMessage>>>
    
    suspend fun endSession(sessionId: String): Either<AppFailure, ConversationSession>
    
    suspend fun getSessions(page: Int?, limit: Int?, search: String?, sort: String?): Either<AppFailure, PaginatedResponse<ConversationSession>>
    
    suspend fun getSessionDetail(sessionId: String): Either<AppFailure, ConversationSession>
    
    suspend fun deleteSession(sessionId: String): Either<AppFailure, Unit>
}
