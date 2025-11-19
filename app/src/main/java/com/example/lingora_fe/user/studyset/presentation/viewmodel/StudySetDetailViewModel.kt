package com.example.lingora_fe.user.studyset.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import com.example.lingora_fe.user.studyset.presentation.StudySetDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySetDetailViewModel @Inject constructor(
    private val repository: StudySetRepository,
    private val forumRepository: ForumRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studySetId: Int = savedStateHandle.get<Int>("studySetId") ?: 0

    private val _uiState = MutableStateFlow(StudySetDetailUiState())
    val uiState: StateFlow<StudySetDetailUiState> = _uiState.asStateFlow()
    
    fun getCurrentUserId(): Int? {
        return tokenManager.getUserId()
    }
    
    fun isOwner(): Boolean {
        val currentUserId = tokenManager.getUserId()
        val studySet = _uiState.value.studySet
        Log.d("StudySetDetailViewModel", "$studySet")
        Log.d("StudySetDetailViewModel", "isOwner: currentUserId=$currentUserId, studySetOwnerId=${studySet?.owner?.id}")
        return currentUserId != null && studySet != null && studySet.owner.id == currentUserId
    }
    
    fun deleteStudySet(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.deleteStudySet(token, studySetId).fold(
                ifLeft = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                },
                ifRight = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
            )
        }
    }

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

    fun toggleLike(studySetId: Int) {
        viewModelScope.launch {
            val studySet = _uiState.value.studySet ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLiking = true)
            
            // Optimistic update
            val isCurrentlyLiked = studySet.isAlreadyLike
            val delta = if (isCurrentlyLiked) -1 else 1
            
            _uiState.value = _uiState.value.copy(
                studySet = studySet.copy(
                    isAlreadyLike = !isCurrentlyLiked,
                    likeCount = (studySet.likeCount + delta).coerceAtLeast(0)
                ),
                isLiking = false
            )
            
            val action = if (isCurrentlyLiked) {
                forumRepository.unlike(studySetId, "STUDY_SET")
            } else {
                forumRepository.like(studySetId, "STUDY_SET")
            }
            
            action.fold(
                ifLeft = { error ->
                    // Revert optimistic update on error
                    _uiState.value = _uiState.value.copy(
                        studySet = studySet,
                        error = error.message ?: "Failed to toggle like",
                        isLiking = false
                    )
                },
                ifRight = {
                    // Reload to get accurate state
                    loadStudySet()
                }
            )
        }
    }
}

