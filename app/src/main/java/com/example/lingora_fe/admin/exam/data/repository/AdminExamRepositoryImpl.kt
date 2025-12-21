package com.example.lingora_fe.admin.exam.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.exam.data.remote.api.AdminExamApiService
import com.example.lingora_fe.admin.exam.data.remote.dto.AdminUpdateExamRequest
import com.example.lingora_fe.admin.exam.data.remote.dto.ImportExamBodyReq
import com.example.lingora_fe.admin.exam.data.remote.dto.toDomain
import com.example.lingora_fe.admin.exam.domain.model.AdminExam
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttempt
import com.example.lingora_fe.admin.exam.domain.model.AdminExamAttemptList
import com.example.lingora_fe.admin.exam.domain.model.AdminExamList
import com.example.lingora_fe.admin.exam.domain.repository.AdminExamRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.exam.data.remote.dto.ExamDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto
import javax.inject.Inject

class AdminExamRepositoryImpl @Inject constructor(
    private val api: AdminExamApiService
) : AdminExamRepository {

    override suspend fun getExams(
        page: Int,
        limit: Int,
        search: String?,
        examType: String?,
        isPublished: Boolean?
    ): Either<AppFailure, AdminExamList> {
        return Either.catch {
            api.getExams(page, limit, search, examType, isPublished).metaData?.toDomain()
                ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun importExam(exams: List<ImportExamBodyReq>): Either<AppFailure, List<Int>> {
        return Either.catch {
            api.importExam(exams).metaData?.map { it.id } ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getExamDetail(id: Int): Either<AppFailure, ExamDto> {
        return Either.catch {
            api.getExamDetail(id).metaData ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateExam(
        id: Int,
        title: String?,
        description: String?,
        isPublished: Boolean?,
        thumbnailUrl: String?,
        code: String?
    ): Either<AppFailure, AdminExam> {
        return Either.catch {
            val request = AdminUpdateExamRequest(
                title = title,
                description = description,
                isPublished = isPublished,
                thumbnailUrl = thumbnailUrl,
                code = code
            )
            api.updateExam(id, request).metaData?.toDomain() ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteExam(id: Int): Either<AppFailure, Unit> {
        return Either.catch {
            api.deleteExam(id)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getExamAttempts(
        page: Int,
        limit: Int,
        search: String?,
        userId: Int?,
        examId: Int?,
        status: String?,
        startDate: String?,
        endDate: String?,
        minScore: Double?,
        maxScore: Double?
    ): Either<AppFailure, AdminExamAttemptList> {
        return Either.catch {
            api.getExamAttempts(
                page, limit, search, userId, examId, status, startDate, endDate, minScore, maxScore
            ).metaData?.toDomain() ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getExamAttemptDetail(id: Int): Either<AppFailure, AttemptDetailResponseDto> {
        return Either.catch {
            api.getExamAttemptDetail(id).metaData ?: throw Exception("No data")
        }.mapLeft { it.toAppFailure() }
    }
}
