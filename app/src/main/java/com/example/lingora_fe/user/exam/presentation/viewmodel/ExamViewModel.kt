package com.example.lingora_fe.user.exam.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.user.exam.domain.repository.ExamRepository
import com.example.lingora_fe.user.exam.domain.repository.AnswerPayload
import com.example.lingora_fe.user.ranking.presentation.XpRewardTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lingora_fe.user.exam.domain.model.ExamAttempt

data class AttemptListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val attempts: List<ExamAttempt> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0
)

data class AttemptDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val detail: com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto? = null
)

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val tokenManager: TokenManager,
    private val xpRewardTracker: XpRewardTracker
) : ViewModel() {

    private val _listState = MutableStateFlow(ExamListUiState())
    val listState: StateFlow<ExamListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ExamDetailUiState())
    val detailState: StateFlow<ExamDetailUiState> = _detailState.asStateFlow()

    private val _sectionState = MutableStateFlow(ExamSectionUiState())
    val sectionState: StateFlow<ExamSectionUiState> = _sectionState.asStateFlow()

    private val _attemptsState = MutableStateFlow(AttemptListUiState())
    val attemptsState: StateFlow<AttemptListUiState> = _attemptsState.asStateFlow()

    private val _attemptDetailState = MutableStateFlow(AttemptDetailUiState())
    val attemptDetailState: StateFlow<AttemptDetailUiState> = _attemptDetailState.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    // Full test flow - track completed sections locally
    private val _completedSections = MutableStateFlow<Set<Int>>(emptySet())
    val completedSections: StateFlow<Set<Int>> = _completedSections.asStateFlow()
    
    // Track when full test is submitted successfully
    private val _fullTestSubmitted = MutableStateFlow(false)
    val fullTestSubmitted: StateFlow<Boolean> = _fullTestSubmitted.asStateFlow()
    
    fun markSectionCompleted(sectionId: Int) {
        _completedSections.value = _completedSections.value + sectionId
    }
    
    fun clearCompletedSections() {
        _completedSections.value = emptySet()
    }
    
    fun isSectionCompleted(sectionId: Int): Boolean {
        return sectionId in _completedSections.value
    }
    
    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }
    
    // Set existing attempt ID for FULL mode (passed from navigation)
    fun setExistingAttemptId(attemptId: Int) {
        _detailState.value = _detailState.value.copy(attemptId = attemptId)
        Log.d("ExamViewModel", "Set existing attemptId from navigation: $attemptId")
    }
    
    // Clear detail message and reset full test state
    fun clearDetailMessage() {
        _detailState.value = _detailState.value.copy(message = null, attemptId = null)
        _fullTestSubmitted.value = false
    }

    fun loadExams(page: Int = 1) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _listState.value = _listState.value.copy(isLoading = true, error = null)
            val filter = _listState.value.filter.copy(page = page)
            repository.getExams(token, filter).fold(
                ifLeft = { e ->
                    _listState.value = _listState.value.copy(isLoading = false, error = e.message)
                },
                ifRight = { meta ->
                    _listState.value = _listState.value.copy(
                        exams = if (page == 1) meta.exams else _listState.value.exams + meta.exams,
                        isLoading = false,
                        error = null,
                        currentPage = meta.currentPage,
                        totalPages = meta.totalPages,
                        total = meta.total
                    )
                }
            )
        }
    }

    fun loadSection(examId: Int, sectionId: Int) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _sectionState.value = _sectionState.value.copy(isLoading = true, error = null, message = null)
            repository.getSection(token, examId, sectionId).fold(
                ifLeft = { e ->
                    _sectionState.value = _sectionState.value.copy(isLoading = false, error = e.message)
                },
                ifRight = { section ->
                    _sectionState.value = _sectionState.value.copy(isLoading = false, section = section, answers = emptyMap(), error = null)
                }
            )
        }
    }

    fun loadAttempts(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _attemptsState.value = _attemptsState.value.copy(isLoading = true, error = null)
            repository.getAttempts(token, page, limit).fold(
                ifLeft = { e ->
                    _attemptsState.value = _attemptsState.value.copy(isLoading = false, error = e.message)
                },
                ifRight = { meta ->
                    _attemptsState.value = _attemptsState.value.copy(
                        attempts = if (page == 1) meta.attempts else _attemptsState.value.attempts + meta.attempts,
                        isLoading = false,
                        error = null,
                        currentPage = meta.currentPage,
                        totalPages = meta.totalPages,
                        total = meta.total
                    )
                }
            )
        }
    }

    fun loadAttemptDetail(attemptId: Int) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _attemptDetailState.value = _attemptDetailState.value.copy(isLoading = true, error = null)
            repository.getAttemptDetail(token, attemptId).fold(
                ifLeft = { e ->
                    _attemptDetailState.value = _attemptDetailState.value.copy(isLoading = false, error = e.message)
                },
                ifRight = { detail ->
                    Log.d("ExamViewModel", "Loaded attempt detail: $detail")
                    _attemptDetailState.value = _attemptDetailState.value.copy(isLoading = false, detail = detail)
                }
            )
        }
    }

    fun updateAnswer(questionId: Int, value: Any?) {
        Log.d("ExamViewModel", "updateAnswer: questionId=$questionId, value=$value")
        val current = _sectionState.value
        _sectionState.value = current.copy(answers = current.answers.toMutableMap().apply { put(questionId, value) })
        Log.d("ExamViewModel", "answers after update: ${_sectionState.value.answers}")
    }

    fun submitCurrentSection(examId: Int) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val sectionId = _sectionState.value.section?.id ?: return@launch
            _sectionState.value = _sectionState.value.copy(isSubmitting = true, message = null)
            val currentAnswers = _sectionState.value.answers
            Log.d("ExamViewModel", "submitCurrentSection: currentAnswers = $currentAnswers")
            val allQuestions = _sectionState.value.section?.groups?.flatMap { it.questionGroups }?.flatMap { it.questions } ?: emptyList()
            Log.d("ExamViewModel", "submitCurrentSection: allQuestions.size = ${allQuestions.size}")
            val answers = allQuestions.map { q -> AnswerPayload(q.id, currentAnswers[q.id] ?: "") }
            val existingAttemptId = _detailState.value.attemptId
            Log.d("ExamViewModel", "Submitting section $sectionId for exam $examId, existingAttemptId=$existingAttemptId, answers: $answers")
            if (existingAttemptId != null) {
                // FULL mode flow: submit section into current attempt, do NOT finalize yet
                repository.submitSection(token, existingAttemptId, sectionId, answers).fold(
                    ifLeft = { e ->
                        _sectionState.value = _sectionState.value.copy(isSubmitting = false, error = e.message)
                    },
                    ifRight = { updated ->
                        _detailState.value = _detailState.value.copy(attemptId = updated.id)
                        _sectionState.value = _sectionState.value.copy(isSubmitting = false, message = "Đã nộp phần $sectionId")
                    }
                )
            } else {
                // SECTION mode: start attempt for this section, submit section, then finalize attempt
                repository.startExamAttempt(token, examId, mode = "SECTION", sectionId = sectionId, resumeLast = null).fold(
                    ifLeft = { e ->
                        _sectionState.value = _sectionState.value.copy(isSubmitting = false, error = e.message)
                    },
                    ifRight = { attempt ->
                        Log.d("ExamViewModel", "Started SECTION attempt with id ${attempt.id}")
                        repository.submitSection(token, attempt.id, sectionId, answers).fold(
                            ifLeft = { e ->
                                _sectionState.value = _sectionState.value.copy(isSubmitting = false, error = e.message)
                            },
                            ifRight = { updated ->
                                Log.d("ExamViewModel", "Submitted section $sectionId for attempt id ${updated.id}")
                                // finalize attempt to compute score
                                repository.submitAttempt(token, attempt.id).fold(
                                    ifLeft = { e ->
                                        Log.d("ExamViewModel", "Failed to finalize attempt id ${attempt.id}: ${e.message}")
                                        _sectionState.value = _sectionState.value.copy(isSubmitting = false, error = e.message)
                                    },
                                    ifRight = { finalAttempt ->
                                        _sectionState.value = _sectionState.value.copy(isSubmitting = false, message = "Đã nộp phần $sectionId")
                                    }
                                )
                            }
                        )
                    }
                )
            }
        }
    }

    fun refreshExams() {
        loadExams(1)
    }

    fun searchExams(query: String) {
        _listState.value = _listState.value.copy(filter = _listState.value.filter.copy(search = query))
        loadExams(1)
    }

    fun loadExamDetail(examId: Int) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _detailState.value = _detailState.value.copy(isLoading = true, error = null)
            repository.getExamById(token, examId).fold(
                ifLeft = { e ->
                    _detailState.value = _detailState.value.copy(isLoading = false, error = e.message)
                },
                ifRight = { exam ->
                    _detailState.value = _detailState.value.copy(
                        exam = exam,
                        sections = exam.sections,
                        isLoading = false,
                        error = null
                    )
                }
            )
        }
    }

    fun startFullAttempt(examId: Int, resumeLast: Boolean = true) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            _detailState.value = _detailState.value.copy(isSubmitting = true, message = null)
            repository.startExamAttempt(token, examId, mode = "FULL", sectionId = null, resumeLast = resumeLast).fold(
                ifLeft = { e ->
                    _detailState.value = _detailState.value.copy(isSubmitting = false, error = e.message)
                },
                ifRight = { attempt ->
                    Log.d("ExamViewModel", "Started FULL attempt with id ${attempt.id}")
                    _detailState.value = _detailState.value.copy(isSubmitting = false, attemptId = attempt.id, message = "Đã bắt đầu bài FULL")
                }
            )
        }
    }

    fun submitAttemptFinal() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken() ?: return@launch
            val attemptId = _detailState.value.attemptId ?: return@launch
            _detailState.value = _detailState.value.copy(isSubmitting = true, message = null)
            repository.submitAttempt(token, attemptId).fold(
                ifLeft = { e ->
                    _detailState.value = _detailState.value.copy(isSubmitting = false, error = e.message)
                },
                ifRight = { finalAttempt ->
                    _detailState.value = _detailState.value.copy(isSubmitting = false, attemptId = finalAttempt.id, message = "Đã nộp toàn bài")
                    _fullTestSubmitted.value = true // Signal to show dialog
                    // Backend awards XP asynchronously through the exam_completed event.
                    // Poll /rankings/me so the reward popup fires once XP is credited.
                    xpRewardTracker.observeAfterAction(sourceActionKey = "exam_completed")
                }
            )
        }
    }

    fun abortFullAttempt() {
        _detailState.value = _detailState.value.copy(attemptId = null, message = null)
    }
}

