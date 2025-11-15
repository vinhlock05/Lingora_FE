package com.example.lingora_fe.admin.studyset.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.studyset.domain.repository.AdminStudySetRepository
import com.example.lingora_fe.core.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudySetManagementViewModel @Inject constructor(
    private val repository: AdminStudySetRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AdminStudySetManagementState())
    val state: StateFlow<AdminStudySetManagementState> = _state.asStateFlow()

    fun onEvent(event: AdminStudySetManagementEvent) {
        when (event) {
            is AdminStudySetManagementEvent.LoadStudySets -> loadStudySets(event.page)
            is AdminStudySetManagementEvent.SearchStudySets -> searchStudySets(event.query)
            is AdminStudySetManagementEvent.FilterByStatus -> filterByStatus(event.filter)
            is AdminStudySetManagementEvent.RefreshStudySets -> refreshStudySets()
            
            is AdminStudySetManagementEvent.LoadStudySetDetails -> loadStudySetDetails(event.studySetId)
            is AdminStudySetManagementEvent.ClearSelectedStudySet -> clearSelectedStudySet()
            
            is AdminStudySetManagementEvent.ApproveStudySet -> approveStudySet(event.studySetId)
            is AdminStudySetManagementEvent.RejectStudySet -> rejectStudySet(event.studySetId, event.reason)
            
            is AdminStudySetManagementEvent.ClearError -> clearError()
            is AdminStudySetManagementEvent.ClearActionMessages -> clearActionMessages()
        }
    }

    private fun loadStudySets(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val token = tokenManager.getAccessToken() ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication token not found"
                )
                return@launch
            }

            val filterStatus = _state.value.filterStatus?.status?.value

            repository.getAllStudySets(
                token = token,
                limit = 10,
                page = page,
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                visibility = null,
                status = filterStatus,
                minPrice = null,
                maxPrice = null,
                sort = null
            )
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        studySets = metadata.studySets,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        total = metadata.total,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun searchStudySets(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadStudySets(page = 1)
    }

    private fun filterByStatus(filter: StudySetFilter?) {
        _state.value = _state.value.copy(filterStatus = filter)
        loadStudySets(page = 1)
    }

    private fun refreshStudySets() {
        loadStudySets(_state.value.currentPage)
    }

    private fun loadStudySetDetails(studySetId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isStudySetDetailsLoading = true)
            
            val token = tokenManager.getAccessToken() ?: return@launch

            repository.getStudySetById(token, studySetId)
                .onRight { studySet ->
                    _state.value = _state.value.copy(
                        selectedStudySet = studySet,
                        isStudySetDetailsLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isStudySetDetailsLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun clearSelectedStudySet() {
        _state.value = _state.value.copy(selectedStudySet = null)
    }

    private fun approveStudySet(studySetId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isApproving = true, actionError = null)
            
            val token = tokenManager.getAccessToken() ?: return@launch

            repository.approveStudySet(token, studySetId)
                .onRight {
                    _state.value = _state.value.copy(
                        isApproving = false,
                        actionSuccess = "Study set đã được duyệt thành công"
                    )
                    loadStudySets(_state.value.currentPage)
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isApproving = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun rejectStudySet(studySetId: Int, reason: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRejecting = true, actionError = null)
            
            val token = tokenManager.getAccessToken() ?: return@launch

            repository.rejectStudySet(token, studySetId, reason)
                .onRight {
                    _state.value = _state.value.copy(
                        isRejecting = false,
                        actionSuccess = "Study set đã bị từ chối"
                    )
                    loadStudySets(_state.value.currentPage)
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isRejecting = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun clearActionMessages() {
        _state.value = _state.value.copy(
            actionSuccess = null,
            actionError = null
        )
    }
}

