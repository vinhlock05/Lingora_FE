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
class QuizAttemptsViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val quizId: Int =
        requireNotNull(savedStateHandle.get<String>("quizId")?.toIntOrNull()) {
            "quizId is required"
        }

    private val _state = MutableStateFlow(QuizAttemptsState())
    val state: StateFlow<QuizAttemptsState> = _state.asStateFlow()

    init {
        loadAttempts()
    }

    fun refresh() {
        loadAttempts()
    }

    private fun loadAttempts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getQuizAttempts(classroomId, quizId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải kết quả làm bài"
                    )
                },
                ifRight = { attempts ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        attempts = attempts
                    )
                }
            )
        }
    }
}
