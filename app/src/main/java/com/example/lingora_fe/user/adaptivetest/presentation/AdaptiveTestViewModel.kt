package com.example.lingora_fe.user.adaptivetest.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.adaptivetest.domain.model.*
import com.example.lingora_fe.user.adaptivetest.domain.repository.AdaptiveTestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdaptiveTestViewModel @Inject constructor(
    private val repository: AdaptiveTestRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdaptiveTestState())
    val state: StateFlow<AdaptiveTestState> = _state.asStateFlow()

    init {
        startTest()
    }

    fun startTest() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getNextQuestion(emptyList())
                .onRight { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentQuestion = result.nextQuestion,
                        currentProficiency = result.currentProficiency,
                        answeredCount = result.answeredCount,
                        isCompleted = result.isCompleted,
                        error = null
                    )
                }
                .onLeft { failure ->
                    Log.e("AdaptiveTestViewModel", "Failed to start test: ${failure.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message ?: "Không thể bắt đầu bài test"
                    )
                }
        }
    }

    fun selectAnswer(answer: String) {
        _state.value = _state.value.copy(selectedAnswer = answer)
    }

    fun submitAnswer() {
        val currentQuestion = _state.value.currentQuestion
        val selectedAnswer = _state.value.selectedAnswer
        
        if (currentQuestion == null || selectedAnswer == null) {
            _state.value = _state.value.copy(
                error = "Vui lòng chọn một đáp án"
            )
            return
        }

        val newAnswer = AdaptiveTestAnswer(
            questionId = currentQuestion.id,
            answer = selectedAnswer
        )
        
        val updatedAnsweredQuestions = _state.value.answeredQuestions + newAnswer

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getNextQuestion(updatedAnsweredQuestions)
                .onRight { result ->
                    // Find the evaluation for the current question
                    val currentEvaluation = result.answerEvaluations.find { 
                        it.questionId == currentQuestion.id 
                    }
                    val correctAnswer = currentEvaluation?.correctAnswer
                    
                    // After getting response, show result with the answered question
                    // Store the next question temporarily while showing result
                    _state.value = _state.value.copy(
                        isLoading = false,
                        answeredQuestions = updatedAnsweredQuestions,
                        answerEvaluations = result.answerEvaluations,
                        // Keep last question to show result
                        lastAnsweredQuestion = currentQuestion,
                        lastSelectedAnswer = selectedAnswer,
                        lastQuestionId = currentQuestion.id,
                        lastCorrectAnswer = correctAnswer,
                        showResult = true,
                        // Store next question but don't show it yet
                        nextQuestion = result.nextQuestion,
                        currentProficiency = result.currentProficiency,
                        answeredCount = result.answeredCount,
                        isCompleted = result.isCompleted,
                        finalProficiency = result.proficiency,
                        selectedAnswer = null,
                        error = null
                    )
                }
                .onLeft { failure ->
                    Log.e("AdaptiveTestViewModel", "Failed to submit answer: ${failure.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showResult = false,
                        error = failure.message ?: "Không thể gửi câu trả lời"
                    )
                }
        }
    }
    
    fun continueToNextQuestion() {
        // Use the stored nextQuestion when continuing
        val nextQuestion = _state.value.nextQuestion
        val isCompleted = _state.value.isCompleted
        
        if (isCompleted) {
            // Test completed, just clear result
            _state.value = _state.value.copy(
                showResult = false,
                lastAnsweredQuestion = null,
                lastSelectedAnswer = null,
                lastQuestionId = null,
                lastCorrectAnswer = null,
                nextQuestion = null
            )
        } else {
            // Move to next question
            _state.value = _state.value.copy(
                showResult = false,
                lastAnsweredQuestion = null,
                lastSelectedAnswer = null,
                lastQuestionId = null,
                lastCorrectAnswer = null,
                currentQuestion = nextQuestion,
                nextQuestion = null
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

