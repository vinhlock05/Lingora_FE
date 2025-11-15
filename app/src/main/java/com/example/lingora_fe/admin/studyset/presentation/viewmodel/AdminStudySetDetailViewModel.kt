package com.example.lingora_fe.admin.studyset.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.studyset.domain.repository.AdminStudySetRepository
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.studyset.presentation.StudySetDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudySetDetailViewModel @Inject constructor(
    private val repository: AdminStudySetRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int = savedStateHandle.get<Int>("studySetId") ?: 0

    private val _uiState = MutableStateFlow(StudySetDetailUiState())
    val uiState: StateFlow<StudySetDetailUiState> = _uiState.asStateFlow()

    init {
        loadStudySet()
    }

    fun loadStudySet() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getStudySetById(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = { studySet ->
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        isLoading = false,
                        error = null
                    )
                }
            )
        }
    }

    fun refresh() {
        loadStudySet()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

