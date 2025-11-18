package com.example.lingora_fe.user.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val repository: ForumRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ForumState())
    val state: StateFlow<ForumState> = _state.asStateFlow()
    
    init {
        _state.value = _state.value.copy(currentUserId = tokenManager.getUserId())
        loadPosts()
    }
    
    fun loadPosts(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val currentState = _state.value
            val topic = currentState.selectedTopic
            val tags = if (currentState.appliedTags.isNotEmpty()) currentState.appliedTags else null
            val search = currentState.searchText.takeIf { it.isNotEmpty() }
            val ownerId = if (currentState.showMyPosts) currentState.currentUserId else null
            val status = if (currentState.showMyPosts) currentState.myPostsStatus.name.lowercase() else null
            
            repository.getAllPosts(
                page = page,
                limit = 20,
                sort = "-createdAt",
                search = search,
                topic = topic,
                tags = tags,
                ownerId = ownerId,
                status = status
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load posts"
                    )
                },
                ifRight = { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        posts = result.posts,
                        currentPage = result.currentPage,
                        totalPages = result.totalPages,
                        total = result.total
                    )
                }
            )
        }
    }
    
    fun selectTopic(topic: PostTopic?) {
        _state.value = _state.value.copy(selectedTopic = topic, currentPage = 1)
        loadPosts(1)
    }

    fun setShowMyPosts(show: Boolean) {
        if (show == _state.value.showMyPosts) return
        if (show && _state.value.currentUserId == null) {
            _state.value = _state.value.copy(error = "Vui lòng đăng nhập để xem bài viết của bạn")
            return
        }
        _state.value = _state.value.copy(showMyPosts = show, currentPage = 1)
        loadPosts(1)
    }

    fun setMyPostsStatus(status: PostStatus) {
        if (_state.value.myPostsStatus == status) return
        _state.value = _state.value.copy(myPostsStatus = status, currentPage = 1)
        loadPosts(1)
    }
    
    fun onSearchInputChange(input: String) {
        var updatedInput = input
        if (updatedInput.isNotEmpty() && updatedInput.last().isWhitespace()) {
            val trimmed = updatedInput.trimEnd()
            val lastToken = trimmed.substringAfterLast(" ")
            val hasSpace = trimmed.contains(' ')
            val token = if (hasSpace) trimmed.substringAfterLast(' ') else trimmed
            if (token.startsWith("#") && token.length > 1) {
                addSearchTag(token.substring(1))
                updatedInput = if (hasSpace) trimmed.substring(0, trimmed.length - token.length).trimEnd() + " " else ""
            }
        }
        _state.value = _state.value.copy(searchInput = updatedInput)
    }
    
    fun removeSearchTag(tag: String) {
        val updated = _state.value.searchTags.filterNot { it.equals(tag, ignoreCase = false) }
        _state.value = _state.value.copy(searchTags = updated)
    }
    
    private fun addSearchTag(tag: String) {
        val normalized = tag.trim()
        if (normalized.isEmpty()) return
        val current = _state.value.searchTags.toMutableList()
        current.remove(normalized)
        current.add(0, normalized)
        _state.value = _state.value.copy(searchTags = current)
    }
    
    fun applySearchFilters() {
        val trimmedText = _state.value.searchInput.trim()
        val currentTags = _state.value.searchTags
        if (trimmedText == _state.value.searchText && currentTags == _state.value.appliedTags) return
        _state.value = _state.value.copy(
            searchText = trimmedText,
            appliedTags = currentTags
        )
        loadPosts(1)
    }
    
    fun refresh() {
        loadPosts(_state.value.currentPage)
    }
    
    fun forceReload() {
        loadPosts(1)
    }
    
    fun updatePostStatus(postId: Int, status: PostStatus) {
        viewModelScope.launch {
            repository.updatePost(id = postId, status = status.name.lowercase()).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(error = error.message ?: "Không thể cập nhật trạng thái")
                },
                ifRight = {
                    loadPosts(_state.value.currentPage)
                }
            )
        }
    }
    
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            repository.deletePost(postId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(error = error.message ?: "Không thể xóa bài viết")
                },
                ifRight = {
                    val filtered = _state.value.posts.filterNot { it.id == postId }
                    _state.value = _state.value.copy(posts = filtered)
                }
            )
        }
    }
    
    fun loadMore() {
        val currentState = _state.value
        if (currentState.currentPage < currentState.totalPages && !currentState.isLoading) {
            loadPosts(currentState.currentPage + 1)
        }
    }
    
    fun togglePostLike(postId: Int) {
        viewModelScope.launch {
            val currentPosts = _state.value.posts
            val targetPost = currentPosts.find { it.id == postId } ?: return@launch
            val isCurrentlyLiked = targetPost.isAlreadyLike
            val delta = if (isCurrentlyLiked) -1 else 1
            
            val updatedPosts = currentPosts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isAlreadyLike = !isCurrentlyLiked,
                        likeCount = (post.likeCount + delta).coerceAtLeast(0)
                    )
                } else post
            }
            
            _state.value = _state.value.copy(posts = updatedPosts)
            
            val action = if (isCurrentlyLiked) {
                repository.unlike(postId, "POST")
            } else {
                repository.like(postId, "POST")
            }
            
            action.fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        posts = currentPosts,
                        error = error.message ?: "Failed to toggle like"
                    )
                },
                ifRight = { }
            )
        }
    }
}


