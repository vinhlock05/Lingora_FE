package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.StudySetDetailUiState
import com.example.lingora_fe.user.forum.domain.model.Comment
import com.example.lingora_fe.user.forum.presentation.CommentThread
import com.example.lingora_fe.user.forum.presentation.ReplyContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetDetailViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val forumRepository: ForumRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int = savedStateHandle.get<Int>("studySetId") ?: 0

    private val _uiState = MutableStateFlow(StudySetDetailUiState())
    val uiState: StateFlow<StudySetDetailUiState> = _uiState.asStateFlow()
    
    fun getCurrentUserId(): Int? {
        return tokenManager.getUserId()
    }
    
    fun isOwner(): Boolean {
        val currentUserId = tokenManager.getUserId()
        val studySet = _uiState.value.studySet
        Log.d("StudySetDetailViewModel", "$studySet")
        Log.d("StudySetDetailViewModel", "isOwner: currentUserId=$currentUserId, studySetOwnerId=${studySet?.owner?.id}")
        return currentUserId != null && studySet != null && studySet.owner.id == currentUserId
    }
    
    fun deleteStudySet(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.deleteStudySet(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
            )
        }
    }

    init {
        _uiState.value = _uiState.value.copy(currentUserId = tokenManager.getUserId())
        loadStudySet()
    }

    fun loadStudySet() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getStudySetById(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = { studySet ->
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        isLoading = false,
                        error = null
                    )
                    loadComments()
                }
            )
        }
    }


    fun refresh() {
        loadStudySet()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun buildCommentContent(
        content: String,
        replyContext: ReplyContext?
    ): String {
        return replyContext?.let {
            "@${it.targetUsername} $content"
        } ?: content
    }

    fun loadComments() {
        viewModelScope.launch {
            val targetId = studySetId
            val expandedIds = _uiState.value.expandedParentIds
            forumRepository.getChildComments(targetId, null, "STUDY_SET").fold(
                ifLeft = { },
                ifRight = { parents ->
                    val replyMap = mutableMapOf<Int, List<Comment>>()
                    val replyIds = mutableSetOf<Int>()
                    
                    parents.forEach { parent ->
                        val replies = fetchReplies(targetId, parent.id)
                        replyMap[parent.id] = replies
                        replyIds.addAll(replies.map { it.id })
                    }
                    
                    val topLevelParents = parents.filter { comment ->
                        (comment.parentCommentId == null) && !replyIds.contains(comment.id)
                    }
                    
                    val threads = topLevelParents.map { parent ->
                        val replies = replyMap[parent.id] ?: emptyList()
                        CommentThread(
                            parent = parent,
                            replies = replies,
                            areRepliesVisible = expandedIds.contains(parent.id)
                        )
                    }
                    _uiState.value = _uiState.value.copy(commentThreads = threads)
                }
            )
        }
    }

    private suspend fun fetchReplies(targetId: Int, parentId: Int): List<Comment> {
        return forumRepository.getChildComments(targetId, parentId, "STUDY_SET").fold(
            ifLeft = { emptyList() },
            ifRight = { it }
        )
    }

    fun toggleRepliesVisibility(parentId: Int) {
        val currentIds = _uiState.value.expandedParentIds
        val newSet = if (currentIds.contains(parentId)) currentIds - parentId else currentIds + parentId
        _uiState.value = _uiState.value.copy(
            expandedParentIds = newSet,
            commentThreads = _uiState.value.commentThreads.map {
                if (it.parent.id == parentId) it.copy(areRepliesVisible = newSet.contains(parentId))
                else it
            }
        )
    }

    fun buyStudySet(onPaymentUrlReceived: (String) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val studySetId = _uiState.value.studySet?.id ?: return@launch
            
            _uiState.value = _uiState.value.copy(isPurchasing = true, error = null)
            
            repository.buyStudySet(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = error.message
                    )
                },
                ifRight = { response ->
                    if (response.isFree) {
                        _uiState.value = _uiState.value.copy(
                            isPurchasing = false
                        )
                        loadStudySet()
                    } else {
                        response.paymentUrl?.let { paymentUrl ->
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false
                            )
                            onPaymentUrlReceived(paymentUrl)
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false,
                                error = "Không thể tạo link thanh toán"
                            )
                        }
                    }
                }
            )
        }
    }

    fun verifyPayment(vnpParams: Map<String, String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: run {
                onResult(false, "Không tìm thấy token xác thực")
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.verifyVNPayPayment(token, vnpParams).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onResult(false, "Xác thực thất bại: ${error.message}")
                },
                ifRight = { response ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    if (response.success) {
                        onResult(true, response.message ?: "Thanh toán thành công!")
                    } else {
                        onResult(false, response.message ?: "Thanh toán thất bại")
                    }
                }
            )
        }
    }

    fun updateCommentText(text: String) {
        if (text.length > 256) return
        _uiState.value = _uiState.value.copy(
            commentText = text
        )
    }

    fun startReply(parentId: Int, targetUsername: String) {
        _uiState.value = _uiState.value.copy(
            replyContext = ReplyContext(parentCommentId = parentId, targetUsername = targetUsername)
        )
    }

    fun cancelReply() {
        _uiState.value = _uiState.value.copy(replyContext = null)
    }

    fun submitComment(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val studySet = currentState.studySet ?: return@launch
            val commentText = currentState.commentText.trim()
            
            if (commentText.isEmpty() || commentText.length > 256) {
                return@launch
            }
            
            val replyContext = currentState.replyContext
            val content = buildCommentContent(commentText, replyContext)
            
            _uiState.value = _uiState.value.copy(isSubmittingComment = true)
            
            forumRepository.createComment(
                targetId = studySet.id,
                content = content,
                parentId = replyContext?.parentCommentId,
                targetType = "STUDY_SET"
            ).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingComment = false,
                        error = error.message ?: "Failed to create comment"
                    )
                },
                ifRight = {
                    val updatedExpanded = replyContext?.parentCommentId?.let {
                        _uiState.value.expandedParentIds + it
                    } ?: _uiState.value.expandedParentIds
                    _uiState.value = _uiState.value.copy(
                        isSubmittingComment = false,
                        commentText = "",
                        replyContext = null,
                        expandedParentIds = updatedExpanded
                    )
                    loadComments()
                    onSuccess()
                }
            )
        }
    }

    fun toggleCommentLike(commentId: Int) {
        viewModelScope.launch {
            val originalThreads = _uiState.value.commentThreads
            var targetComment: Comment? = null
            
            val updatedThreads = originalThreads.map { thread ->
                when {
                    thread.parent.id == commentId -> {
                        targetComment = thread.parent
                        thread.copy(parent = toggleCommentLikeState(thread.parent))
                    }
                    thread.replies.any { it.id == commentId } -> {
                        val updatedReplies = thread.replies.map { reply ->
                            if (reply.id == commentId) {
                                targetComment = reply
                                toggleCommentLikeState(reply)
                            } else reply
                        }
                        thread.copy(replies = updatedReplies)
                    }
                    else -> thread
                }
            }
            
            val comment = targetComment ?: return@launch
            _uiState.value = _uiState.value.copy(commentThreads = updatedThreads)
            
            val action = if (comment.isAlreadyLike) {
                forumRepository.unlike(commentId, "COMMENT")
            } else {
                forumRepository.like(commentId, "COMMENT")
            }
            
            action.fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        commentThreads = originalThreads,
                        error = error.message ?: "Failed to toggle like"
                    )
                },
                ifRight = { }
            )
        }
    }

    private fun toggleCommentLikeState(comment: Comment): Comment {
        val isLiked = comment.isAlreadyLike
        val delta = if (isLiked) -1 else 1
        return comment.copy(
            isAlreadyLike = !isLiked,
            likeCount = (comment.likeCount + delta).coerceAtLeast(0)
        )
    }

    fun startEditComment(comment: Comment) {
        _uiState.value = _uiState.value.copy(
            editingCommentId = comment.id,
            editingCommentText = comment.content
        )
    }

    fun updateEditingCommentText(text: String) {
        if (text.length > 256) return
        _uiState.value = _uiState.value.copy(editingCommentText = text)
    }

    fun cancelEditComment() {
        _uiState.value = _uiState.value.copy(
            editingCommentId = null,
            editingCommentText = "",
            isUpdatingComment = false
        )
    }

    fun submitEditComment() {
        val commentId = _uiState.value.editingCommentId ?: return
        val content = _uiState.value.editingCommentText.trim()
        if (content.isEmpty() || content.length > 256) {
            _uiState.value = _uiState.value.copy(error = "Bình luận phải từ 1-256 ký tự")
            return
        }
        viewModelScope.launch {
            val targetId = studySetId
            _uiState.value = _uiState.value.copy(isUpdatingComment = true)
            forumRepository.updateComment(
                commentId = commentId,
                targetId = targetId,
                content = content,
                targetType = "STUDY_SET"
            ).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingComment = false,
                        error = error.message
                    )
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(isUpdatingComment = false)
                    cancelEditComment()
                    loadComments()
                }
            )
        }
    }

    fun toggleLike(studySetId: Int) {
        viewModelScope.launch {
            val studySet = _uiState.value.studySet ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLiking = true)
            
            // Optimistic update
            val isCurrentlyLiked = studySet.isAlreadyLike
            val delta = if (isCurrentlyLiked) -1 else 1
            
            _uiState.value = _uiState.value.copy(
                studySet = studySet.copy(
                    isAlreadyLike = !isCurrentlyLiked,
                    likeCount = (studySet.likeCount + delta).coerceAtLeast(0)
                ),
                isLiking = false
            )
            
            val action = if (isCurrentlyLiked) {
                forumRepository.unlike(studySetId, "STUDY_SET")
            } else {
                forumRepository.like(studySetId, "STUDY_SET")
            }
            
            action.fold(
                ifLeft = { error ->
                    // Revert optimistic update on error
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        error = error.message ?: "Failed to toggle like",
                        isLiking = false
                    )
                },
                ifRight = {
                    // Reload to get accurate state
                    loadStudySet()
                }
            )
        }
    }
}

