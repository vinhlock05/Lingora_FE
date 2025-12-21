package com.example.lingora_fe.admin.exam.presentation

import com.example.lingora_fe.admin.exam.domain.model.AdminExam
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttempt

import com.example.lingora_fe.user.exam.data.remote.dto.ExamDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto

data class AdminExamState(
    // Common
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Exam Management
    val exams: List<AdminExam> = emptyList(),
    val examCurrentPage: Int = 1,
    val examTotalPages: Int = 1,
    
    // Attempt Monitoring
    val attempts: List<AdminExamAttempt> = emptyList(),
    val attemptCurrentPage: Int = 1,
    val attemptTotalPages: Int = 1,
    
    // Filters & Search
    val examSearchQuery: String = "",
    val filterPublished: Boolean? = null, // null = All, true = Published, false = Draft
    val attemptSearchQuery: String = "",

    // Details (Transient)
    val examDetail: ExamDto? = null,
    val attemptDetail: AttemptDetailResponseDto? = null,
    val isEditExamDialogVisible: Boolean = false,
    val isUpdating: Boolean = false
)
