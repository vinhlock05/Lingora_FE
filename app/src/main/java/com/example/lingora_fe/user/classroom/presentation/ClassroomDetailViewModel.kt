package com.example.lingora_fe.user.classroom.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
import com.example.lingora_fe.util.FileUploadHelper
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
    private val socketManager: com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }
    
    private var socketCollectionJob: kotlinx.coroutines.Job? = null

    private val _state = MutableStateFlow(ClassroomDetailState())
    val state: StateFlow<ClassroomDetailState> = _state.asStateFlow()

    init {
        val userId = tokenManager.getUserId()
        _state.value = _state.value.copy(currentUserId = userId)
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            loadClassroom()
            loadLessons()
            loadQuizzes()
            loadMembers()
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

    private suspend fun loadMembers() {
        _state.value = _state.value.copy(isMembersLoading = true)
        val currentUserId = _state.value.currentUserId
        repository.getMembers(classroomId, status = null).fold(
            ifLeft = { /* non-fatal, keep existing list */ },
            ifRight = { members ->
                // Determine current user's role
                val userMember = members.find { it.user.id == currentUserId }
                var role = userMember?.role?.value
                
                // If it's the teacher, force ASSISTANT role for UI purposes (or keep original)
                // Actually, the teacher is often the one who created it.
                if (_state.value.classroom?.teacher?.id == currentUserId) {
                    role = "ASSISTANT" // Assuming ASSISTANT is the teacher/admin role for this UI
                }
                
                _state.value = _state.value.copy(
                    members = members,
                    currentUserRole = role
                )
            }
        )
        _state.value = _state.value.copy(isMembersLoading = false)
    }

    fun approveMember(memberId: Int) {
        viewModelScope.launch {
            repository.approveMember(classroomId, memberId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể duyệt thành viên"
                    )
                },
                ifRight = { approvedMember ->
                    val updatedMembers = _state.value.members.map { 
                        if (it.id == approvedMember.id) approvedMember else it 
                    }
                    _state.value = _state.value.copy(members = updatedMembers)
                }
            )
        }
    }

    private suspend fun loadChatHistory() {
        _state.value = _state.value.copy(isChatLoading = true)
        repository.getChatHistory(classroomId, limit = 50).fold(
            ifLeft = { /* non-fatal */ },
            ifRight = { messages ->
                _state.value = _state.value.copy(
                    chatMessages = messages,
                    hasMoreChatMessages = messages.size >= 50
                )
            }
        )
        _state.value = _state.value.copy(isChatLoading = false)
    }

    fun loadMoreChatHistory() {
        if (_state.value.isChatLoadingMore || !_state.value.hasMoreChatMessages) return
        
        val oldestId = _state.value.chatMessages.firstOrNull()?.id ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isChatLoadingMore = true)
            repository.getChatHistory(classroomId, limit = 30, beforeId = oldestId).fold(
                ifLeft = { /* non-fatal */ },
                ifRight = { newMessages ->
                    if (newMessages.isEmpty()) {
                        _state.value = _state.value.copy(hasMoreChatMessages = false)
                    } else {
                        val current = _state.value.chatMessages
                        _state.value = _state.value.copy(
                            chatMessages = newMessages + current,
                            hasMoreChatMessages = newMessages.size >= 30
                        )
                    }
                }
            )
            _state.value = _state.value.copy(isChatLoadingMore = false)
        }
    }

    private fun joinRoomAndListenMessages() {
        socketManager.joinClassroom(classroomId)

        if (socketCollectionJob?.isActive == true) return

        socketCollectionJob = viewModelScope.launch {
            socketManager.classroomMessageFlow()
                .catch { /* ignore socket errors */ }
                .collect { json ->
                    val message = parseMessageFromJson(json)
                    if (message != null) {
                        val currentMessages = _state.value.chatMessages
                        // Only add if ID is null (unsent) or not already in list
                        if (message.id == null || currentMessages.none { it.id == message.id }) {
                            _state.value = _state.value.copy(chatMessages = currentMessages + message)
                        }
                    }
                }
        }
    }

    fun onChatInputChange(text: String) {
        _state.value = _state.value.copy(chatInput = text)
    }

    fun sendMessage() {
        val content = _state.value.chatInput.trim()
        val attachmentUrl = _state.value.pendingAttachmentUrl
        val attachmentType = _state.value.pendingAttachmentType
        val repliedToId = _state.value.replyingTo?.id

        if (content.isEmpty() && attachmentUrl == null) return

        val type = when {
            attachmentType == "IMAGE" -> "IMAGE"
            attachmentType == "FILE" -> "FILE"
            else -> "TEXT"
        }
        val messageContent = content.ifEmpty { attachmentUrl ?: "" }

        _state.value = _state.value.copy(
            isSendingMessage = true,
            chatInput = "",
            replyingTo = null,
            pendingAttachmentUrl = null,
            pendingAttachmentType = null
        )
        socketManager.sendClassroomMessage(
            classroomId = classroomId,
            content = messageContent,
            type = type,
            attachmentUrl = attachmentUrl,
            repliedToId = repliedToId
        )
        _state.value = _state.value.copy(isSendingMessage = false)
    }

    fun setReplyingTo(message: ClassroomMessage?) {
        _state.value = _state.value.copy(replyingTo = message)
    }

    fun clearAttachment() {
        _state.value = _state.value.copy(
            pendingAttachmentUrl = null,
            pendingAttachmentType = null
        )
    }

    fun uploadAttachment(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingAttachment = true)
            FileUploadHelper.uploadImage(context, uri, "lingora/classroom-chat").fold(
                ifLeft = {
                    _state.value = _state.value.copy(isUploadingAttachment = false)
                },
                ifRight = { url ->
                    _state.value = _state.value.copy(
                        pendingAttachmentUrl = url,
                        pendingAttachmentType = "IMAGE",
                        isUploadingAttachment = false
                    )
                }
            )
        }
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

    fun showRemoveMemberDialog(member: ClassroomMember) {
        _state.value = _state.value.copy(memberToRemove = member)
    }

    fun dismissRemoveMemberDialog() {
        _state.value = _state.value.copy(memberToRemove = null)
    }

    fun confirmRemoveMember() {
        val member = _state.value.memberToRemove ?: return
        _state.value = _state.value.copy(isRemovingMember = true)

        viewModelScope.launch {
            repository.removeMember(classroomId, member.id).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể loại bỏ thành viên",
                        isRemovingMember = false,
                        memberToRemove = null
                    )
                },
                ifRight = {
                    val updated = _state.value.members.filterNot { it.id == member.id }
                    _state.value = _state.value.copy(
                        members = updated,
                        isRemovingMember = false,
                        memberToRemove = null
                    )
                }
            )
        }
    }

    fun archiveClassroom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.updateClassroom(
                id = classroomId,
                status = "ARCHIVED"
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể lưu trữ lớp học"
                    )
                },
                ifRight = {
                    loadClassroom()
                    _state.value = _state.value.copy(isLoading = false)
                }
            )
        }
    }

    fun deleteClassroom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.deleteClassroom(classroomId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể xóa lớp học"
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(isLoading = false, isDeleted = true)
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
                    com.example.lingora_fe.util.DateFormatHelper.parseDate(it)
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
