package com.example.lingora_fe.user.classroom.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.UploadRepository
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.LessonAttachmentType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val uploadRepository: UploadRepository,
    private val studySetRepository: com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager,
    @ApplicationContext private val context: Context,
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

    private val isTeacher: Boolean = savedStateHandle.get<String>("isTeacher")?.toBoolean() ?: false

    private val _state = MutableStateFlow(LessonDetailState(isTeacher = isTeacher))
    val state: StateFlow<LessonDetailState> = _state.asStateFlow()

    init {
        loadLessonDetail()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun refresh() {
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
            flashcardExample = "",
            flashcardImageUrl = ""
        )
    }

    fun hideAddFlashcardDialog() {
        _state.value = _state.value.copy(
            showAddFlashcardDialog = false,
            editingFlashcard = null,
            flashcardFront = "",
            flashcardBack = "",
            flashcardExample = "",
            flashcardImageUrl = ""
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

    fun onFlashcardImageUrlChange(text: String) {
        _state.value = _state.value.copy(flashcardImageUrl = text)
    }

    fun saveFlashcard(uploadedImageUrl: String? = null, removeImage: Boolean = false) {
        val current = _state.value
        if (current.flashcardFront.isBlank() || current.flashcardBack.isBlank()) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingFlashcard = true)

            val isEditing = current.editingFlashcard != null

            if (isEditing) {
                updateFlashcard(uploadedImageUrl = uploadedImageUrl, removeImage = removeImage)
            } else {
                createFlashcard(uploadedImageUrl = uploadedImageUrl)
            }
        }
    }

    private suspend fun createFlashcard(uploadedImageUrl: String? = null) {
        val current = _state.value
        repository.createFlashcard(
            classroomId = classroomId,
            lessonId = lessonId,
            frontText = current.flashcardFront.trim(),
            backText = current.flashcardBack.trim(),
            example = current.flashcardExample.trim().takeIf { it.isNotEmpty() },
            imageUrl = uploadedImageUrl ?: current.flashcardImageUrl.trim().takeIf { it.isNotEmpty() }
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
                    flashcardExample = "",
                    flashcardImageUrl = ""
                )
                loadLessonDetail()
            }
        )
    }

    private suspend fun updateFlashcard(uploadedImageUrl: String? = null, removeImage: Boolean = false) {
        val current = _state.value
        val flashcard = current.editingFlashcard ?: return
        val finalImageUrl = when {
            uploadedImageUrl != null -> uploadedImageUrl
            removeImage -> null
            else -> current.flashcardImageUrl.trim().takeIf { it.isNotEmpty() }
        }

        repository.updateFlashcard(
            classroomId = classroomId,
            lessonId = lessonId,
            flashcardId = flashcard.id,
            frontText = current.flashcardFront.trim(),
            backText = current.flashcardBack.trim(),
            example = current.flashcardExample.trim().takeIf { it.isNotEmpty() },
            imageUrl = finalImageUrl
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
                    flashcardExample = "",
                    flashcardImageUrl = ""
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
            flashcardExample = flashcard.example ?: "",
            flashcardImageUrl = flashcard.imageUrl ?: ""
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

        studySetRepository.getAllStudySets(
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
                        showImportStudySetDialog = false,
                        selectedStudySetId = null,
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

    // ─── Attachment ──────────────────────────────────────────────────────────

    fun showAddAttachmentDialog() {
        _state.value = _state.value.copy(
            showAddAttachmentDialog = true,
            attachmentFileUrl = "",
            attachmentFileName = "",
            attachmentTitle = "",
            attachmentMimeType = "",
            attachmentFileSizeBytes = null,
            attachmentDurationSeconds = null,
            attachmentFileType = com.example.lingora_fe.user.classroom.util.LessonAttachmentType.OTHER,
            attachmentRole = com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.DOWNLOAD
        )
    }

    /**
     * Gọi khi user đã chọn file từ file picker.
     * Upload lên server, rồi tự động điền các field.
     */
    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingAttachment = true)
            uploadRepository.uploadFromUri(context, uri).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isUploadingAttachment = false,
                        error = error.message ?: "Upload thất bại"
                    )
                },
                ifRight = { result ->
                    // Xác định fileType từ mimeType
                    val fileType = when {
                        result.mimeType.startsWith("video/") -> LessonAttachmentType.VIDEO
                        result.mimeType.startsWith("audio/") -> LessonAttachmentType.AUDIO
                        result.mimeType == "application/pdf" -> LessonAttachmentType.PDF
                        result.mimeType.startsWith("image/") -> LessonAttachmentType.IMAGE
                        result.mimeType.contains("word") ||
                        result.mimeType.contains("document") ||
                        result.mimeType.contains("sheet") ||
                        result.mimeType.contains("presentation") -> LessonAttachmentType.DOCUMENT
                        else -> LessonAttachmentType.OTHER
                    }
                    val role = when (fileType) {
                        LessonAttachmentType.VIDEO,
                        LessonAttachmentType.AUDIO,
                        LessonAttachmentType.IMAGE ->
                            com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.INLINE
                        else ->
                            com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.DOWNLOAD
                    }

                    _state.value = _state.value.copy(
                        isUploadingAttachment = false,
                        attachmentFileUrl = result.url,
                        attachmentFileName = result.fileName,
                        attachmentMimeType = result.mimeType,
                        attachmentFileSizeBytes = result.fileSizeBytes,
                        attachmentFileType = fileType,
                        attachmentRole = role
                    )
                }
            )
        }
    }

    fun hideAddAttachmentDialog() {
        _state.value = _state.value.copy(showAddAttachmentDialog = false)
    }

    fun onAttachmentFileUrlChange(value: String) { _state.value = _state.value.copy(attachmentFileUrl = value) }
    fun onAttachmentFileNameChange(value: String) { _state.value = _state.value.copy(attachmentFileName = value) }
    fun onAttachmentTitleChange(value: String) { _state.value = _state.value.copy(attachmentTitle = value) }
    fun onAttachmentMimeTypeChange(value: String) { _state.value = _state.value.copy(attachmentMimeType = value) }
    fun onAttachmentFileSizeChange(value: Long?) { _state.value = _state.value.copy(attachmentFileSizeBytes = value) }
    fun onAttachmentDurationChange(value: Int?) { _state.value = _state.value.copy(attachmentDurationSeconds = value) }
    fun onAttachmentFileTypeChange(value: com.example.lingora_fe.user.classroom.util.LessonAttachmentType) {
        _state.value = _state.value.copy(
            attachmentFileType = value,
            // Video/Audio/Image → INLINE; PDF/Document/Other → DOWNLOAD
            attachmentRole = when (value) {
                com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO,
                com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO,
                com.example.lingora_fe.user.classroom.util.LessonAttachmentType.IMAGE ->
                    com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.INLINE
                else ->
                    com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.DOWNLOAD
            }
        )
    }
    fun onAttachmentRoleChange(value: com.example.lingora_fe.user.classroom.util.LessonAttachmentRole) {
        _state.value = _state.value.copy(attachmentRole = value)
    }

    fun saveAttachment() {
        val current = _state.value
        if (current.attachmentFileUrl.isBlank() || current.attachmentFileName.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingAttachment = true)
            repository.addAttachment(
                classroomId = classroomId,
                lessonId = lessonId,
                role = current.attachmentRole.value,
                fileUrl = current.attachmentFileUrl.trim(),
                fileType = current.attachmentFileType.value,
                fileName = current.attachmentFileName.trim(),
                mimeType = current.attachmentMimeType.trim().takeIf { it.isNotEmpty() },
                fileSizeBytes = current.attachmentFileSizeBytes,
                durationSeconds = current.attachmentDurationSeconds,
                title = current.attachmentTitle.trim().takeIf { it.isNotEmpty() }
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isUploadingAttachment = false,
                        error = error.message ?: "Không thể thêm attachment"
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(
                        isUploadingAttachment = false,
                        showAddAttachmentDialog = false
                    )
                    loadLessonDetail()
                }
            )
        }
    }

    fun confirmDeleteAttachment(attachment: com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment) {
        _state.value = _state.value.copy(attachmentToDelete = attachment)
    }

    fun cancelDeleteAttachment() {
        _state.value = _state.value.copy(attachmentToDelete = null)
    }

    fun deleteAttachment() {
        val attachment = _state.value.attachmentToDelete ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeletingAttachment = true, attachmentToDelete = null)
            repository.deleteAttachment(
                classroomId = classroomId,
                lessonId = lessonId,
                attachmentId = attachment.id
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isDeletingAttachment = false,
                        error = error.message ?: "Không thể xóa attachment"
                    )
                },
                ifRight = {
                    _state.value = _state.value.copy(isDeletingAttachment = false)
                    loadLessonDetail()
                }
            )
        }
    }
}
