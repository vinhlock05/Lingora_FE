package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val studySetRepository: com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val lessonId: Int =
        requireNotNull(savedStateHandle.get<String>("lessonId")?.toIntOrNull()) {
            "lessonId is required"
        }

    private val _state = MutableStateFlow(LessonDetailState())
    val state: StateFlow<LessonDetailState> = _state.asStateFlow()

    init {
        loadLessonDetail()
    }

    private fun loadLessonDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getLessonDetail(classroomId, lessonId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải bài học"
                    )
                },
                ifRight = { lesson ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        lesson = lesson
                    )
                }
            )
        }
    }

    fun showAddFlashcardDialog() {
        _state.value = _state.value.copy(
            showAddFlashcardDialog = true,
            editingFlashcard = null,
            flashcardFront = "",
            flashcardBack = "",
            flashcardExample = ""
        )
    }

    fun hideAddFlashcardDialog() {
        _state.value = _state.value.copy(
            showAddFlashcardDialog = false,
            editingFlashcard = null,
            flashcardFront = "",
            flashcardBack = "",
            flashcardExample = ""
        )
    }

    fun onFlashcardFrontChange(text: String) {
        _state.value = _state.value.copy(flashcardFront = text)
    }

    fun onFlashcardBackChange(text: String) {
        _state.value = _state.value.copy(flashcardBack = text)
    }

    fun onFlashcardExampleChange(text: String) {
        _state.value = _state.value.copy(flashcardExample = text)
    }

    fun saveFlashcard() {
        val current = _state.value
        if (current.flashcardFront.isBlank() || current.flashcardBack.isBlank()) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingFlashcard = true)

            val isEditing = current.editingFlashcard != null

            if (isEditing) {
                updateFlashcard()
            } else {
                createFlashcard()
            }
        }
    }

    private suspend fun createFlashcard() {
        val current = _state.value
        repository.createFlashcard(
            classroomId = classroomId,
            lessonId = lessonId,
            frontText = current.flashcardFront.trim(),
            backText = current.flashcardBack.trim(),
            example = current.flashcardExample.trim().takeIf { it.isNotEmpty() }
        ).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(isSavingFlashcard = false)
            },
            ifRight = { _ ->
                _state.value = _state.value.copy(
                    isSavingFlashcard = false,
                    showAddFlashcardDialog = false,
                    flashcardFront = "",
                    flashcardBack = "",
                    flashcardExample = ""
                )
                loadLessonDetail()
            }
        )
    }

    private suspend fun updateFlashcard() {
        val current = _state.value
        val flashcard = current.editingFlashcard ?: return

        repository.updateFlashcard(
            classroomId = classroomId,
            lessonId = lessonId,
            flashcardId = flashcard.id,
            frontText = current.flashcardFront.trim(),
            backText = current.flashcardBack.trim(),
            example = current.flashcardExample.trim().takeIf { it.isNotEmpty() }
        ).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(isSavingFlashcard = false)
            },
            ifRight = { _ ->
                _state.value = _state.value.copy(
                    isSavingFlashcard = false,
                    showAddFlashcardDialog = false,
                    editingFlashcard = null,
                    flashcardFront = "",
                    flashcardBack = "",
                    flashcardExample = ""
                )
                loadLessonDetail()
            }
        )
    }

    fun deleteFlashcard(flashcardId: Int) {
        viewModelScope.launch {
            repository.deleteFlashcard(
                classroomId = classroomId,
                lessonId = lessonId,
                flashcardId = flashcardId
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa flashcard"
                    )
                },
                ifRight = {
                    loadLessonDetail()
                }
            )
        }
    }

    fun editFlashcard(flashcard: ClassroomFlashcard) {
        _state.value = _state.value.copy(
            showAddFlashcardDialog = true,
            editingFlashcard = flashcard,
            flashcardFront = flashcard.frontText,
            flashcardBack = flashcard.backText,
            flashcardExample = flashcard.example ?: ""
        )
    }

    fun showImportStudySetDialog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                showImportStudySetDialog = true,
                isLoadingStudySets = true,
                selectedStudySetId = null
            )
            loadStudySets()
        }
    }

    fun hideImportStudySetDialog() {
        _state.value = _state.value.copy(
            showImportStudySetDialog = false,
            selectedStudySetId = null,
            isImporting = false
        )
    }

    fun onStudySetSelected(studySetId: Int) {
        _state.value = _state.value.copy(selectedStudySetId = studySetId)
    }

    private suspend fun loadStudySets() {
        val filterOptions = com.example.lingora_fe.user.studyset.domain.model.StudySetFilterOptions(
            page = 1,
            limit = 100
        )

        studySetRepository.getOwnStudySets(
            token = tokenManager.getAccessToken() ?: "",
            filterOptions = filterOptions
        ).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(
                    isLoadingStudySets = false,
                    error = error.message ?: "Không thể tải StudySet"
                )
            },
            ifRight = { metadata ->
                val options = metadata.studySets.map { studySet ->
                    StudySetOption(
                        id = studySet.id,
                        title = studySet.title
                    )
                }
                _state.value = _state.value.copy(
                    isLoadingStudySets = false,
                    studySetOptions = options
                )
            }
        )
    }

    fun importFromStudySet() {
        val studySetId = _state.value.selectedStudySetId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true)

            repository.importFlashcardsFromStudySet(
                classroomId = classroomId,
                lessonId = lessonId,
                studySetId = studySetId
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        error = error.message ?: "Không thể import flashcard"
                    )
                },
                ifRight = { lesson ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        showImportStudySetDialog = false,
                        selectedStudySetId = null,
                        lesson = lesson
                    )
                }
            )
        }
    }
}
