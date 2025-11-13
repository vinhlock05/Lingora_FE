package com.example.lingora_fe.user.adaptivetest.presentation

import com.example.lingora_fe.core.domain.model.ProficiencyLevel
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
    val selectedAnswer: String? = null,
    val lastAnsweredQuestion: PublicAdaptiveQuestion? = null,
    val lastSelectedAnswer: String? = null,
    val lastQuestionId: Int? = null, // Store question ID to find evaluation
    val lastCorrectAnswer: String? = null, // Store correct answer for the last question
    val showResult: Boolean = false,
    val nextQuestion: PublicAdaptiveQuestion? = null // Store next question while showing result
)

