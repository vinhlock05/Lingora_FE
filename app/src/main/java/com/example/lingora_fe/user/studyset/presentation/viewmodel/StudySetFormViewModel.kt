package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.studyset.domain.model.CreateStudySetData
import com.example.lingora_fe.user.studyset.domain.model.Flashcard
import com.example.lingora_fe.user.studyset.domain.model.Quiz
import com.example.lingora_fe.user.studyset.domain.model.UpdateStudySetData
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.ContentTab
import com.example.lingora_fe.user.studyset.presentation.FlashcardFormItem
import com.example.lingora_fe.user.studyset.presentation.QuizFormItem
import com.example.lingora_fe.user.studyset.presentation.StudySetFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetFormViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int? = savedStateHandle.get<Int>("studySetId") ?: null

    private val _uiState = MutableStateFlow(
        StudySetFormUiState(
            isEditMode = studySetId != null,
            studySetId = studySetId
        )
    )
    val uiState: StateFlow<StudySetFormUiState> = _uiState.asStateFlow()

    init {
        if (studySetId != null) {
            loadStudySet()
        }
    }

    private fun loadStudySet() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val id = studySetId ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getStudySetById(token, id).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                        saveSuccess = false
                    )
                },
                ifRight = { studySet ->
                    _uiState.value = _uiState.value.copy(
                        title = studySet.title,
                        description = studySet.description ?: "",
                        visibility = studySet.visibility,
                        isPaid = studySet.price > 0,
                        price = if (studySet.price > 0) studySet.price.toString() else "",
                        flashcards = studySet.flashcards.map { 
                            FlashcardFormItem(
                                id = it.id,
                                frontText = it.frontText,
                                backText = it.backText,
                                example = it.example ?: ""
                            )
                        },
                        quizzes = studySet.quizzes.map {
                            QuizFormItem(
                                id = it.id,
                                type = it.type,
                                question = it.question,
                                options = it.options,
                                correctAnswer = it.correctAnswer
                            )
                        },
                        isLoading = false,
                        error = null,
                        saveSuccess = false
                    )
                }
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun togglePaid() {
        _uiState.value = _uiState.value.copy(
            isPaid = !_uiState.value.isPaid,
            price = if (!_uiState.value.isPaid) "" else _uiState.value.price
        )
    }

    fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price)
    }

    fun updateVisibility(visibility: com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility) {
        _uiState.value = _uiState.value.copy(visibility = visibility)
    }

    fun switchContentTab(tab: ContentTab) {
        _uiState.value = _uiState.value.copy(selectedContentTab = tab)
    }

    fun addFlashcard() {
        val newFlashcard = FlashcardFormItem()
        _uiState.value = _uiState.value.copy(
            flashcards = _uiState.value.flashcards + newFlashcard
        )
    }

    fun updateFlashcard(index: Int, flashcard: FlashcardFormItem) {
        val updated = _uiState.value.flashcards.toMutableList()
        updated[index] = flashcard
        _uiState.value = _uiState.value.copy(flashcards = updated)
    }

    fun removeFlashcard(index: Int) {
        val updated = _uiState.value.flashcards.toMutableList()
        updated.removeAt(index)
        _uiState.value = _uiState.value.copy(flashcards = updated)
    }

    fun addQuiz() {
        val newQuiz = QuizFormItem()
        _uiState.value = _uiState.value.copy(
            quizzes = _uiState.value.quizzes + newQuiz
        )
    }

    fun updateQuiz(index: Int, quiz: QuizFormItem) {
        val updated = _uiState.value.quizzes.toMutableList()
        updated[index] = quiz
        _uiState.value = _uiState.value.copy(quizzes = updated)
    }

    fun removeQuiz(index: Int) {
        val updated = _uiState.value.quizzes.toMutableList()
        updated.removeAt(index)
        _uiState.value = _uiState.value.copy(quizzes = updated)
    }

    fun saveStudySet() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val state = _uiState.value
            
            if (state.title.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tiêu đề")
                return@launch
            }

        _uiState.value = state.copy(isLoading = true, error = null, saveSuccess = false)

            val flashcards = state.flashcards.map {
                Flashcard(
                    id = it.id,
                    frontText = it.frontText,
                    backText = it.backText,
                    example = it.example.ifEmpty { null }
                )
            }

            val quizzes = state.quizzes.map {
                Quiz(
                    id = it.id,
                    type = it.type,
                    question = it.question,
                    options = it.options.filter { opt -> opt.isNotBlank() },
                    correctAnswer = it.correctAnswer
                )
            }

            val price = if (state.isPaid) {
                state.price.toIntOrNull() ?: 0
            } else {
                0
            }

            val result = if (state.isEditMode && state.studySetId != null) {
                repository.updateStudySet(
                    token,
                    state.studySetId,
                    UpdateStudySetData(
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        visibility = state.visibility,
                        price = price,
                        flashcards = flashcards,
                        quizzes = quizzes
                    )
                )
            } else {
                repository.createStudySet(
                    token,
                    CreateStudySetData(
                        title = state.title,
                        description = state.description.ifEmpty { null },
                        visibility = state.visibility,
                        price = price,
                        flashcards = flashcards,
                        quizzes = quizzes
                    )
                )
            }

            result.fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                        saveSuccess = false
                    )
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        saveSuccess = true
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun consumeSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}

