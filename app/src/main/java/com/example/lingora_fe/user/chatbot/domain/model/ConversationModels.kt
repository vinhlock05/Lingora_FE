package com.example.lingora_fe.user.chatbot.domain.model

data class ConversationContext(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val iconUrl: String?,
    val systemPrompt: String,
    val difficultyLevel: String, // enum difficulty_level
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String
)

data class ConversationSession(
    val id: String,           // uuid
    val userId: Int,
    val contextId: Int,
    val title: String?,
    val status: String,       // enum status
    val currentPhase: String,
    val totalMessages: Int,
    val errorCount: Int,
    val grammarScore: Float?,
    val fluencyScore: Float?,
    val overallScore: Float?,
    val createdAt: String,
    val updatedAt: String?,
    val endedAt: String?
)

data class ConversationMessage(
    val id: String,           // uuid
    val sessionId: String,    // uuid
    val sender: ChatSender,   // enum sender
    val content: String,      // text
    val corrections: ConversationCorrection?, // jsonb
    val suggestions: List<String>?,           // text_arr
    val createdAt: String
)

data class ConversationCorrection(
    val hasError: Boolean,
    val original: String,
    val corrected: String,
    val explanation: String
)

data class ConversationSuggestionTemplate(
    val id: Int,
    val contextId: Int,
    val phase: String,
    val suggestionText: String,
    val sortOrder: Int
)
