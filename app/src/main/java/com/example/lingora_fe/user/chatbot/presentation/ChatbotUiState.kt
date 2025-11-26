package com.example.lingora_fe.user.chatbot.presentation

import com.example.lingora_fe.user.chatbot.domain.model.ChatMessage
import com.example.lingora_fe.user.chatbot.domain.model.ChatSession

data class ChatbotUiState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isBotTyping: Boolean = false,
    val question: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val session: ChatSession? = null,
    val error: String? = null,
    val sessions: List<ChatSession> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val deletingSessionId: String? = null
)

