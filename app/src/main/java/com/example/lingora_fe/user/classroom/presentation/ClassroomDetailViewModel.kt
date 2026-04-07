package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ClassroomDetailViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val socketManager: NotificationSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val _state = MutableStateFlow(ClassroomDetailState())
    val state: StateFlow<ClassroomDetailState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            loadClassroom()
            loadLessons()
            loadQuizzes()
            loadChatHistory()
            _state.value = _state.value.copy(isLoading = false)
            joinRoomAndListenMessages()
        }
    }

    private suspend fun loadClassroom() {
        repository.getClassroomById(classroomId).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(
                    error = error.message ?: "Không thể tải thông tin lớp học"
                )
            },
            ifRight = { classroom ->
                _state.value = _state.value.copy(classroom = classroom)
            }
        )
    }

    private suspend fun loadLessons() {
        repository.getLessons(classroomId).fold(
            ifLeft = { /* non-fatal, keep existing list */ },
            ifRight = { lessons ->
                _state.value = _state.value.copy(lessons = lessons)
            }
        )
    }

    private suspend fun loadQuizzes() {
        repository.getQuizzes(classroomId).fold(
            ifLeft = { /* non-fatal, keep existing list */ },
            ifRight = { quizzes ->
                _state.value = _state.value.copy(quizzes = quizzes)
            }
        )
    }

    private suspend fun loadChatHistory() {
        _state.value = _state.value.copy(isChatLoading = true)
        repository.getChatHistory(classroomId, limit = 50).fold(
            ifLeft = { /* non-fatal */ },
            ifRight = { messages ->
                _state.value = _state.value.copy(chatMessages = messages)
            }
        )
        _state.value = _state.value.copy(isChatLoading = false)
    }

    private fun joinRoomAndListenMessages() {
        socketManager.joinClassroom(classroomId)

        viewModelScope.launch {
            socketManager.classroomMessageFlow()
                .catch { /* ignore socket errors */ }
                .collect { json ->
                    val message = parseMessageFromJson(json)
                    if (message != null) {
                        val updated = _state.value.chatMessages + message
                        _state.value = _state.value.copy(chatMessages = updated)
                    }
                }
        }
    }

    fun onChatInputChange(text: String) {
        _state.value = _state.value.copy(chatInput = text)
    }

    fun sendMessage() {
        val content = _state.value.chatInput.trim()
        if (content.isEmpty()) return

        _state.value = _state.value.copy(isSendingMessage = true, chatInput = "")
        socketManager.sendClassroomMessage(classroomId, content)
        _state.value = _state.value.copy(isSendingMessage = false)
    }

    fun selectTab(tab: Int) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun deleteLesson(lessonId: Int) {
        viewModelScope.launch {
            repository.deleteLesson(classroomId, lessonId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa bài học"
                    )
                },
                ifRight = {
                    val updated = _state.value.lessons.filterNot { it.id == lessonId }
                    _state.value = _state.value.copy(lessons = updated)
                }
            )
        }
    }

    fun deleteQuiz(quizId: Int) {
        viewModelScope.launch {
            repository.deleteQuiz(classroomId, quizId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa bài kiểm tra"
                    )
                },
                ifRight = {
                    val updated = _state.value.quizzes.filterNot { it.id == quizId }
                    _state.value = _state.value.copy(quizzes = updated)
                }
            )
        }
    }

    private fun parseMessageFromJson(json: JSONObject): ClassroomMessage? {
        return try {
            val senderJson = json.optJSONObject("sender")
            val sender = ClassroomUser(
                id = senderJson?.optInt("id") ?: 0,
                username = senderJson?.optString("username"),
                avatar = senderJson?.optString("avatar")?.takeIf { it.isNotEmpty() }
            )
            val repliedToJson = json.optJSONObject("repliedTo")
            val repliedTo = if (repliedToJson != null) parseMessageFromJson(repliedToJson) else null

            ClassroomMessage(
                id = json.optInt("id"),
                sender = sender,
                type = ClassroomMessageType.fromValue(json.optString("type", "TEXT")),
                content = json.optString("content", ""),
                attachmentUrl = json.optString("attachmentUrl").takeIf { it.isNotEmpty() },
                repliedTo = repliedTo,
                createdAt = json.optString("createdAt").takeIf { it.isNotEmpty() }?.let {
                    runCatching { Date(it) }.getOrNull()
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.leaveClassroom(classroomId)
    }
}
