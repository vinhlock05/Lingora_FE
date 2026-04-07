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
class CreateClassroomViewModel @Inject constructor(
    private val repository: ClassroomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateClassroomState())
    val state: StateFlow<CreateClassroomState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onIsPublicChange(isPublic: Boolean) {
        _state.value = _state.value.copy(isPublic = isPublic)
    }

    fun onMaxStudentsChange(value: String) {
        _state.value = _state.value.copy(maxStudents = value.toIntOrNull())
    }

    fun submit() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.value = current.copy(error = "Tên lớp học không được để trống")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            repository.createClassroom(
                name = current.name.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                isPublic = current.isPublic,
                maxStudents = current.maxStudents
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tạo lớp học"
                    )
                },
                ifRight = { classroom ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdClassroomId = classroom.id
                    )
                }
            )
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }
}
