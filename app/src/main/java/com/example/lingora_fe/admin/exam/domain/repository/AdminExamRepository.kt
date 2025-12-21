package com.example.lingora_fe.admin.exam.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.exam.domain.model.AdminExam
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttempt
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttemptList
import com.example.lingora_fe.admin.exam.domain.model.AdminExamList
import com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.exam.data.remote.dto.ExamDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto

interface AdminExamRepository {
    suspend fun getExams(
        page: Int,
        limit: Int,
        search: String? = null,
        examType: String? = null,
        isPublished: Boolean? = null
    ): Either<AppFailure, AdminExamList>

    suspend fun importExam(exams: List<ImportExamBodyReq>): Either<AppFailure, List<Int>>

    suspend fun getExamDetail(id: Int): Either<AppFailure, ExamDto>

    suspend fun updateExam(
        id: Int,
        title: String? = null,
        description: String? = null,
        isPublished: Boolean? = null,
        thumbnailUrl: String? = null,
        code: String? = null
    ): Either<AppFailure, AdminExam>

    suspend fun deleteExam(id: Int): Either<AppFailure, Unit>

    suspend fun getExamAttempts(
        page: Int,
        limit: Int,
        search: String? = null,
        userId: Int? = null,
        examId: Int? = null,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        minScore: Double? = null,
        maxScore: Double? = null
    ): Either<AppFailure, AdminExamAttemptList>

    suspend fun getExamAttemptDetail(id: Int): Either<AppFailure, AttemptDetailResponseDto>
}
