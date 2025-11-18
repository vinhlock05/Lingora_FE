package com.example.lingora_fe.user.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.forum.domain.model.Comment
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: ForumRepository,
    tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()
    
    init {
        _state.value = _state.value.copy(currentUserId = tokenManager.getUserId())
    }
    
    fun loadPost(postId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getPostById(postId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = { post ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        post = post
                    )
                    loadComments(postId)
                }
            )
        }
    }
    
    fun loadComments(postId: Int) {
        viewModelScope.launch {
            val expandedIds = _state.value.expandedParentIds
            repository.getChildComments(postId, null, "POST").fold(
                ifLeft = { },
                ifRight = { parents ->
                    val replyMap = mutableMapOf<Int, List<Comment>>()
                    val replyIds = mutableSetOf<Int>()
                    
                    parents.forEach { parent ->
                        val replies = fetchReplies(postId, parent.id)
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
                    _state.value = _state.value.copy(commentThreads = threads)
                }
            )
        }
    }
    
    private suspend fun fetchReplies(postId: Int, parentId: Int): List<Comment> {
        return repository.getChildComments(postId, parentId, "POST").fold(
            ifLeft = { emptyList() },
            ifRight = { it }
        )
    }
    
    fun toggleRepliesVisibility(parentId: Int) {
        val currentIds = _state.value.expandedParentIds
        val newSet = if (currentIds.contains(parentId)) currentIds - parentId else currentIds + parentId
        _state.value = _state.value.copy(
            expandedParentIds = newSet,
            commentThreads = _state.value.commentThreads.map {
                if (it.parent.id == parentId) it.copy(areRepliesVisible = newSet.contains(parentId))
                else it
            }
        )
    }
    
    fun updateCommentText(text: String) {
        _state.value = _state.value.copy(
            commentText = text,
            commentCharCount = text.length
        )
    }
    
    fun startReply(parentId: Int, targetUsername: String) {
        _state.value = _state.value.copy(
            replyContext = ReplyContext(parentCommentId = parentId, targetUsername = targetUsername)
        )
    }
    
    fun cancelReply() {
        _state.value = _state.value.copy(replyContext = null)
    }
    
    fun submitComment(postId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            val commentText = currentState.commentText.trim()
            
            if (commentText.isEmpty() || commentText.length > 256) {
                return@launch
            }
            
            val replyContext = currentState.replyContext
            val content = buildCommentContent(commentText, replyContext)
            
            _state.value = _state.value.copy(isSubmittingComment = true)
            
            repository.createComment(
                targetId = postId,
                content = content,
                parentId = replyContext?.parentCommentId,
                targetType = "POST"
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isSubmittingComment = false,
                        error = error.message ?: "Failed to create comment"
                    )
                },
                ifRight = {
                    val updatedExpanded = replyContext?.parentCommentId?.let {
                        _state.value.expandedParentIds + it
                    } ?: _state.value.expandedParentIds
                    _state.value = _state.value.copy(
                        isSubmittingComment = false,
                        commentText = "",
                        commentCharCount = 0,
                        replyContext = null,
                        expandedParentIds = updatedExpanded
                    )
                    loadPost(postId)
                    loadComments(postId)
                    onSuccess()
                }
            )
        }
    }
    
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            val post = _state.value.post ?: return@launch
            
            _state.value = _state.value.copy(isLiking = true)
            
            val action = if (post.isAlreadyLike) {
                repository.unlike(postId, "POST")
            } else {
                repository.like(postId, "POST")
            }
            
            action.fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Failed to toggle like",
                        isLiking = false
                    )
                },
                ifRight = {
                    loadPost(postId)
                    _state.value = _state.value.copy(isLiking = false)
                }
            )
        }
    }
    
    fun toggleCommentLike(commentId: Int) {
        viewModelScope.launch {
            val originalThreads = _state.value.commentThreads
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
            _state.value = _state.value.copy(commentThreads = updatedThreads)
            
            val action = if (comment.isAlreadyLike) {
                repository.unlike(commentId, "COMMENT")
            } else {
                repository.like(commentId, "COMMENT")
            }
            
            action.fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
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
    
    private fun buildCommentContent(
        content: String,
        replyContext: ReplyContext?
    ): String {
        return replyContext?.let {
            "@${it.targetUsername} $content"
        } ?: content
    }
    
    fun deletePost(postId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deletePost(postId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa bài viết"
                    )
                },
                ifRight = {
                    onSuccess()
                }
            )
        }
    }
    
    fun updatePostStatus(postId: Int, status: PostStatus) {
        viewModelScope.launch {
            repository.updatePost(
                id = postId,
                status = status.name.lowercase()
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message
                    )
                },
                ifRight = {
                    loadPost(postId)
                }
            )
        }
    }
    
    fun startEditComment(comment: Comment) {
        _state.value = _state.value.copy(
            editingCommentId = comment.id,
            editingCommentText = comment.content
        )
    }
    
    fun updateEditingCommentText(text: String) {
        if (text.length > 256) return
        _state.value = _state.value.copy(editingCommentText = text)
    }
    
    fun cancelEditComment() {
        _state.value = _state.value.copy(
            editingCommentId = null,
            editingCommentText = "",
            isUpdatingComment = false
        )
    }
    
    fun submitEditComment(postId: Int) {
        val commentId = _state.value.editingCommentId ?: return
        val content = _state.value.editingCommentText.trim()
        if (content.isEmpty() || content.length > 256) {
            _state.value = _state.value.copy(error = "Bình luận phải từ 1-256 ký tự")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdatingComment = true)
            repository.updateComment(
                commentId = commentId,
                targetId = postId,
                content = content,
                targetType = "POST"
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isUpdatingComment = false,
                        error = error.message
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(isUpdatingComment = false)
                    cancelEditComment()
                    loadComments(postId)
                }
            )
        }
    }
}

