package com.example.lingora_fe.user.exam.presentation.viewmodel

import com.example.lingora_fe.user.exam.domain.model.Exam
import com.example.lingora_fe.user.exam.domain.model.ExamFilterOptions
import com.example.lingora_fe.user.exam.domain.model.ExamSection

data class ExamListUiState(
    val exams: List<Exam> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val filter: ExamFilterOptions = ExamFilterOptions()
)

data class ExamDetailUiState(
    val exam: Exam? = null,
    val sections: List<ExamSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val attemptId: Int? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null
)

