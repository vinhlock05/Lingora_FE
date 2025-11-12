package com.example.lingora_fe.user.adaptivetest.domain.model

data class AdaptiveQuestion(
    val id: Int,
    val skill: String,
    val text: String,
    val options: List<String>,
    val proficiency: ProficiencyLevel
)

data class PublicAdaptiveQuestion(
    val id: Int,
    val skill: String,
    val text: String,
    val options: List<String>,
    val proficiency: ProficiencyLevel
)

data class AdaptiveTestAnswer(
    val questionId: Int,
    val answer: String
)

data class AnswerEvaluation(
    val questionId: Int,
    val isCorrect: Boolean,
    val proficiency: ProficiencyLevel
)

data class NextQuestionResult(
    val currentProficiency: ProficiencyLevel,
    val answeredCount: Int,
    val answerEvaluations: List<AnswerEvaluation>,
    val isCompleted: Boolean,
    val nextQuestion: PublicAdaptiveQuestion?,
    val proficiency: ProficiencyLevel?
)

data class QuestionBank(
    val beginner: List<PublicAdaptiveQuestion>,
    val intermediate: List<PublicAdaptiveQuestion>,
    val advanced: List<PublicAdaptiveQuestion>
)

enum class ProficiencyLevel(val value: String) {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED");
    
    companion object {
        fun fromString(value: String?): ProficiencyLevel? {
            return when (value?.uppercase()) {
                "BEGINNER" -> BEGINNER
                "INTERMEDIATE" -> INTERMEDIATE
                "ADVANCED" -> ADVANCED
                else -> null
            }
        }
    }
}

