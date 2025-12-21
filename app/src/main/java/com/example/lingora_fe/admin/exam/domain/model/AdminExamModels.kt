package com.example.lingora_fe.admin.exam.domain.model

data class AdminExamList(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val exams: List<AdminExam>
)

data class AdminExam(
    val id: Int,
    val title: String,
    val code: String,
    val type: String,
    val isPublished: Boolean,
    val thumbnailUrl: String?,
    val createdAt: String
)

data class AdminExamAttemptList(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val attempts: List<AdminExamAttempt>
)

data class AdminExamAttempt(
    val id: Int,
    val userId: Int,
    val username: String,
    val userAvatar: String?,
    val examId: Int,
    val examTitle: String,
    val score: Double,
    val status: String,
    val submittedAt: String?,
    val durationMinutes: Int?
)
