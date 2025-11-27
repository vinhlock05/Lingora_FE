package com.example.lingora_fe.user.chatbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.chatbot.domain.model.ChatMessage
import com.example.lingora_fe.user.chatbot.domain.model.ChatSender
import com.example.lingora_fe.user.chatbot.domain.repository.ChatbotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: ChatbotRepository,
    tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatbotUiState(
            isAuthenticated = tokenManager.isLoggedIn()
        )
    )
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.isAuthenticated) {
            loadSessions(fetchLatest = true)
        }
    }

    fun onQuestionChange(value: String) {
        _uiState.update { it.copy(question = value) }
    }

    fun sendMessage() {
        val currentQuestion = _uiState.value.question.trim()
        if (currentQuestion.isEmpty() || _uiState.value.isSending) return

        val previousMessages = _uiState.value.messages
        val tempMessage = ChatMessage(
            id = "local-${System.currentTimeMillis()}",
            content = currentQuestion,
            sender = ChatSender.USER,
            createdAt = Instant.now().toString()
        )

        _uiState.update {
            it.copy(
                isSending = true,
                isBotTyping = true,
                error = null,
                question = "",
                messages = previousMessages + tempMessage
            )
        }

        viewModelScope.launch {
            repository.sendMessage(
                question = currentQuestion,
                sessionId = _uiState.value.session?.id
            ).fold(
                ifLeft = { failure ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            isBotTyping = false,
                            error = failure.message,
                            messages = previousMessages
                        )
                    }
                },
                ifRight = { conversation ->
                    val mergedMessages = (previousMessages + conversation.messages).distinctBy { it.id }
                    val updatedSessions = _uiState.value.sessions.toMutableList().apply {
                        val sessionIndex = indexOfFirst { it.id == conversation.session.id }
                        if (sessionIndex >= 0) {
                            removeAt(sessionIndex)
                        }
                        add(0, conversation.session)
                    }
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            isBotTyping = false,
                            messages = mergedMessages,
                            session = conversation.session,
                            error = null,
                            sessions = updatedSessions
                        )
                    }
                }
            )
        }
    }

    fun loadSessions(fetchLatest: Boolean = false) {
        if (!_uiState.value.isAuthenticated) return

        _uiState.update { it.copy(isHistoryLoading = true, error = null) }

        viewModelScope.launch {
            repository.getSessions().fold(
                ifLeft = { failure ->
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            error = failure.message
                        )
                    }
                },
                ifRight = { sessions ->
                    _uiState.update {
                        it.copy(
                            sessions = sessions,
                            isHistoryLoading = false
                        )
                    }
                    if (fetchLatest) {
                        sessions.firstOrNull()?.let { session ->
                            loadSessionMessages(session.id)
                        }
                    }
                }
            )
        }
    }

    fun loadSessionMessages(sessionId: String, closeHistoryDialog: Boolean = true) {
        _uiState.update { it.copy(isLoading = true, error = null, isBotTyping = false) }

        viewModelScope.launch {
            repository.getSessionMessages(sessionId).fold(
                ifLeft = { failure ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = failure.message,
                            showHistoryDialog = if (closeHistoryDialog) false else it.showHistoryDialog
                        )
                    }
                },
                ifRight = { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = detail.session,
                            messages = detail.messages,
                            isBotTyping = false,
                            showHistoryDialog = if (closeHistoryDialog) false else it.showHistoryDialog
                        )
                    }
                }
            )
        }
    }

    fun toggleHistoryDialog(show: Boolean) {
        if (!_uiState.value.isAuthenticated) return

        if (show && _uiState.value.sessions.isEmpty()) {
            loadSessions(fetchLatest = false)
        }
        _uiState.update { it.copy(showHistoryDialog = show) }
    }

    fun startNewSession() {
        _uiState.update {
            it.copy(
                session = null,
                messages = emptyList(),
                showHistoryDialog = false,
                isBotTyping = false
            )
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.update { it.copy(deletingSessionId = sessionId, error = null) }

        viewModelScope.launch {
            repository.deleteSession(sessionId).fold(
                ifLeft = { failure ->
                    _uiState.update {
                        it.copy(
                            deletingSessionId = null,
                            error = failure.message
                        )
                    }
                },
                ifRight = {
                    _uiState.update {
                        val updatedSessions = it.sessions.filterNot { session -> session.id == sessionId }
                        val isCurrentSession = it.session?.id == sessionId
                        it.copy(
                            deletingSessionId = null,
                            sessions = updatedSessions,
                            session = if (isCurrentSession) null else it.session,
                            messages = if (isCurrentSession) emptyList() else it.messages,
                            showHistoryDialog = it.showHistoryDialog && updatedSessions.isNotEmpty()
                        )
                    }
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

