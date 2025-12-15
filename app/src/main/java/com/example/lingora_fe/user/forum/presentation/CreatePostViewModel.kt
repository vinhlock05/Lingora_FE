package com.example.lingora_fe.user.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: ForumRepository
) : ViewModel() {
    
    companion object {
        private const val TITLE_MAX_LENGTH = 150
    }
    
    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()
    
    private val popularTags = listOf(
        "grammar", "vocabulary", "pronunciation", "beginner",
        "intermediate", "advanced", "tips", "practice"
    )
    
    fun updateTitle(title: String) {
        val normalizedTitle = title.take(TITLE_MAX_LENGTH)
        _state.value = _state.value.copy(
            title = normalizedTitle,
            titleCharCount = normalizedTitle.length
        )
    }
    
    fun updateContent(content: String) {
        _state.value = _state.value.copy(
            content = content,
            contentCharCount = content.length
        )
    }
    
    fun setThumbnails(thumbnails: List<String>) {
        _state.value = _state.value.copy(thumbnails = thumbnails)
    }
    
    fun addThumbnail(imageUrl: String) {
        val currentThumbnails = _state.value.thumbnails.toMutableList()
        currentThumbnails.add(imageUrl)
        _state.value = _state.value.copy(thumbnails = currentThumbnails)
    }
    
    fun removeThumbnail(index: Int) {
        val currentThumbnails = _state.value.thumbnails.toMutableList()
        if (index in currentThumbnails.indices) {
            currentThumbnails.removeAt(index)
            _state.value = _state.value.copy(thumbnails = currentThumbnails)
        }
    }
    
    fun addTag(tag: String) {
        val cleanTag = tag.trim().removePrefix("#")
        if (cleanTag.isEmpty()) return
        
        val currentTags = _state.value.tags.toMutableList()
        if (currentTags.size >= 5) return // Max 5 tags
        if (cleanTag in currentTags) return // Already exists
        
        currentTags.add(cleanTag)
        _state.value = _state.value.copy(tags = currentTags)
    }
    
    fun removeTag(tag: String) {
        val currentTags = _state.value.tags.toMutableList()
        currentTags.remove(tag)
        _state.value = _state.value.copy(tags = currentTags)
    }
    
    fun selectTopic(topic: PostTopic?) {
        _state.value = _state.value.copy(selectedTopic = topic)
    }
    
    fun getPopularTags(): List<String> = popularTags
    
    fun createPost() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (currentState.title.isEmpty() || currentState.title.length > TITLE_MAX_LENGTH) {
                setError("Tiêu đề phải từ 1-${TITLE_MAX_LENGTH} ký tự")
                return@launch
            }
            
            if (currentState.content.isEmpty() || currentState.content.length > 2000) {
                setError("Nội dung phải từ 1-2000 ký tự")
                return@launch
            }
            
            if (currentState.selectedTopic == null) {
                setError("Vui lòng chọn chủ đề")
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.createPost(
                title = currentState.title,
                content = currentState.content,
                thumbnails = if (currentState.thumbnails.isNotEmpty()) currentState.thumbnails else null,
                tags = if (currentState.tags.isNotEmpty()) currentState.tags else null,
                topic = currentState.selectedTopic
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create post",
                        isSuccess = false
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            )
        }
    }
    
    fun reset() {
        _state.value = CreatePostState()
    }
    
    private fun setError(message: String) {
        _state.value = _state.value.copy(error = message, isLoading = false, isSuccess = false)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun consumeSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}


