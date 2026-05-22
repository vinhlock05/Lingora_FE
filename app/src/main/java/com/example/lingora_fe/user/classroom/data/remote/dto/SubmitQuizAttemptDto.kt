package com.example.lingora_fe.user.classroom.data.remote.dto

data class SubmitQuizAttemptRequest(
    val answers: Map<String, String>
)

data class SubmitQuizAttemptResponseDto(
    val attempt: ClassroomQuizAttemptDto,
    val isPassing: Boolean
)

data class ClassroomQuizAttemptDto(
    val id: Int,
    val attemptNumber: Int,
    val score: Double,
    val correctCount: Int? = null,
    val answers: Map<String, String>,
    val startedAt: String,
    val submittedAt: String?
)

data class QuizAttemptWithUserDto(
    val id: Int,
    val attemptNumber: Int,
    val score: Double,
    val answers: Map<String, String>,
    val startedAt: String?,
    val submittedAt: String?,
    val user: ClassroomUserDto?
)
