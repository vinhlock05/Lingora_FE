// data/remote/dto/ConversationDtos.kt
package com.example.lingora_fe.user.conversation.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.lingora_fe.user.conversation.domain.model.*

data class ConversationContextDto(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val iconUrl: String?,
    val difficultyLevel: String,
    val isActive: Boolean,
    val sortOrder: Int
)

fun ConversationContextDto.toDomain() = ConversationContext(
    id = id, name = name, slug = slug, description = description,
    iconUrl = iconUrl, difficultyLevel = difficultyLevel,
    isActive = isActive, sortOrder = sortOrder
)

data class ConversationSessionDto(
    val id: String,
    val context: ConversationContextDto?,
    val title: String?,
    val status: String,
    val currentPhase: String,
    val totalMessages: Int,
    val errorCount: Int,
    val grammarScore: Float?,
    val fluencyScore: Float?,
    val overallScore: Float?,
    val createdAt: String,
    val endedAt: String?,
    val feedback: String?,
    val messages: List<ConversationMessageDto>? = null
)

fun ConversationSessionDto.toDomain() = ConversationSession(
    id = id, contextId = context?.id ?: 0,
    context = context?.toDomain(),
    title = title, status = status, currentPhase = currentPhase,
    totalMessages = totalMessages, errorCount = errorCount,
    grammarScore = grammarScore, fluencyScore = fluencyScore,
    overallScore = overallScore, createdAt = createdAt,
    endedAt = endedAt, feedback = feedback,
    messages = messages?.map { it.toDomain() }
)

data class ConversationMessageDto(
    val id: String,
    val sender: String,
    val content: String,
    val corrections: ConversationCorrectionDto?,
    val improvement: ConversationImprovementDto?,
    val vocabulary: ConversationVocabularyDto?,
    val suggestions: List<String>?,
    val createdAt: String
)

data class ConversationImprovementDto(
    @SerializedName("has_improvement") val hasImprovement: Boolean,
    val original: String?,
    val improved: String?,
    val explanation: String?
)

data class ConversationVocabularyDto(
    val highlight: String?,
    val meaning: String?
)

data class ConversationCorrectionDto(
    @SerializedName("has_error") val hasError: Boolean,
    @SerializedName("errors") val errors: List<ErrorDetailDto>?
)

data class ErrorDetailDto(
    val wrong: String,
    val correct: String,
    val explanation: String
)

fun ConversationMessageDto.toDomain() = ConversationMessage(
    id = id, sender = sender, content = content,
    corrections = corrections?.let { dto ->
        ConversationCorrection(
            hasError = dto.hasError,
            errors = dto.errors?.map { ErrorDetail(it.wrong, it.correct, it.explanation) } ?: emptyList()
        )
    },
    improvement = improvement?.let { dto ->
        ConversationImprovement(
            hasImprovement = dto.hasImprovement,
            original = dto.original,
            improved = dto.improved,
            explanation = dto.explanation
        )
    },
    vocabulary = vocabulary?.let { dto ->
        ConversationVocabulary(
            highlight = dto.highlight,
            meaning = dto.meaning
        )
    },
    suggestions = suggestions,
    createdAt = createdAt
)

data class PaginatedContextsDto(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val contexts: List<ConversationContextDto>
)

data class ContextsResponseDto(
    val metaData: PaginatedContextsDto
)

data class SessionResponseDto(
    val metaData: ConversationSessionDto
)

data class SendMessageResponseDto(
    val metaData: SendMessageDataDto
)

data class SendMessageDataDto(
    val session: ConversationSessionDto,
    val messages: List<ConversationMessageDto>
)

data class SessionDetailResponseDto(
    val metaData: ConversationSessionDto
)

data class PaginatedSessionsDto(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val sessions: List<ConversationSessionDto>
)

data class SessionsResponseDto(
    val metaData: PaginatedSessionsDto
)

data class TemplatesResponseDto(
    val metaData: List<ConversationSuggestionTemplateDto>
)

data class ConversationSuggestionTemplateDto(
    val id: Int,
    val phase: String,
    val suggestionText: String,
    val sortOrder: Int
)

fun ConversationSuggestionTemplateDto.toDomain() = ConversationSuggestionTemplate(
    id = id, phase = phase, suggestionText = suggestionText, sortOrder = sortOrder
)
