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

    private val _state = MutableStateFlow(QuizDetailState())
    val state: StateFlow<QuizDetailState> = _state.asStateFlow()

    init {
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
            questionOptions = emptyList(),
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
            questionOptions = emptyList(),
            correctAnswer = "",
            explanation = ""
        )
    }

    fun onQuestionTypeChange(type: QuizType) {
        _state.value = _state.value.copy(questionType = type)
    }

    fun onQuestionTextChange(text: String) {
        _state.value = _state.value.copy(questionText = text)
    }

    fun onQuestionOptionsChange(options: List<String>) {
        _state.value = _state.value.copy(questionOptions = options)
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
            options = current.questionOptions.takeIf { it.isNotEmpty() } ?: emptyList(),
            correctAnswer = current.correctAnswer.trim().ifEmpty { "" },
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
                    questionOptions = emptyList(),
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
            options = current.questionOptions.takeIf { it.isNotEmpty() } ?: emptyList(),
            correctAnswer = current.correctAnswer.trim().ifEmpty { "" },
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
                    questionOptions = emptyList(),
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
        _state.value = _state.value.copy(
            showAddQuestionDialog = true,
            editingQuestion = question,
            questionType = question.type,
            questionText = question.question,
            questionOptions = question.options,
            correctAnswer = question.correctAnswer ?: "",
            explanation = question.explanation ?: ""
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

            repository.importQuestionsFromStudySet(
                classroomId = classroomId,
                quizId = quizId,
                studySetId = studySetId
            ).fold(
                ifLeft = { error ->
                    _state.value = _state.value.copy(
                        isImporting = false,
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
