package com.example.lingora_fe.user.exam.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.exam.domain.model.Exam
import com.example.lingora_fe.user.exam.domain.model.ExamAttempt
import com.example.lingora_fe.user.exam.domain.model.ExamFilterOptions

interface ExamRepository {
    suspend fun getExams(
        token: String,
        filter: ExamFilterOptions
    ): Either<AppFailure, ExamListMetadata>

    suspend fun getExamById(
        token: String,
        examId: Int
    ): Either<AppFailure, Exam>

    suspend fun getSection(
        token: String,
        examId: Int,
        sectionId: Int
    ): Either<AppFailure, ExamSectionLite>

    suspend fun startExamAttempt(
        token: String,
        examId: Int,
        mode: String,
        sectionId: Int? = null,
        resumeLast: Boolean? = null
    ): Either<AppFailure, ExamAttempt>

    suspend fun submitSection(
        token: String,
        attemptId: Int,
        sectionId: Int,
        answers: List<AnswerPayload>
    ): Either<AppFailure, ExamAttempt>

    suspend fun submitAttempt(
        token: String,
        attemptId: Int
    ): Either<AppFailure, ExamAttempt>

    suspend fun getAttempts(
        token: String,
        page: Int = 1,
        limit: Int = 20
    ): Either<AppFailure, AttemptListMetadata>

    suspend fun getAttemptDetail(
        token: String,
        attemptId: Int
    ): Either<AppFailure, com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto>
}

data class ExamListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val exams: List<Exam>
)

data class ExamSectionLite(
    val id: Int,
    val sectionType: String,
    val title: String?,
    val durationSeconds: Int?,
    val instructions: String?,
    val audioUrl: String?,
    val groups: List<ExamGroupLite>
)

data class ExamGroupLite(
    val id: Int,
    val groupType: String,
    val title: String?,
    val description: String?,
    val content: String?,
    val resourceUrl: String?,
    val questionGroups: List<ExamQuestionGroupLite>
)

data class ExamQuestionGroupLite(
    val id: Int,
    val title: String?,
    val description: String?,
    val content: String?,
    val resourceUrl: String?,
    val metadata: Map<String, Any>? = null,
    val questions: List<ExamQuestionLite>
)

data class ExamQuestionLite(
    val id: Int,
    val questionType: String,
    val prompt: String,
    val options: Any?,
    val metadata: Map<String, Any>? = null
)

data class AnswerPayload(
    val questionId: Int,
    val answer: Any?
)

data class AttemptListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val attempts: List<ExamAttempt>
)

