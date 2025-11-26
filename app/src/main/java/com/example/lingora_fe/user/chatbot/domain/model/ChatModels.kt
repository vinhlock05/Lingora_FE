package com.example.lingora_fe.user.chatbot.domain.model

enum class ChatSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String,
    val content: String,
    val sender: ChatSender,
    val createdAt: String
)

data class ChatSession(
    val id: String,
    val title: String,
    val userId: Int?,
    val createdAt: String,
    val updatedAt: String
)

data class ChatConversation(
    val session: ChatSession,
    val answer: String,
    val messages: List<ChatMessage>
)

data class ChatSessionDetail(
    val session: ChatSession,
    val messages: List<ChatMessage>
)

