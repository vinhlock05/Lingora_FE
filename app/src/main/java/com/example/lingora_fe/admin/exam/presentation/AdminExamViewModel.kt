package com.example.lingora_fe.admin.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.exam.domain.repository.AdminExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminExamViewModel @Inject constructor(
    private val repository: AdminExamRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminExamState())
    val state = _state.asStateFlow()

    private var examSearchJob: kotlinx.coroutines.Job? = null
    private var attemptSearchJob: kotlinx.coroutines.Job? = null

    init {
        loadExams()
    }

    // ==================== Exam Management ====================

    fun loadExams(page: Int = 1) {
        val query = _state.value.examSearchQuery.takeIf { it.isNotBlank() }
        val isPublished = _state.value.filterPublished
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getExams(
                page = page,
                limit = 20,
                search = query, // Restored search support
                isPublished = isPublished
            ).fold(
                { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                { result ->
                    _state.update { it.copy(
                        isLoading = false,
                        exams = result.exams,
                        examCurrentPage = result.currentPage,
                        examTotalPages = result.totalPages
                    ) }
                }
            )
        }
    }

    fun onFilterPublishedChange(published: Boolean?) {
        _state.update { it.copy(filterPublished = published) }
        loadExams(page = 1)
    }

    fun onExamSearchQueryChange(query: String) {
        _state.update { it.copy(examSearchQuery = query) }
        examSearchJob?.cancel()
        examSearchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce 500ms
            loadExams(page = 1)
        }
    }

    fun getExamTemplateJson(): String {
        val referenceMetadata = mapOf(
            "VALID_EXAM_TYPES" to com.example.lingora_fe.admin.exam.data.remote.dto.ExamType.entries,
            "VALID_SECTION_TYPES" to com.example.lingora_fe.admin.exam.data.remote.dto.ExamSectionType.entries,
            "VALID_GROUP_TYPES" to com.example.lingora_fe.admin.exam.data.remote.dto.ExamGroupType.entries,
            "VALID_QUESTION_TYPES" to com.example.lingora_fe.admin.exam.data.remote.dto.ExamQuestionType.entries
        )

        val sampleExam = com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq(
            examType = com.example.lingora_fe.admin.exam.data.remote.dto.ExamType.IELTS,
            code = "EXAM_IELTS_SAMPLE",
            title = "Sample IELTS Exam (With Reference)",
            description = "This is a sample exam template. Please check the 'metadata' field for valid Enum values.",
            totalDurationSeconds = 3600,
            isPublished = false,
            metadata = referenceMetadata,
            sections = listOf(
                com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamSectionReq(
                    sectionType = com.example.lingora_fe.admin.exam.data.remote.dto.ExamSectionType.READING,
                    title = "Reading Section 1",
                    displayOrder = 1,
                    groups = listOf(
                        com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamSectionGroupReq(
                            groupType = com.example.lingora_fe.admin.exam.data.remote.dto.ExamGroupType.PASSAGE,
                            title = "Passage 1",
                            content = "Insert passage text here...",
                            questionGroups = listOf(
                                com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamQuestionGroupReq(
                                    title = "Questions 1-5",
                                    questions = listOf(
                                        com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamQuestionReq(
                                            questionType = com.example.lingora_fe.admin.exam.data.remote.dto.ExamQuestionType.MULTIPLE_CHOICE,
                                            prompt = "What is the capital of Vietnam?",
                                            options = listOf("Hanoi", "Ho Chi Minh City", "Da Nang", "Hue"),
                                            correctAnswer = "Hanoi",
                                            scoreWeight = 1.0
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        return com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(listOf(sampleExam))
    }

    fun importExam(jsonContent: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val gson = com.google.gson.Gson()
                val trimmedJson = jsonContent.trim()
                
                val importReqs: List<com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq> = when {
                    trimmedJson.startsWith("[") -> {
                        val type = object : com.google.gson.reflect.TypeToken<List<com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq>>() {}.type
                        gson.fromJson(trimmedJson, type)
                    }
                    trimmedJson.startsWith("{") -> {
                         val singleReq = gson.fromJson(trimmedJson, com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq::class.java)
                         listOf(singleReq)
                    }
                    else -> throw IllegalArgumentException("JSON must start with '{' or '['")
                }

                repository.importExam(importReqs).fold(
                    { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                    { ids ->
                        _state.update { it.copy(isLoading = false) }
                        loadExams(_state.value.examCurrentPage)
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Invalid JSON format: ${e.message}") }
            }
        }
    }

    fun deleteExam(id: Int) {
         viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.deleteExam(id).fold(
                 { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                 {
                     _state.update { it.copy(isLoading = false) }
                     loadExams(_state.value.examCurrentPage)
                 }
            )
         }
    }

    fun toggleExamStatus(id: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.updateExam(id, isPublished = !currentStatus).fold(
                { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                { 
                     _state.update { it.copy(isLoading = false) }
                     loadExams(_state.value.examCurrentPage) 
                }
            )
        }
    }

    fun loadExamDetail(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, examDetail = null) }
            repository.getExamDetail(id).fold(
                { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                { exam -> _state.update { it.copy(isLoading = false, examDetail = exam) } }
            )
        }
    }

    fun updateExamInfo(id: Int, title: String, code: String, isPublished: Boolean, type: String) {
         viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            // Note: API updateExam accepts title, description, isPublished, code. 
            // Type might not be editable directly via this updateExam or ignored if not supported by API yet.
            // Based on earlier usage of updateExam request dto, it supports title, description, isPublished, thumbnailUrl, code.
            // User request mentioned "PATCH sửa title/description".
            
            repository.updateExam(
                id = id, 
                title = title, 
                code = code, 
                isPublished = isPublished
            ).fold(
                { error -> _state.update { it.copy(isUpdating = false, error = error.message) } },
                { 
                     _state.update { it.copy(isUpdating = false, isEditExamDialogVisible = false) }
                     loadExamDetail(id) // Reload detail
                     loadExams(_state.value.examCurrentPage) // Refresh list if needed
                }
            )
        }
    }

    fun showEditExamDialog() {
        _state.update { it.copy(isEditExamDialogVisible = true) }
    }

    fun hideEditExamDialog() {
        _state.update { it.copy(isEditExamDialogVisible = false) }
    }

    // ==================== Attempts Monitoring ====================

    fun loadAttempts(page: Int = 1) {
        val query = _state.value.attemptSearchQuery.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getExamAttempts(
                page = page,
                limit = 20,
                search = query
            ).fold(
                { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                { result ->
                    _state.update { it.copy(
                        isLoading = false,
                        attempts = result.attempts,
                        attemptCurrentPage = result.currentPage,
                        attemptTotalPages = result.totalPages
                    ) }
                }
            )
        }
    }

    fun onAttemptSearchQueryChange(query: String) {
        _state.update { it.copy(attemptSearchQuery = query) }
        attemptSearchJob?.cancel()
        attemptSearchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            loadAttempts(page = 1)
        }
    }

    fun loadAttemptDetail(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, attemptDetail = null) }
            repository.getExamAttemptDetail(id).fold(
                { error -> _state.update { it.copy(isLoading = false, error = error.message) } },
                { attempt -> _state.update { it.copy(isLoading = false, attemptDetail = attempt) } }
            )
        }
    }


    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
