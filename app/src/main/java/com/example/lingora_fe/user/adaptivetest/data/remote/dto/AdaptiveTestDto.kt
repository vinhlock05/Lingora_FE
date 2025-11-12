package com.example.lingora_fe.user.adaptivetest.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request DTOs
data class GetNextQuestionRequest(
    @SerializedName("answeredQuestions")
    val answeredQuestions: List<AdaptiveTestAnswerDto> = emptyList()
)

data class AdaptiveTestAnswerDto(
    @SerializedName("questionId")
    val questionId: Int,
    
    @SerializedName("answer")
    val answer: String
)

// Response DTOs
data class QuestionBankMetaData(
    @SerializedName("BEGINNER")
    val beginner: List<PublicAdaptiveQuestionDto> = emptyList(),
    
    @SerializedName("INTERMEDIATE")
    val intermediate: List<PublicAdaptiveQuestionDto> = emptyList(),
    
    @SerializedName("ADVANCED")
    val advanced: List<PublicAdaptiveQuestionDto> = emptyList()
)

data class PublicAdaptiveQuestionDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("skill")
    val skill: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("options")
    val options: List<String>,
    
    @SerializedName("proficiency")
    val proficiency: String
)

data class NextQuestionMetaData(
    @SerializedName("currentProficiency")
    val currentProficiency: String,
    
    @SerializedName("answeredCount")
    val answeredCount: Int,
    
    @SerializedName("answerEvaluations")
    val answerEvaluations: List<AnswerEvaluationDto>,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean,
    
    @SerializedName("nextQuestion")
    val nextQuestion: PublicAdaptiveQuestionDto?,
    
    @SerializedName("proficiency")
    val proficiency: String?
)

data class AnswerEvaluationDto(
    @SerializedName("questionId")
    val questionId: Int,
    
    @SerializedName("isCorrect")
    val isCorrect: Boolean,
    
    @SerializedName("proficiency")
    val proficiency: String
)

