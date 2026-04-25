package com.example.lingora_fe.user.conversation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.conversation.domain.repository.ConversationRepository
import com.example.lingora_fe.user.ranking.presentation.XpRewardTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val repository: ConversationRepository,
    private val xpRewardTracker: XpRewardTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        fetchContexts(page = 1)
        fetchSessions(page = 1)
    }

    fun startSession(sessionId: String, initialPhase: String = "opening") {
        _uiState.update { it.copy(
            activeSessionId = sessionId,
            currentPhase = initialPhase,
            chatMessages = emptyList(),
            currentSuggestions = emptyList(),
            isReviewMode = false,
            isEndingSession = false,
            endedSession = null
        ) }
        fetchSessionMessages(sessionId)
    }

    private fun fetchSessionMessages(sessionId: String) {
        viewModelScope.launch {
            repository.getSessionDetail(sessionId).fold(
                ifLeft = { err -> 
                    _uiState.update { it.copy(errorMessage = "Failed to load chat history: ${err.message}") }
                },
                ifRight = { session ->
                    val isCompleted = session.status == "COMPLETED"
                    _uiState.update { state ->
                        state.copy(
                            chatMessages = session.messages ?: emptyList(),
                            currentPhase = session.currentPhase,
                            currentSuggestions = if (isCompleted) emptyList() else session.messages?.lastOrNull()?.suggestions ?: emptyList(),
                            isReviewMode = isCompleted
                        )
                    }
                }
            )
        }
    }

    fun createSession(contextId: Int, onSuccess: (String) -> Unit) {
        _uiState.update { it.copy(isSessionsLoading = true) }
        viewModelScope.launch {
            repository.createSession(contextId).fold(
                ifLeft = { err ->
                    _uiState.update { it.copy(isSessionsLoading = false, errorMessage = err.message) }
                },
                ifRight = { session ->
                    _uiState.update { it.copy(isSessionsLoading = false) }
                    startSession(session.id, session.currentPhase)
                    onSuccess(session.id)
                }
            )
        }
    }

    fun sendMessage(question: String) {
        val sessionId = _uiState.value.activeSessionId ?: return

        // Optimistically add user message
        val tempUserMsg = com.example.lingora_fe.user.conversation.domain.model.ConversationMessage(
            id = "temp_${System.currentTimeMillis()}",
            sender = "USER",
            content = question,
            corrections = null,
            improvement = null,
            vocabulary = null,
            suggestions = null,
            createdAt = "Sending..."
        )
        _uiState.update { it.copy(
            isSendingMessage = true,
            chatMessages = it.chatMessages + tempUserMsg,
            currentSuggestions = emptyList()
        ) }

        viewModelScope.launch {
            repository.sendMessage(sessionId, question).fold(
                ifLeft = { err ->
                    _uiState.update { it.copy(isSendingMessage = false, errorMessage = err.message) }
                },
                ifRight = { (session, messages) ->
                    _uiState.update { state ->
                        // Replace temp user message + append all returned messages
                        val msgs = state.chatMessages.toMutableList()
                        val idx = msgs.indexOfLast { it.id.startsWith("temp_") }
                        if (idx != -1) msgs.removeAt(idx)
                        msgs.addAll(messages)
                        val isCompleted = session.status == "COMPLETED"
                        state.copy(
                            chatMessages = msgs,
                            currentSuggestions = if (isCompleted) emptyList() else messages.lastOrNull()?.suggestions ?: emptyList(),
                            currentPhase = session.currentPhase,
                            isSendingMessage = false,
                            isReviewMode = isCompleted,
                            endedSession = if (isCompleted) session else null,
                            errorMessage = null
                        )
                    }
                }
            )
        }
    }

    fun fetchContexts(page: Int = 1, search: String? = null) {
        if (page == 1) _uiState.update { it.copy(isContextsLoading = true, contextCurrentPage = 1) }
        
        viewModelScope.launch {
            repository.getContexts(page = page, limit = 10, search = search, sort = "+sortOrder").fold(
                ifLeft = { err -> 
                    _uiState.update { it.copy(isContextsLoading = false, errorMessage = err.message) }
                },
                ifRight = { paginatedResp ->
                    _uiState.update { state ->
                        val newContexts = if (page == 1) paginatedResp.items else state.contexts + paginatedResp.items
                        state.copy(
                            isContextsLoading = false,
                            contexts = newContexts,
                            contextCurrentPage = paginatedResp.currentPage,
                            contextTotalPages = paginatedResp.totalPages,
                            hasReachedMaxContexts = paginatedResp.currentPage >= paginatedResp.totalPages,
                            errorMessage = null
                        )
                    }
                }
            )
        }
    }

    fun fetchSessions(page: Int = 1, search: String? = null) {
        if (page == 1) _uiState.update { it.copy(isSessionsLoading = true, sessionCurrentPage = 1) }

        viewModelScope.launch {
            repository.getSessions(page = page, limit = 10, search = search, sort = "-createdAt").fold(
                ifLeft = { err -> 
                    _uiState.update { it.copy(isSessionsLoading = false, errorMessage = err.message) }
                },
                ifRight = { paginatedResp ->
                    _uiState.update { state ->
                        val newSessions = if (page == 1) paginatedResp.items else state.sessions + paginatedResp.items
                        state.copy(
                            isSessionsLoading = false,
                            sessions = newSessions,
                            sessionCurrentPage = paginatedResp.currentPage,
                            sessionTotalPages = paginatedResp.totalPages,
                            hasReachedMaxSessions = paginatedResp.currentPage >= paginatedResp.totalPages,
                            errorMessage = null
                        )
                    }
                }
            )
        }
    }

    fun endSession(onSuccess: (String) -> Unit) {
        val sessionId = _uiState.value.activeSessionId ?: return
        _uiState.update { it.copy(isEndingSession = true) }

        viewModelScope.launch {
            repository.endSession(sessionId).fold(
                ifLeft = { err ->
                    _uiState.update { it.copy(isEndingSession = false, errorMessage = err.message) }
                },
                ifRight = { session ->
                    _uiState.update { it.copy(isEndingSession = false, endedSession = session) }
                    onSuccess(sessionId)
                    // Backend awards conversation XP asynchronously on CONVERSATION_ENDED.
                    xpRewardTracker.observeAfterAction(sourceActionKey = "conversation_completed")
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
