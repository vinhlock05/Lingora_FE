package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateQuizViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val quizId: Int? = savedStateHandle.get<String>("quizId")?.toIntOrNull()

    private val _state = MutableStateFlow(CreateQuizState())
    val state: StateFlow<CreateQuizState> = _state.asStateFlow()

    init {
        quizId?.let { 
            _state.value = _state.value.copy(isEditMode = true)
            loadQuizData(it) 
        }
    }

    private fun loadQuizData(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getQuizById(classroomId, id).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải thông tin bài kiểm tra"
                    )
                },
                ifRight = { quiz ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isEditMode = true,
                        title = quiz.title,
                        description = quiz.description ?: "",
                        timeLimitSeconds = quiz.timeLimitSeconds,
                        maxAttempts = quiz.maxAttempts,
                        passingScore = (quiz.passingScore * 100).toInt().toString(),
                        isPublished = quiz.isPublished
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

    fun onTimeLimitSecondsChange(value: String) {
        _state.value = _state.value.copy(timeLimitSeconds = value.toIntOrNull())
    }

    fun onMaxAttemptsChange(value: String) {
        _state.value = _state.value.copy(maxAttempts = value.toIntOrNull() ?: 1)
    }

    fun onPassingScoreChange(value: String) {
        _state.value = _state.value.copy(passingScore = value)
    }

    fun onIsPublishedChange(isPublished: Boolean) {
        _state.value = _state.value.copy(isPublished = isPublished)
    }

    fun createQuiz() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Tiêu đề bài kiểm tra không được để trống")
            return
        }

        val scaledScore = current.passingScore.toDoubleOrNull()?.let { it / 100.0 } ?: 0.7

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = if (quizId != null) {
                repository.updateQuiz(
                    classroomId = classroomId,
                    quizId = quizId,
                    title = current.title.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    timeLimitSeconds = current.timeLimitSeconds,
                    maxAttempts = current.maxAttempts,
                    passingScore = scaledScore,
                    isPublished = current.isPublished
                )
            } else {
                repository.createQuiz(
                    classroomId = classroomId,
                    title = current.title.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    timeLimitSeconds = current.timeLimitSeconds,
                    maxAttempts = current.maxAttempts,
                    passingScore = scaledScore,
                    isPublished = current.isPublished
                )
            }

            result.fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể lưu bài kiểm tra"
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
