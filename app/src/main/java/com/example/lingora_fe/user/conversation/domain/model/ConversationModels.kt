// domain/model/ConversationModels.kt
package com.example.lingora_fe.user.conversation.domain.model

data class ConversationContext(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val iconUrl: String?,
    val difficultyLevel: String, // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    val isActive: Boolean,
    val sortOrder: Int
)

data class ConversationSession(
    val id: String,
    val contextId: Int,
    val context: ConversationContext?,
    val title: String?,
    val status: String,           // "ACTIVE", "COMPLETED"
    val currentPhase: String,     // "opening", "developing", "closing", "completed"
    val totalMessages: Int,
    val errorCount: Int,
    val grammarScore: Float?,
    val fluencyScore: Float?,
    val overallScore: Float?,
    val createdAt: String,
    val endedAt: String?,
    val feedback: String?,
    val messages: List<ConversationMessage>? = null
)

data class ConversationMessage(
    val id: String,
    val sender: String,           // "USER", "AI"
    val content: String,
    val corrections: ConversationCorrection?,
    val improvement: ConversationImprovement?,
    val vocabulary: ConversationVocabulary?,
    val suggestions: List<String>?,
    val createdAt: String
)

data class ConversationCorrection(
    val hasError: Boolean,
    val errors: List<ErrorDetail> = emptyList()
)

data class ErrorDetail(
    val wrong: String,
    val correct: String,
    val explanation: String
)

data class ConversationImprovement(
    val hasImprovement: Boolean,
    val original: String?,
    val improved: String?,
    val explanation: String?
)

data class ConversationVocabulary(
    val highlight: String?,
    val meaning: String?
)

data class ConversationSuggestionTemplate(
    val id: Int,
    val phase: String,
    val suggestionText: String,
    val sortOrder: Int
)

data class PaginatedResponse<T>(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val items: List<T>
)
