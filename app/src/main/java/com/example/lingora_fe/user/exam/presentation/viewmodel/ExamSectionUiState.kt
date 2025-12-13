package com.example.lingora_fe.user.exam.presentation.viewmodel

import com.example.lingora_fe.user.exam.domain.repository.ExamSectionLite

data class ExamSectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val section: ExamSectionLite? = null,
    val answers: Map<Int, Any?> = emptyMap(),
    val isSubmitting: Boolean = false,
    val message: String? = null
)

