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
    private val repository: ClassroomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClassroomListState())
    val state: StateFlow<ClassroomListState> = _state.asStateFlow()

    init {
        loadClassrooms()
    }

    fun loadClassrooms(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val currentState = _state.value
            val isPublic = if (currentState.selectedTab == 0) true else null
            val search = currentState.searchQuery.takeIf { it.isNotEmpty() }

            repository.getAllClassrooms(
                page = page,
                limit = 20,
                search = search,
                isPublic = isPublic,
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
}
