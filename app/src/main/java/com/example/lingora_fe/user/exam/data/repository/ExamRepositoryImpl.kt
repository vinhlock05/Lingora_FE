package com.example.lingora_fe.user.exam.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.exam.data.remote.api.ExamApiService
import com.example.lingora_fe.user.exam.data.remote.dto.StartAttemptRequestDto
import com.example.lingora_fe.user.exam.data.remote.dto.SubmitSectionRequestDto
import com.example.lingora_fe.user.exam.data.remote.dto.toDomain
import com.example.lingora_fe.user.exam.domain.repository.AnswerPayload
import com.example.lingora_fe.user.exam.domain.repository.ExamListMetadata
import com.example.lingora_fe.user.exam.domain.repository.ExamRepository
import com.example.lingora_fe.user.exam.domain.repository.ExamSectionLite
import com.example.lingora_fe.user.exam.domain.model.Exam
import com.example.lingora_fe.user.exam.domain.model.ExamAttempt
import com.example.lingora_fe.user.exam.domain.model.ExamFilterOptions
import com.example.lingora_fe.user.exam.data.remote.dto.ExamQueryParamsDto
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val api: ExamApiService
) : ExamRepository {

    override suspend fun getExams(token: String, filter: ExamFilterOptions): Either<AppFailure, ExamListMetadata> {
        return Either.catch {
            val q = com.example.lingora_fe.user.exam.data.remote.dto.ExamQueryParamsDto(
                examType = filter.examType,
                isPublished = filter.isPublished,
                search = filter.search,
                page = filter.page,
                limit = filter.limit
            )
            val res = api.getExams(q.examType, q.isPublished, q.search, q.page, q.limit)
            res.metaData?.toDomain() ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getExamById(token: String, examId: Int): Either<AppFailure, Exam> {
        return Either.catch {
            val res = api.getExamById(examId)
            res.metaData?.toDomain() ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getSection(token: String, examId: Int, sectionId: Int): Either<AppFailure, ExamSectionLite> {
        return Either.catch {
            val res = api.getSection(examId, sectionId)
            val dto = res.metaData ?: throw Exception(res.message)
            ExamSectionLite(
                id = dto.id,
                sectionType = dto.sectionType,
                title = dto.title,
                durationSeconds = dto.durationSeconds,
                instructions = dto.instructions,
                audioUrl = dto.audioUrl,
                groups = dto.groups?.map { g ->
                    com.example.lingora_fe.user.exam.domain.repository.ExamGroupLite(
                        id = g.id,
                        groupType = g.groupType,
                        title = g.title,
                        description = g.description,
                        content = g.content,
                        resourceUrl = g.resourceUrl,
                        questionGroups = g.questionGroups?.map { qg ->
                            com.example.lingora_fe.user.exam.domain.repository.ExamQuestionGroupLite(
                                id = qg.id,
                                title = qg.title,
                                description = qg.description,
                                content = qg.content,
                                resourceUrl = qg.resourceUrl,
                                metadata = qg.metadata,
                                questions = qg.questions?.map { q ->
                                    com.example.lingora_fe.user.exam.domain.repository.ExamQuestionLite(
                                        id = q.id,
                                        questionType = q.questionType,
                                        prompt = q.prompt,
                                        options = q.options,
                                        metadata = q.metadata
                                    )
                                } ?: emptyList()
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()
            )
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun startExamAttempt(token: String, examId: Int, mode: String, sectionId: Int?, resumeLast: Boolean?): Either<AppFailure, ExamAttempt> {
        return Either.catch {
            val res = api.startExamAttempt(examId, StartAttemptRequestDto(mode, sectionId, resumeLast))
            res.metaData?.toDomain() ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun submitSection(token: String, attemptId: Int, sectionId: Int, answers: List<AnswerPayload>): Either<AppFailure, ExamAttempt> {
        return Either.catch {
            val body = SubmitSectionRequestDto(answers.map { com.example.lingora_fe.user.exam.data.remote.dto.AnswerDto(it.questionId, it.answer) })
            val res = api.submitSection(attemptId, sectionId, body)
            res.metaData?.toDomain() ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun submitAttempt(token: String, attemptId: Int): Either<AppFailure, ExamAttempt> {
        return Either.catch {
            val res = api.submitAttempt(attemptId)
            res.metaData?.toDomain() ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAttempts(token: String, page: Int, limit: Int): Either<AppFailure, com.example.lingora_fe.user.exam.domain.repository.AttemptListMetadata> {
        return Either.catch {
            val res = api.getAttempts(page, limit)
            val meta = res.metaData ?: throw Exception(res.message)
            com.example.lingora_fe.user.exam.domain.repository.AttemptListMetadata(
                currentPage = meta.currentPage,
                totalPages = meta.totalPages,
                total = meta.total,
                attempts = meta.attempts.map { it.toDomain() }
            )
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAttemptDetail(token: String, attemptId: Int): Either<AppFailure, com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto> {
        return Either.catch {
            val res = api.getAttemptDetail(attemptId)
            res.metaData ?: throw Exception(res.message)
        }.mapLeft { it.toAppFailure() }
    }
}

