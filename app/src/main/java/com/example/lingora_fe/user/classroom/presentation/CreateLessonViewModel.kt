package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateLessonViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val lessonId: Int? = savedStateHandle.get<String>("lessonId")?.toIntOrNull()

    private val _state = MutableStateFlow(CreateLessonState())
    val state: StateFlow<CreateLessonState> = _state.asStateFlow()

    init {
        lessonId?.let { 
            _state.value = _state.value.copy(isEditMode = true)
            loadLessonData(it) 
        }
    }

    private fun loadLessonData(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getLessonById(classroomId, id).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải thông tin bài học"
                    )
                },
                ifRight = { lesson ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isEditMode = true,
                        title = lesson.title,
                        description = lesson.description ?: "",
                        lessonType = lesson.lessonType,
                        content = lesson.content ?: "",
                        sortOrder = lesson.sortOrder,
                        isPublished = lesson.isPublished
                    )
                }
            )
        }
    }

    fun onTitleChange(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onLessonTypeChange(lessonType: ClassroomLessonType) {
        _state.value = _state.value.copy(lessonType = lessonType)
    }

    fun onContentChange(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    fun onSortOrderChange(value: String) {
        _state.value = _state.value.copy(sortOrder = value.toIntOrNull() ?: 0)
    }

    fun onIsPublishedChange(isPublished: Boolean) {
        _state.value = _state.value.copy(isPublished = isPublished)
    }

    fun createLesson() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Tiêu đề bài học không được để trống")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = if (lessonId != null) {
                repository.updateLesson(
                    classroomId = classroomId,
                    lessonId = lessonId,
                    title = current.title.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    lessonType = current.lessonType.value,
                    content = current.content.trim().takeIf { it.isNotEmpty() },
                    sortOrder = current.sortOrder,
                    isPublished = current.isPublished
                )
            } else {
                repository.createLesson(
                    classroomId = classroomId,
                    title = current.title.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    lessonType = current.lessonType.value,
                    content = current.content.trim().takeIf { it.isNotEmpty() },
                    sortOrder = current.sortOrder,
                    isPublished = current.isPublished
                )
            }

            result.fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể lưu bài học"
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            )
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }
}
