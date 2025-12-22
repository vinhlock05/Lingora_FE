package com.example.lingora_fe.admin.exam.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== Exam List ====================

data class AdminExamListResponse(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("exams") val exams: List<AdminExamDto>?
)

data class AdminExamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("examType") val examType: String?,
    @SerializedName("isPublished") val isPublished: Boolean,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("createdAt") val createdAt: String?
)

// ==================== Update Exam ====================

data class AdminUpdateExamRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("code") val code: String? = null
)

// ==================== Exam Attempts ====================

data class AdminExamAttemptListResponse(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("attempts") val attempts: List<AdminExamAttemptDto>?
)

data class AdminExamAttemptDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user") val user: AdminUserShortDto,
    @SerializedName("exam") val exam: AdminExamShortDto,
    @SerializedName("score") val score: Double,
    @SerializedName("status") val status: String,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int?
)

data class AdminUserShortDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("avatar") val avatar: String?
)

data class AdminExamShortDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

// ==================== Import Exam ====================
// Assuming import assumes raw JSON body, but if there's a response format:
data class ImportExamResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)
