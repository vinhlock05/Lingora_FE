package com.example.lingora_fe.user.forum.presentation

import androidx.lifecycle.SavedStateHandle
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

data class EditPostState(
    val isLoadingData: Boolean = true,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val content: String = "",
    val thumbnails: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val selectedTopic: PostTopic? = PostTopic.GENERAL,
    val titleCharCount: Int = 0,
    val contentCharCount: Int = 0,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val repository: ForumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: Int = savedStateHandle.get<Int>("postId") ?: 0

    private val popularTags = listOf(
        "grammar", "vocabulary", "pronunciation", "beginner",
        "intermediate", "advanced", "tips", "practice"
    )

    private val _state = MutableStateFlow(EditPostState())
    val state: StateFlow<EditPostState> = _state.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            repository.getPostById(postId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoadingData = false,
                        error = error.message ?: "Không thể tải dữ liệu bài viết"
                    )
                },
                ifRight = { post ->
                    _state.value = _state.value.copy(
                        isLoadingData = false,
                        title = post.title,
                        content = post.content,
                        thumbnails = post.thumbnails,
                        tags = post.tags,
                        selectedTopic = post.topic,
                        titleCharCount = post.title.length,
                        contentCharCount = post.content.length
                    )
                }
            )
        }
    }

    fun updateTitle(title: String) {
        val normalized = title.take(150)
        _state.value = _state.value.copy(
            title = normalized,
            titleCharCount = normalized.length
        )
    }

    fun updateContent(content: String) {
        _state.value = _state.value.copy(
            content = content,
            contentCharCount = content.length
        )
    }

    fun addThumbnail(url: String) {
        val updated = _state.value.thumbnails.toMutableList()
        updated.add(url)
        _state.value = _state.value.copy(thumbnails = updated)
    }

    fun removeThumbnail(index: Int) {
        val updated = _state.value.thumbnails.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _state.value = _state.value.copy(thumbnails = updated)
        }
    }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isEmpty()) return
        val current = _state.value.tags.toMutableList()
        if (current.size >= 5 || clean in current) return
        current.add(clean)
        _state.value = _state.value.copy(tags = current)
    }

    fun removeTag(tag: String) {
        val current = _state.value.tags.toMutableList()
        current.remove(tag)
        _state.value = _state.value.copy(tags = current)
    }

    fun getPopularTags(): List<String> = popularTags

    fun selectTopic(topic: PostTopic?) {
        _state.value = _state.value.copy(selectedTopic = topic)
    }

    fun setUploading(isUploading: Boolean) {
        _state.value = _state.value.copy(isUploading = isUploading)
    }

    fun submitEdit() {
        val current = _state.value
        if (current.title.isBlank() || current.content.isBlank()) {
            setError("Tiêu đề và nội dung không được để trống")
            return
        }
        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, error = null)
            repository.updatePost(
                id = postId,
                title = current.title,
                content = current.content,
                thumbnails = if (current.thumbnails.isNotEmpty()) current.thumbnails else null,
                tags = if (current.tags.isNotEmpty()) current.tags else null,
                topic = current.selectedTopic
            ).fold(
                ifLeft = { error ->
                    _state.value = current.copy(
                        isSaving = false,
                        error = error.message ?: "Không thể lưu bài viết"
                    )
                },
                ifRight = {
                    _state.value = current.copy(
                        isSaving = false,
                        isSuccess = true,
                        error = null
                    )
                }
            )
        }
    }

    private fun setError(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun consumeSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}

