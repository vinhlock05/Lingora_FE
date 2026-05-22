package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.QuizType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val repository: ClassroomRepository,
    private val studySetRepository: com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository,
    private val tokenManager: com.example.lingora_fe.core.network.TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val classroomId: Int =
        requireNotNull(savedStateHandle.get<String>("classroomId")?.toIntOrNull()) {
            "classroomId is required"
        }

    private val quizId: Int =
        requireNotNull(savedStateHandle.get<String>("quizId")?.toIntOrNull()) {
            "quizId is required"
        }

    private val isTeacher: Boolean = savedStateHandle.get<String>("isTeacher")?.toBoolean() ?: false

    private val _state = MutableStateFlow(QuizDetailState(
        classroomId = savedStateHandle.get<String>("classroomId") ?: "",
        isTeacher = isTeacher
    ))
    val state: StateFlow<QuizDetailState> = _state.asStateFlow()

    init {
        loadQuizDetail()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun refresh() {
        loadQuizDetail()
    }

    private fun loadQuizDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getQuizDetail(classroomId, quizId).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải bài kiểm tra"
                    )
                },
                ifRight = { quiz ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        quiz = quiz
                    )
                }
            )
        }
    }

    fun showAddQuestionDialog() {
        _state.value = _state.value.copy(
            showAddQuestionDialog = true,
            editingQuestion = null,
            questionType = QuizType.MULTIPLE_CHOICE,
            questionText = "",
            questionOptions = listOf("", "", "", ""),
            correctAnswer = "",
            explanation = ""
        )
    }

    fun hideAddQuestionDialog() {
        _state.value = _state.value.copy(
            showAddQuestionDialog = false,
            editingQuestion = null,
            questionType = QuizType.MULTIPLE_CHOICE,
            questionText = "",
            questionOptions = listOf("", "", "", ""),
            correctAnswer = "",
            explanation = ""
        )
    }

    fun onQuestionTypeChange(type: QuizType) {
        val updatedOptions = when (type) {
            QuizType.TRUE_FALSE -> listOf("True", "False")
            QuizType.MULTIPLE_CHOICE -> {
                val current = _state.value.questionOptions.filter { it.isNotBlank() }
                if (current.size >= 2) current else listOf("", "", "", "")
            }
            QuizType.SHORT_ANSWER -> emptyList()
        }
        _state.value = _state.value.copy(
            questionType = type,
            questionOptions = updatedOptions,
            correctAnswer = if (type == QuizType.TRUE_FALSE) "True" else _state.value.correctAnswer
        )
    }

    fun onQuestionTextChange(text: String) {
        _state.value = _state.value.copy(questionText = text)
    }

    fun onQuestionOptionsChange(options: List<String>) {
        _state.value = _state.value.copy(questionOptions = options)
    }

    fun onQuestionOptionItemChange(index: Int, value: String) {
        val current = _state.value.questionOptions.toMutableList()
        if (index in current.indices) {
            current[index] = value
            _state.value = _state.value.copy(questionOptions = current)
        }
    }

    fun addQuestionOption() {
        if (_state.value.questionType != QuizType.MULTIPLE_CHOICE) return
        _state.value = _state.value.copy(questionOptions = _state.value.questionOptions + "")
    }

    fun removeQuestionOption(index: Int) {
        if (_state.value.questionType != QuizType.MULTIPLE_CHOICE) return
        val current = _state.value.questionOptions.toMutableList()
        if (index in current.indices && current.size > 2) {
            current.removeAt(index)
            _state.value = _state.value.copy(questionOptions = current)
        }
    }

    fun onCorrectAnswerChange(answer: String) {
        _state.value = _state.value.copy(correctAnswer = answer)
    }

    fun onExplanationChange(explanation: String) {
        _state.value = _state.value.copy(explanation = explanation)
    }

    fun saveQuestion() {
        val current = _state.value
        if (current.questionText.isBlank()) {
            return
        }
        if (current.questionType == QuizType.MULTIPLE_CHOICE && current.questionOptions.filter { it.isNotBlank() }.size < 2) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSavingQuestion = true)

            val isEditing = current.editingQuestion != null

            if (isEditing) {
                updateQuestion()
            } else {
                createQuestion()
            }
        }
    }

    private suspend fun createQuestion() {
        val current = _state.value
        repository.createQuizQuestion(
            classroomId = classroomId,
            quizId = quizId,
            type = current.questionType.value,
            question = current.questionText.trim(),
            options = buildOptionsForSubmit(current.questionType, current.questionOptions),
            correctAnswer = buildCorrectAnswerForSubmit(
                type = current.questionType,
                current.correctAnswer,
                current.questionOptions
            ),
            explanation = current.explanation.trim().takeIf { it.isNotEmpty() }
        ).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(isSavingQuestion = false)
            },
            ifRight = { _ ->
                _state.value = _state.value.copy(
                    isSavingQuestion = false,
                    showAddQuestionDialog = false,
                    questionType = QuizType.MULTIPLE_CHOICE,
                    questionText = "",
                    questionOptions = listOf("", "", "", ""),
                    correctAnswer = "",
                    explanation = ""
                )
                loadQuizDetail()
            }
        )
    }

    private suspend fun updateQuestion() {
        val current = _state.value
        val question = current.editingQuestion ?: return

        repository.updateQuizQuestion(
            classroomId = classroomId,
            quizId = quizId,
            questionId = question.id,
            type = current.questionType.value,
            question = current.questionText.trim(),
            options = buildOptionsForSubmit(current.questionType, current.questionOptions),
            correctAnswer = buildCorrectAnswerForSubmit(
                type = current.questionType,
                current.correctAnswer,
                current.questionOptions
            ),
            explanation = current.explanation.trim().takeIf { it.isNotEmpty() }
        ).fold(
            ifLeft = { error ->
                _state.value = _state.value.copy(isSavingQuestion = false)
            },
            ifRight = { _ ->
                _state.value = _state.value.copy(
                    isSavingQuestion = false,
                    showAddQuestionDialog = false,
                    editingQuestion = null,
                    questionType = QuizType.MULTIPLE_CHOICE,
                    questionText = "",
                    questionOptions = listOf("", "", "", ""),
                    correctAnswer = "",
                    explanation = ""
                )
                loadQuizDetail()
            }
        )
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            repository.deleteQuizQuestion(
                classroomId = classroomId,
                quizId = quizId,
                questionId = questionId
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Không thể xóa câu hỏi"
                    )
                },
                ifRight = {
                    loadQuizDetail()
                }
            )
        }
    }

    fun editQuestion(question: ClassroomQuizQuestion) {
        val normalizedOptions = when (question.type) {
            QuizType.TRUE_FALSE -> if (question.options.isEmpty()) listOf("True", "False") else question.options
            QuizType.MULTIPLE_CHOICE -> if (question.options.size < 2) question.options + listOf("", "") else question.options
            QuizType.SHORT_ANSWER -> emptyList()
        }
        _state.value = _state.value.copy(
            showAddQuestionDialog = true,
            editingQuestion = question,
            questionType = question.type,
            questionText = question.question,
            questionOptions = normalizedOptions,
            correctAnswer = question.correctAnswer ?: "",
            explanation = question.explanation ?: ""
        )
    }

    private fun buildOptionsForSubmit(type: QuizType, options: List<String>): List<String> {
        return when (type) {
            QuizType.TRUE_FALSE -> listOf("True", "False")
            QuizType.MULTIPLE_CHOICE -> options.map { it.trim() }.filter { it.isNotEmpty() }
            QuizType.SHORT_ANSWER -> emptyList()
        }
    }

    private fun buildCorrectAnswerForSubmit(type: QuizType, answer: String, options: List<String>): String {
        return when (type) {
            QuizType.TRUE_FALSE -> if (answer.equals("false", ignoreCase = true)) "False" else "True"
            QuizType.MULTIPLE_CHOICE -> answer.trim()
            QuizType.SHORT_ANSWER -> answer.trim()
        }
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

            repository.importQuestionsFromStudySet(
                classroomId = classroomId,
                quizId = quizId,
                studySetId = studySetId
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        showImportStudySetDialog = false,
                        selectedStudySetId = null,
                        error = error.message ?: "Không thể import câu hỏi"
                    )
                },
                ifRight = { quiz ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        showImportStudySetDialog = false,
                        selectedStudySetId = null,
                        quiz = quiz
                    )
                }
            )
        }
    }

}
