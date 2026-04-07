package com.example.lingora_fe.user.classroom.presentation

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
class ClassroomListViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClassroomListState())
    val state: StateFlow<ClassroomListState> = _state.asStateFlow()

    init {
        val userId = tokenManager.getUserId()
        _state.value = _state.value.copy(currentUserId = userId)
        loadClassrooms()
    }

    fun loadClassrooms(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val currentState = _state.value
            val isPublic = if (currentState.selectedTab == 0) true else null
            val search = currentState.searchQuery.takeIf { it.isNotEmpty() }
            
            val teacherId = if (currentState.selectedTab == 1) currentState.currentUserId else null
            val status = if (currentState.selectedTab == 1) {
                when (currentState.selectedStatusFilter) {
                    1 -> "ACTIVE"
                    2 -> "ARCHIVED"
                    3 -> "DRAFT"
                    else -> null
                }
            } else {
                "ACTIVE" // Default to show only Active for Discovery tab
            }

            repository.getAllClassrooms(
                page = page,
                limit = 20,
                search = search,
                isPublic = isPublic,
                status = status,
                teacherId = teacherId,
                sort = "-createdAt"
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải danh sách lớp học"
                    )
                },
                ifRight = { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        classrooms = result.classrooms,
                        currentPage = result.currentPage,
                        totalPages = result.totalPages,
                        total = result.total
                    )
                }
            )
        }
    }

    fun onStatusFilterChange(filter: Int) {
        if (_state.value.selectedStatusFilter == filter) return
        _state.value = _state.value.copy(selectedStatusFilter = filter, currentPage = 1)
        loadClassrooms(1)
    }

    fun archiveClassroom(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.updateClassroom(
                id = id,
                status = "ARCHIVED"
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể lưu trữ lớp học"
                    )
                },
                ifRight = {
                    loadClassrooms(_state.value.currentPage)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun applySearch() {
        loadClassrooms(1)
    }

    fun selectTab(tab: Int) {
        if (_state.value.selectedTab == tab) return
        _state.value = _state.value.copy(selectedTab = tab, currentPage = 1)
        loadClassrooms(1)
    }

    fun deleteClassroom(id: Int) {
        viewModelScope.launch {
            repository.deleteClassroom(id).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa lớp học"
                    )
                },
                ifRight = {
                    val filtered = _state.value.classrooms.filterNot { it.id == id }
                    _state.value = _state.value.copy(classrooms = filtered)
                }
            )
        }
    }

    fun refresh() {
        loadClassrooms(_state.value.currentPage)
    }

    fun showJoinDialog() {
        _state.value = _state.value.copy(
            showJoinDialog = true,
            joinCode = "",
            joinError = null
        )
    }

    fun dismissJoinDialog() {
        _state.value = _state.value.copy(
            showJoinDialog = false,
            joinCode = "",
            joinError = null,
            isJoining = false
        )
    }

    fun onJoinCodeChange(code: String) {
        _state.value = _state.value.copy(joinCode = code)
    }

    fun joinByCode() {
        viewModelScope.launch {
            val code = _state.value.joinCode.trim()
            if (code.isEmpty()) {
                _state.value = _state.value.copy(
                    joinError = "Vui lòng nhập mã lớp học"
                )
                return@launch
            }

            _state.value = _state.value.copy(isJoining = true, joinError = null)

            repository.joinClassroomByCode(code).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isJoining = false,
                        joinError = error.message ?: "Không thể tham gia lớp học"
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(
                        isJoining = false,
                        showJoinDialog = false,
                        joinCode = "",
                        joinError = null
                    )
                    loadClassrooms(1)
                }
            )
        }
    }
}
