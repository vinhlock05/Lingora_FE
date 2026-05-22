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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val quizId: Int =
        requireNotNull(savedStateHandle.get<String>("quizId")?.toIntOrNull()) {
            "quizId is required"
        }

    private val _state = MutableStateFlow(QuizSessionState(
        timeLeftSeconds = savedStateHandle.get<Int>("timeLeftSeconds") ?: 0
    ))
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
                    val savedTime = savedStateHandle.get<Int>("timeLeftSeconds") as? Int
                    _state.value = _state.value.copy(
                        isLoading = false,
                        quiz = quiz,
                        timeLeftSeconds = savedTime ?: quiz.timeLimitSeconds ?: 0,
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
                savedStateHandle["timeLeftSeconds"] = _state.value.timeLeftSeconds
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
            
            val answers = buildSubmitAnswers()
            
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
                        score = result.attempt.correctCount ?: inferCorrectCountFromScore(
                            scoreRatio = result.attempt.score,
                            totalQuestions = _state.value.totalQuestions
                        ),
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

    private fun buildSubmitAnswers(): Map<String, String> {
        val quizQuestions = _state.value.quiz?.questions.orEmpty()
        return _state.value.userChoices.mapValues { (questionId, selectedValue) ->
            val question = quizQuestions.find { it.id == questionId } ?: return@mapValues selectedValue
            if (question.correctAnswer.toIntOrNull() != null) {
                val selectedIndex = question.options.indexOf(selectedValue)
                if (selectedIndex >= 0) selectedIndex.toString() else selectedValue
            } else {
                selectedValue
            }
        }.mapKeys { it.key.toString() }
    }

    private fun inferCorrectCountFromScore(scoreRatio: Double, totalQuestions: Int): Int {
        if (totalQuestions <= 0) return 0
        return (scoreRatio * totalQuestions).toInt()
    }
}
