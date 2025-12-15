package com.example.lingora_fe.user.exam.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExamListMetaDataDto(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("exams") val exams: List<ExamDto>
)

data class ExamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("examType") val examType: String,
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("isPublished") val isPublished: Boolean,
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("sections") val sections: List<ExamSectionDto>? = null
)

data class ExamSectionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("sectionType") val sectionType: String,
    @SerializedName("title") val title: String?,
    @SerializedName("durationSeconds") val durationSeconds: Int?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("audioUrl") val audioUrl: String?,
    @SerializedName("status") val status: String? = null, // NOT_STARTED, COMPLETED
    @SerializedName("groups") val groups: List<ExamSectionGroupDto>? = null
)

data class ExamSectionGroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("groupType") val groupType: String,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("resourceUrl") val resourceUrl: String?,
    @SerializedName("questionGroups") val questionGroups: List<ExamQuestionGroupDto>? = null
)

data class ExamQuestionGroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("resourceUrl") val resourceUrl: String?,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("questions") val questions: List<ExamQuestionDto>? = null
)

data class ExamQuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("questionType") val questionType: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("options") val options: Any?,
    @SerializedName("correctAnswer") val correctAnswer: Any?,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)


// For listAttempts API - includes nested exam object
data class ExamAttemptDto(
    @SerializedName("id") val id: Int,
    @SerializedName("examId") val examId: Int?,
    @SerializedName("exam") val exam: AttemptExamSummaryDto?, // Nested exam object
    @SerializedName("mode") val mode: String,
    @SerializedName("status") val status: String,
    @SerializedName("startedAt") val startedAt: String?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("sectionProgress") val sectionProgress: Map<String, SectionProgressDto>?,
    @SerializedName("scoreSummary") val scoreSummary: ScoreSummaryDto?
)

data class SectionProgressDto(
    @SerializedName("sectionId") val sectionId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("correctCount") val correctCount: Int?,
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("earnedScore") val earnedScore: Double?,
    @SerializedName("band") val band: Double?
)

data class ScoreSummaryDto(
    @SerializedName("sections") val sections: Map<String, SectionScoreDto>?,
    @SerializedName("overallBand") val overallBand: Double?,
    @SerializedName("overallScore") val overallScore: Double?
)

data class SectionScoreDto(
    @SerializedName("sectionType") val sectionType: String?,
    @SerializedName("correct") val correct: Int?,
    @SerializedName("correctCount") val correctCount: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("score") val score: Double?,
    @SerializedName("band") val band: Double?,
    @SerializedName("status") val status: String?
)

// Response DTO for finalizeAttempt API (POST .../attempts/:id/submit)
data class FinalizeAttemptResponseDto(
    @SerializedName("attemptId") val attemptId: Int,
    @SerializedName("examId") val examId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("scoreSummary") val scoreSummary: ScoreSummaryDto?
)

data class StartAttemptRequestDto(
    @SerializedName("mode") val mode: String,
    @SerializedName("sectionId") val sectionId: Int?,
    @SerializedName("resumeLast") val resumeLast: Boolean?
)

data class SubmitSectionRequestDto(
    @SerializedName("answers") val answers: List<AnswerDto>
)

data class AnswerDto(
    @SerializedName("questionId") val questionId: Int,
    @SerializedName("answer") val answer: Any?
)

data class AttemptListMetaDataDto(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("attempts") val attempts: List<ExamAttemptDto>
)

// For getAttemptDetail API - full detail response
data class AttemptDetailResponseDto(
    @SerializedName("attempt") val attempt: ExamAttemptDto,
    @SerializedName("exam") val exam: AttemptExamSummaryDto?,
    @SerializedName("user") val user: AttemptUserDto?,
    @SerializedName("scoreSummary") val scoreSummary: AttemptScoreSummaryDto?,
    @SerializedName("sections") val sections: List<AttemptSectionDto>?
)

data class AttemptUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String?
)

data class AttemptSummaryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("mode") val mode: String,
    @SerializedName("status") val status: String,
    @SerializedName("startedAt") val startedAt: String?,
    @SerializedName("submittedAt") val submittedAt: String?
)

data class AttemptExamSummaryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("examType") val examType: String?,
    @SerializedName("code") val code: String?
)

data class AttemptScoreSummaryDto(
    @SerializedName("overallScore") val overallScore: Double?,
    @SerializedName("sections") val sections: Map<String, AttemptSectionScoreDto>?,
    @SerializedName("totals") val totals: AttemptTotalsDto?,
    @SerializedName("bands") val bands: AttemptBandsDto?
)

data class AttemptSectionScoreDto(
    @SerializedName("sectionType") val sectionType: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("correct") val correct: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("correctCount") val correctCount: Int?,
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("earnedScore") val earnedScore: Double?,
    @SerializedName("band") val band: Double?
)

data class AttemptTotalsDto(
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("totalCorrect") val totalCorrect: Int?,
    @SerializedName("totalScore") val totalScore: Double?
)

data class AttemptBandsDto(
    @SerializedName("listening") val listening: Double?,
    @SerializedName("reading") val reading: Double?,
    @SerializedName("writing") val writing: Double?,
    @SerializedName("speaking") val speaking: Double?,
    @SerializedName("overall") val overall: Double?
)

// Section hierarchy: Section -> SectionGroup -> QuestionGroup -> Questions
data class AttemptSectionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("sectionType") val sectionType: String,
    @SerializedName("groups") val groups: List<AttemptSectionGroupDto>?
)

data class AttemptSectionGroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("groupType") val groupType: String?,
    @SerializedName("questionGroups") val questionGroups: List<AttemptQuestionGroupDto>?
)

data class AttemptQuestionGroupDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("metadata") val metadata: Map<String, Any>?,
    @SerializedName("questions") val questions: List<AttemptQuestionDto>?
)

data class AttemptQuestionDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("questionId") val questionId: Int?,
    @SerializedName("prompt") val prompt: String?,
    @SerializedName("questionType") val questionType: String?,
    @SerializedName("options") val options: Any?, // Can be List<String> or List<{key, value}>
    @SerializedName("correctAnswer") val correctAnswer: Any?,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("userAnswer") val userAnswer: Any?,
    @SerializedName("isCorrect") val isCorrect: Boolean?,
    @SerializedName("score") val score: Double?,
    @SerializedName("aiFeedback") val aiFeedback: Any?
)

