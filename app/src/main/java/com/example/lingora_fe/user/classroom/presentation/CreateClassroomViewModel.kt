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
    private val repository: ClassroomRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateClassroomState())
    val state: StateFlow<CreateClassroomState> = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("classroomId")?.toIntOrNull()?.let { id ->
            _state.value = _state.value.copy(classroomId = id, isEditMode = true)
            loadClassroomDetails(id)
        }
    }

    private fun loadClassroomDetails(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getClassroomById(id).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải thông tin lớp học"
                    )
                },
                ifRight = { classroom ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        name = classroom.name,
                        description = classroom.description ?: "",
                        isPublic = classroom.isPublic,
                        maxStudents = classroom.maxStudents,
                        status = classroom.status,
                        coverImageUri = classroom.coverImageUrl
                    )
                }
            )
        }
    }

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

    fun onStatusChange(status: com.example.lingora_fe.user.classroom.util.ClassroomStatus) {
        _state.value = _state.value.copy(status = status)
    }

    fun onCoverImageUriChange(uri: String?) {
        _state.value = _state.value.copy(coverImageUri = uri)
    }

    fun submit(context: android.content.Context) {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.value = current.copy(error = "Tên lớp học không được để trống")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            var coverImageUrl: String? = current.coverImageUri
            // Only upload if it's a new local URI (starts with content:// or file://)
            if (current.coverImageUri != null && (current.coverImageUri.startsWith("content://") || current.coverImageUri.startsWith("file://"))) {
                val uri = android.net.Uri.parse(current.coverImageUri)
                com.example.lingora_fe.util.FileUploadHelper.uploadImage(context, uri).fold(
                    ifLeft = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Không thể tải ảnh lên: ${error.message}"
                        )
                        return@launch
                    },
                    ifRight = { url ->
                        coverImageUrl = url
                    }
                )
            }

            val result = if (current.isEditMode && current.classroomId != null) {
                repository.updateClassroom(
                    id = current.classroomId,
                    name = current.name.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    isPublic = current.isPublic,
                    maxStudents = current.maxStudents,
                    status = current.status.name,
                    coverImageUrl = coverImageUrl
                )
            } else {
                repository.createClassroom(
                    name = current.name.trim(),
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    isPublic = current.isPublic,
                    maxStudents = current.maxStudents,
                    status = current.status.name,
                    coverImageUrl = coverImageUrl
                )
            }

            result.fold(
                ifLeft = { error ->
                    val action = if (current.isEditMode) "cập nhật" else "tạo"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể ${action} lớp học"
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
