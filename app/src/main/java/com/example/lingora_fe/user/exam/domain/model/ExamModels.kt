package com.example.lingora_fe.user.exam.domain.model

enum class ExamType(val value: String) {
    IELTS("IELTS"),
    TOEIC("TOEIC"),
    TOEFL("TOEFL")
}

enum class ExamSectionType(val value: String) {
    LISTENING("LISTENING"),
    READING("READING"),
    WRITING("WRITING"),
    SPEAKING("SPEAKING")
}

enum class ExamGroupType(val value: String) {
    LISTENING_PART("LISTENING_PART"),
    PASSAGE("PASSAGE"),
    WRITING_TASK("WRITING_TASK"),
    SPEAKING_PART("SPEAKING_PART")
}

enum class ExamQuestionType(val value: String) {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    TRUE_FALSE("TRUE_FALSE"),
    SHORT_ANSWER("SHORT_ANSWER"),
    NOTE_COMPLETION("NOTE_COMPLETION"),
    DIAGRAM_LABELING("DIAGRAM_LABELING"),
    ESSAY("ESSAY"),
    SPEAKING_PROMPT("SPEAKING_PROMPT"),
    FILL_IN_THE_BLANK("FILL_IN_THE_BLANK"),
    MATCHING("MATCHING"),
    YES_NO_NOT_GIVEN("YES_NO_NOT_GIVEN")
}

enum class ExamAttemptMode(val value: String) {
    SECTION("SECTION"),
    FULL("FULL")
}

data class Exam(
    val id: Int,
    val examType: ExamType,
    val code: String,
    val title: String,
    val isPublished: Boolean,
    val metadata: Map<String, Any>?,
    val sections: List<ExamSection> = emptyList()
)

data class ExamSection(
    val id: Int,
    val sectionType: ExamSectionType,
    val title: String?,
    val durationSeconds: Int?,
    val instructions: String?,
    val audioUrl: String?,
    val status: String? = null, // NOT_STARTED, COMPLETED
    val groups: List<ExamSectionGroup> = emptyList()
)

data class ExamSectionGroup(
    val id: Int,
    val groupType: ExamGroupType,
    val title: String?,
    val description: String?,
    val content: String?,
    val resourceUrl: String?,
    val questionGroups: List<ExamQuestionGroup> = emptyList()
)

data class ExamQuestionGroup(
    val id: Int,
    val title: String?,
    val description: String?,
    val content: String?,
    val resourceUrl: String?,
    val metadata: Map<String, Any>? = null,
    val questions: List<ExamQuestion> = emptyList()
)

data class ExamQuestion(
    val id: Int,
    val questionType: ExamQuestionType,
    val prompt: String,
    val options: Any?,
    val correctAnswer: Any?,
    val explanation: String?,
    val metadata: Map<String, Any>? = null
)

data class ExamAttempt(
    val id: Int,
    val examId: Int,
    val examTitle: String? = null,
    val examCode: String? = null,
    val mode: ExamAttemptMode,
    val status: String,
    val startedAt: String? = null,
    val submittedAt: String? = null,
    val sectionProgress: Map<Int, SectionProgress>,
    val scoreSummary: ScoreSummary?
)

data class SectionProgress(
    val sectionId: Int,
    val status: String,
    val correctCount: Int?,
    val totalQuestions: Int?,
    val earnedScore: Double?,
    val band: Double?
)

data class ExamAttemptAnswer(
    val id: Int,
    val attemptId: Int,
    val sectionId: Int,
    val questionId: Int,
    val answer: Any?,
    val isCorrect: Boolean?,
    val score: Double?,
    val aiFeedback: String?
)

data class ScoreSummary(
    val sections: List<SectionScore>,
    val overallBand: Double?
)

data class SectionScore(
    val sectionType: ExamSectionType,
    val correctCount: Int?,
    val totalQuestions: Int?,
    val band: Double?,
    val status: String? = null
)

data class ExamFilterOptions(
    val examType: String? = null,
    val isPublished: Boolean? = true,
    val search: String? = null,
    val page: Int = 1,
    val limit: Int = 10
)

