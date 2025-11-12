package com.example.lingora_fe.user.adaptivetest.presentation

import com.example.lingora_fe.user.adaptivetest.domain.model.*

data class AdaptiveTestState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentQuestion: PublicAdaptiveQuestion? = null,
    val answeredQuestions: List<AdaptiveTestAnswer> = emptyList(),
    val answerEvaluations: List<AnswerEvaluation> = emptyList(),
    val currentProficiency: ProficiencyLevel = ProficiencyLevel.BEGINNER,
    val answeredCount: Int = 0,
    val isCompleted: Boolean = false,
    val finalProficiency: ProficiencyLevel? = null,
    val selectedAnswer: String? = null
)

