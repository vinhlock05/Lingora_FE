package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class ClassroomListViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager,
    private val socketManager: com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClassroomListState())
    val state: StateFlow<ClassroomListState> = _state.asStateFlow()

    private val _joinSuccessEvent = MutableStateFlow<Int?>(null)
    val joinSuccessEvent: StateFlow<Int?> = _joinSuccessEvent.asStateFlow()

    private val _events = MutableSharedFlow<ClassroomListEvent>()
    val events: SharedFlow<ClassroomListEvent> = _events.asSharedFlow()

    init {
        val userId = tokenManager.getUserId()
        _state.value = _state.value.copy(currentUserId = userId)
        loadClassrooms()
        listenForApprovals()
    }

    private fun listenForApprovals() {
        viewModelScope.launch {
            socketManager.classroomApprovalFlow()
                .catch { /* ignore */ }
                .collect { json ->
                    val message = json.optString("message") ?: "Giáo viên đã duyệt bạn vào lớp."
                    _events.emit(ClassroomListEvent.ShowToast(message))
                    if (_state.value.selectedTab == 1) {
                        loadClassrooms(_state.value.currentPage)
                    }
                }
        }
    }

    fun onEvent(event: ClassroomListEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun loadClassrooms(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val currentState = _state.value
            val isPublic = if (currentState.selectedTab == 0) true else null
            val search = currentState.searchQuery.takeIf { it.isNotEmpty() }
            
            // selectedStatusFilter mappings for "Của tôi" (tab = 1):
            // 0 -> Tất cả (teacher + student, so teacherId = null, membership = ALL)
            // 1 -> Đã tạo (membership = TEACHER)
            // 2 -> Đã tham gia (membership = STUDENT)
            
            val membership = if (currentState.selectedTab == 1) {
                when (currentState.selectedStatusFilter) {
                    0 -> "ALL"
                    1 -> "TEACHER"
                    2 -> "STUDENT"
                    else -> "ALL"
                }
            } else {
                null
            }

            val status = if (currentState.selectedTab == 1) {
                null // Fetch all statuses for my classes or maybe active only? Usually you see all your classes
            } else {
                "ACTIVE" // For Discovery tab, only show ACTIVE classrooms.
            }

            repository.getAllClassrooms(
                page = page,
                limit = 20,
                search = search,
                isPublic = isPublic,
                status = status,
                teacherId = null,
                membership = membership,
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

    fun promptJoinPublicClass(classroom: com.example.lingora_fe.user.classroom.domain.model.Classroom) {
        _state.value = _state.value.copy(publicClassToJoin = classroom)
    }

    fun cancelJoinPublicClass() {
        _state.value = _state.value.copy(publicClassToJoin = null, joinError = null)
    }

    fun joinPublicClass() {
        val classroom = _state.value.publicClassToJoin ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isJoining = true, joinError = null)
            repository.joinClassroomByCode(classroom.code).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isJoining = false,
                        joinError = error.message ?: "Không thể tham gia lớp học",
                        publicClassToJoin = null 
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(
                        isJoining = false,
                        publicClassToJoin = null,
                        joinError = null
                    )
                    _joinSuccessEvent.value = classroom.id
                    loadClassrooms(1)
                }
            )
        }
    }

    fun clearJoinSuccessEvent() {
        _joinSuccessEvent.value = null
    }
}
