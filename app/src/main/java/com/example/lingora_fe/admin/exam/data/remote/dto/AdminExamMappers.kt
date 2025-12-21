package com.example.lingora_fe.admin.exam.data.remote.dto

import com.example.lingora_fe.admin.exam.domain.model.AdminExam
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttempt
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttemptList
import com.example.lingora_fe.admin.exam.domain.model.AdminExamList

fun AdminExamListResponse.toDomain() = AdminExamList(
    currentPage = currentPage,
    totalPages = totalPages,
    total = total,
    exams = exams?.map { it.toDomain() } ?: emptyList()
)

fun AdminExamDto.toDomain() = AdminExam(
    id = id,
    title = title ?: "Untitled Exam",
    code = code ?: "N/A",
    type = examType ?: "Unknown",
    isPublished = isPublished,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt ?: ""
)

fun AdminExamAttemptListResponse.toDomain() = AdminExamAttemptList(
    currentPage = currentPage,
    totalPages = totalPages,
    total = total,
    attempts = attempts?.map { it.toDomain() } ?: emptyList()
)

fun AdminExamAttemptDto.toDomain() = AdminExamAttempt(
    id = id,
    userId = user.id,
    username = user.username,
    userAvatar = user.avatar,
    examId = exam.id,
    examTitle = exam.title,
    score = score,
    status = status,
    submittedAt = submittedAt,
    durationMinutes = durationMinutes
)
