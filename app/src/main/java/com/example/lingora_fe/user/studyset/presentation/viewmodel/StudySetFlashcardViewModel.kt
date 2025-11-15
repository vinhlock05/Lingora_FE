package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.FlashcardStudyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetFlashcardViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int = savedStateHandle.get<Int>("studySetId") ?: 0

    private val _uiState = MutableStateFlow(FlashcardStudyUiState())
    val uiState: StateFlow<FlashcardStudyUiState> = _uiState.asStateFlow()

    init {
        loadStudySet()
    }

    private fun loadStudySet() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            repository.getStudySetById(token, studySetId).fold(
                ifLeft = { error ->
                    // Handle error
                },
                ifRight = { studySet ->
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        currentIndex = 0,
                        isFlipped = false,
                        learnedCount = 0
                    )
                }
            )
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun nextCard() {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return
        val flashcards = studySet.flashcards
        
        if (currentState.currentIndex < flashcards.size - 1) {
            _uiState.value = currentState.copy(
                currentIndex = currentState.currentIndex + 1,
                isFlipped = false
            )
        }
    }

    fun previousCard() {
        val currentState = _uiState.value
        if (currentState.currentIndex > 0) {
            _uiState.value = currentState.copy(
                currentIndex = currentState.currentIndex - 1,
                isFlipped = false
            )
        }
    }

    fun markAsLearned() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            learnedCount = currentState.learnedCount + 1
        )
    }

    fun goToCard(index: Int) {
        val currentState = _uiState.value
        val studySet = currentState.studySet ?: return
        val flashcards = studySet.flashcards
        
        if (index in 0 until flashcards.size) {
            _uiState.value = currentState.copy(
                currentIndex = index,
                isFlipped = false
            )
        }
    }
}


