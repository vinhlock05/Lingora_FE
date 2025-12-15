package com.example.lingora_fe.user.exam.data.remote.dto

import com.example.lingora_fe.user.exam.domain.model.Exam
import com.example.lingora_fe.user.exam.domain.model.ExamAttempt
import com.example.lingora_fe.user.exam.domain.model.ExamAttemptMode
import com.example.lingora_fe.user.exam.domain.model.ExamFilterOptions
import com.example.lingora_fe.user.exam.domain.model.ExamGroupType
import com.example.lingora_fe.user.exam.domain.model.ExamQuestionGroup
import com.example.lingora_fe.user.exam.domain.repository.ExamListMetadata
import com.example.lingora_fe.user.exam.domain.model.ExamQuestion
import com.example.lingora_fe.user.exam.domain.model.ExamQuestionType
import com.example.lingora_fe.user.exam.domain.model.ExamSection
import com.example.lingora_fe.user.exam.domain.repository.ExamSectionLite
import com.example.lingora_fe.user.exam.domain.model.ExamSectionType
import com.example.lingora_fe.user.exam.domain.model.ScoreSummary
import com.example.lingora_fe.user.exam.domain.model.SectionProgress
import com.example.lingora_fe.user.exam.domain.model.SectionScore

fun ExamListMetaDataDto.toDomain(): ExamListMetadata {
    return ExamListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        exams = exams.map { it.toDomain() }
    )
}

fun ExamDto.toDomain(): Exam {
    return Exam(
        id = id,
        examType = ExamTypeMapper.map(examType),
        code = code,
        title = title,
        isPublished = isPublished,
        metadata = metadata,
        sections = sections?.map { it.toDomain() } ?: emptyList()
    )
}

fun ExamSectionDto.toDomain(): ExamSection {
    return ExamSection(
        id = id,
        sectionType = SectionTypeMapper.map(sectionType),
        title = title,
        durationSeconds = durationSeconds,
        instructions = instructions,
        audioUrl = audioUrl,
        status = status,
        groups = groups?.map { it.toDomain() } ?: emptyList()
    )
}

fun ExamSectionGroupDto.toDomain() = com.example.lingora_fe.user.exam.domain.model.ExamSectionGroup(
    id = id,
    groupType = GroupTypeMapper.map(groupType),
    title = title,
    description = description,
    content = content,
    resourceUrl = resourceUrl,
    questionGroups = questionGroups?.map { it.toDomain() } ?: emptyList()
)

fun ExamQuestionGroupDto.toDomain() = ExamQuestionGroup(
    id = id,
    title = title,
    description = description,
    content = content,
    resourceUrl = resourceUrl,
    metadata = metadata,
    questions = questions?.map { it.toDomain() } ?: emptyList()
)

fun ExamQuestionDto.toDomain(): ExamQuestion {
    return ExamQuestion(
        id = id,
        questionType = QuestionTypeMapper.map(questionType),
        prompt = prompt,
        options = options,
        correctAnswer = correctAnswer,
        explanation = explanation,
        metadata = metadata
    )
}


fun ExamAttemptDto.toDomain(): ExamAttempt {
    return ExamAttempt(
        id = id,
        examId = examId ?: exam?.id ?: 0,
        examTitle = exam?.title,
        examCode = exam?.code,
        mode = ExamAttemptMode.values().find { it.value == mode } ?: ExamAttemptMode.FULL,
        status = status,
        startedAt = startedAt,
        submittedAt = submittedAt,
        sectionProgress = sectionProgress?.values?.associate { it.sectionId to it.toDomain() } ?: emptyMap(),
        scoreSummary = scoreSummary?.toDomain()
    )
}

fun SectionProgressDto.toDomain(): SectionProgress {
    return SectionProgress(
        sectionId = sectionId,
        status = status,
        correctCount = correctCount,
        totalQuestions = totalQuestions,
        earnedScore = earnedScore,
        band = band
    )
}


fun ScoreSummaryDto.toDomain(): ScoreSummary {
    return ScoreSummary(
        sections = sections?.map { (key, value) -> 
            value.toDomain(key) 
        } ?: emptyList(),
        overallBand = overallBand ?: overallScore
    )
}

fun SectionScoreDto.toDomain(keyType: String? = null): SectionScore {
    val type = sectionType ?: keyType ?: ""
    return SectionScore(
        sectionType = SectionTypeMapper.map(type),
        correctCount = correct ?: correctCount,
        totalQuestions = total ?: totalQuestions,
        band = band ?: score,
        status = status
    )
}

fun FinalizeAttemptResponseDto.toDomain(): ExamAttempt {
    return ExamAttempt(
        id = attemptId,
        examId = examId,
        mode = ExamAttemptMode.FULL,
        status = status,
        startedAt = null,
        submittedAt = submittedAt,
        sectionProgress = emptyMap(),
        scoreSummary = scoreSummary?.toDomain()
    )
}


object ExamTypeMapper {
    fun map(value: String): com.example.lingora_fe.user.exam.domain.model.ExamType {
        return com.example.lingora_fe.user.exam.domain.model.ExamType.values().find { it.value == value } ?: com.example.lingora_fe.user.exam.domain.model.ExamType.IELTS
    }
}

object SectionTypeMapper {
    fun map(value: String): ExamSectionType {
        return ExamSectionType.values().find { it.value == value } ?: ExamSectionType.LISTENING
    }
}

object GroupTypeMapper {
    fun map(value: String): ExamGroupType {
        return ExamGroupType.values().find { it.value == value } ?: ExamGroupType.LISTENING_PART
    }
}

object QuestionTypeMapper {
    fun map(value: String): ExamQuestionType {
        return ExamQuestionType.values().find { it.value == value } ?: ExamQuestionType.MULTIPLE_CHOICE
    }
}

fun ExamFilterOptions.toQuery(): ExamQueryParamsDto {
    return ExamQueryParamsDto(
        examType = examType,
        isPublished = isPublished,
        search = search,
        page = page,
        limit = limit
    )
}

data class ExamQueryParamsDto(
    val examType: String?,
    val isPublished: Boolean?,
    val search: String?,
    val page: Int,
    val limit: Int
)

