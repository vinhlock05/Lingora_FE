package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.domain.model.SubmitQuizAttemptResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
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

    private val _state = MutableStateFlow(QuizSessionState())
    val state: StateFlow<QuizSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuizAndStart()
    }

    private fun loadQuizAndStart() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getQuizDetail(classroomId, quizId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải bài kiểm tra"
                    )
                },
                ifRight = { quiz ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        quiz = quiz,
                        timeLeftSeconds = quiz.timeLimitSeconds ?: 0,
                        totalQuestions = quiz.questions?.size ?: 0
                    )
                    if (quiz.timeLimitSeconds != null && quiz.timeLimitSeconds > 0) {
                        startTimer()
                    }
                }
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0 && !_state.value.isFinished) {
                delay(1000)
                _state.value = _state.value.copy(
                    timeLeftSeconds = _state.value.timeLeftSeconds - 1
                )
            }
            if (_state.value.timeLeftSeconds <= 0 && !_state.value.isFinished) {
                finishQuiz()
            }
        }
    }

    fun selectOption(questionId: Int, choice: String) {
        if (_state.value.isFinished) return
        val currentChoices = _state.value.userChoices.toMutableMap()
        currentChoices[questionId] = choice
        _state.value = _state.value.copy(userChoices = currentChoices)
    }

    fun nextQuestion() {
        val nextIndex = _state.value.currentQuestionIndex + 1
        if (nextIndex < (_state.value.quiz?.questions?.size ?: 0)) {
            _state.value = _state.value.copy(currentQuestionIndex = nextIndex)
        }
    }

    fun previousQuestion() {
        val prevIndex = _state.value.currentQuestionIndex - 1
        if (prevIndex >= 0) {
            _state.value = _state.value.copy(currentQuestionIndex = prevIndex)
        }
    }

    fun finishQuiz() {
        if (_state.value.isFinished || _state.value.isLoading) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Map keys from Int to String for the API
            val answers = _state.value.userChoices.mapKeys { it.key.toString() }
            
            repository.submitQuizAttempt(classroomId, quizId, answers).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể nộp bài kiểm tra"
                    )
                },
                ifRight = { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isFinished = true,
                        score = result.attempt.correctCount ?: 0,
                        isPassing = result.isPassing
                    )
                    timerJob?.cancel()
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
