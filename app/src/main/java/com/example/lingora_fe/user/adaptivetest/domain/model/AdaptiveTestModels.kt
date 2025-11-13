package com.example.lingora_fe.user.adaptivetest.domain.model

import com.example.lingora_fe.core.domain.model.ProficiencyLevel

data class PublicAdaptiveQuestion(
    val id: Int,
    val skill: String,
    val text: String,
    val passage: String? = null,
    val options: List<String>,
    val answer: String? = null,
    val proficiency: ProficiencyLevel
)

data class AdaptiveTestAnswer(
    val questionId: Int,
    val answer: String
)

data class AnswerEvaluation(
    val questionId: Int,
    val isCorrect: Boolean,
    val correctAnswer: String? = null,
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
