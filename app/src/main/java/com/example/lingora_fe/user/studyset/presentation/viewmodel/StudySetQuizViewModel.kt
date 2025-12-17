package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.studyset.domain.model.Quiz
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.QuizStudyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetQuizViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int = savedStateHandle.get<Int>("studySetId") ?: 0

    private val _uiState = MutableStateFlow(QuizStudyUiState())
    val uiState: StateFlow<QuizStudyUiState> = _uiState.asStateFlow()

    init {
        loadStudySet()
    }

    private fun loadStudySet() {
        viewModelScope.launch {
            repository.getStudySetById(studySetId).fold(
                ifLeft = { error ->
                    // Handle error
                },
                ifRight = { studySet ->
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        currentIndex = 0,
                        selectedAnswers = emptyMap(),
                        showResults = false,
                        correctCount = 0
                    )
                }
            )
        }
    }

    fun selectAnswer(questionIndex: Int, answer: String) {
        val currentState = _uiState.value
        val updatedAnswers = currentState.selectedAnswers.toMutableMap()
        updatedAnswers[questionIndex] = answer
        _uiState.value = currentState.copy(selectedAnswers = updatedAnswers)
    }

    fun checkAnswer(questionIndex: Int) {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return
        val quizzes = studySet.quizzes
        
        if (questionIndex !in quizzes.indices) return
        
        val quiz = quizzes[questionIndex]
        val selectedAnswer = currentState.selectedAnswers[questionIndex]
        val isCorrect = selectedAnswer == quiz.correctAnswer
        
        val updatedCheckedAnswers = currentState.checkedAnswers.toMutableSet()
        updatedCheckedAnswers.add(questionIndex)
        
        // Update correct count if correct
        val newCorrectCount = if (isCorrect) {
            currentState.correctCount + 1
        } else {
            currentState.correctCount
        }
        
        _uiState.value = currentState.copy(
            checkedAnswers = updatedCheckedAnswers,
            showFeedback = true,
            correctCount = newCorrectCount
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return
        val quizzes = studySet.quizzes
        
        if (currentState.currentIndex < quizzes.size - 1) {
            _uiState.value = currentState.copy(
                currentIndex = currentState.currentIndex + 1,
                showFeedback = false
            )
        } else {
            // Last question - show results
            submitQuiz()
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return
        val quizzes = studySet.quizzes
        
        if (index in 0 until quizzes.size) {
            _uiState.value = currentState.copy(currentIndex = index)
        }
    }

    fun submitQuiz() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            showResults = true,
            showFeedback = false
        )
    }

    fun resetQuiz() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            currentIndex = 0,
            selectedAnswers = emptyMap(),
            showResults = false,
            correctCount = 0,
            showFeedback = false,
            checkedAnswers = emptySet()
        )
    }

    fun isAnswerCorrect(questionIndex: Int): Boolean {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return false
        val quizzes = studySet.quizzes
        
        if (questionIndex !in quizzes.indices) return false
        val quiz = quizzes[questionIndex]
        val selectedAnswer = currentState.selectedAnswers[questionIndex]
        
        return selectedAnswer == quiz.correctAnswer
    }

    fun getCurrentQuiz(): Quiz? {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return null
        val quizzes = studySet.quizzes
        
        return if (currentState.currentIndex in quizzes.indices) {
            quizzes[currentState.currentIndex]
        } else {
            null
        }
    }
}


