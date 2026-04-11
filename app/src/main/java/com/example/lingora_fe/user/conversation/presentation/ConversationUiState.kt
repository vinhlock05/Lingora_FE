package com.example.lingora_fe.user.conversation.presentation

import com.example.lingora_fe.user.conversation.domain.model.ConversationContext
import com.example.lingora_fe.user.conversation.domain.model.ConversationSession

data class ConversationUiState(
    val isContextsLoading: Boolean = false,
    val isSessionsLoading: Boolean = false,
    val contexts: List<ConversationContext> = emptyList(),
    val sessions: List<ConversationSession> = emptyList(),
    val errorMessage: String? = null,
    
    // Pagination data for Contexts
    val contextCurrentPage: Int = 1,
    val contextTotalPages: Int = 1,
    val hasReachedMaxContexts: Boolean = false,
    
    // Pagination data for Sessions
    val sessionCurrentPage: Int = 1,
    val sessionTotalPages: Int = 1,
    val hasReachedMaxSessions: Boolean = false,
    
    // Search properties
    val searchQuery: String = "",
    
    // Chat specific properties
    val activeSessionId: String? = null,
    val currentPhase: String = "opening",
    val chatMessages: List<com.example.lingora_fe.user.conversation.domain.model.ConversationMessage> = emptyList(),
    val isSendingMessage: Boolean = false,
    val currentSuggestions: List<String> = emptyList(),
    val isReviewMode: Boolean = false,
    val isEndingSession: Boolean = false,
    val endedSession: com.example.lingora_fe.user.conversation.domain.model.ConversationSession? = null
)
