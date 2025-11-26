package com.example.lingora_fe.user.chatbot.data.remote.dto

import com.example.lingora_fe.user.chatbot.domain.model.ChatConversation
import com.example.lingora_fe.user.chatbot.domain.model.ChatMessage
import com.example.lingora_fe.user.chatbot.domain.model.ChatSender
import com.example.lingora_fe.user.chatbot.domain.model.ChatSession
import com.example.lingora_fe.user.chatbot.domain.model.ChatSessionDetail
import com.google.gson.annotations.SerializedName

data class ChatSessionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("userId")
    val userId: Int?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ChatMessageDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("sender")
    val sender: String,
    @SerializedName("createdAt")
    val createdAt: String
)

data class ChatSendRequestDto(
    @SerializedName("question")
    val question: String,
    @SerializedName("sessionId")
    val sessionId: String? = null
)

data class ChatSendResponseDto(
    @SerializedName("session")
    val session: ChatSessionDto,
    @SerializedName("answer")
    val answer: String,
    @SerializedName("messages")
    val messages: List<ChatMessageDto>
)

data class ChatSessionsResponseDto(
    @SerializedName("sessions")
    val sessions: List<ChatSessionDto>
)

data class ChatSessionMessagesResponseDto(
    @SerializedName("session")
    val session: ChatSessionDto,
    @SerializedName("messages")
    val messages: List<ChatMessageDto>
)

data class ChatDeleteSessionResponseDto(
    @SerializedName("sessionId")
    val sessionId: String
)

fun ChatSessionDto.toDomain(): ChatSession {
    return ChatSession(
        id = id,
        title = title,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ChatMessageDto.toDomain(): ChatMessage {
    val safeSender = runCatching {
        ChatSender.valueOf(sender.uppercase())
    }.getOrDefault(ChatSender.AI)
    return ChatMessage(
        id = id,
        content = content,
        sender = safeSender,
        createdAt = createdAt
    )
}

fun ChatSendResponseDto.toDomain(): ChatConversation {
    return ChatConversation(
        session = session.toDomain(),
        answer = answer,
        messages = messages.map { it.toDomain() }
    )
}

fun ChatSessionsResponseDto.toDomain(): List<ChatSession> {
    return sessions.map { it.toDomain() }
}

fun ChatSessionMessagesResponseDto.toDomain(): ChatSessionDetail {
    return ChatSessionDetail(
        session = session.toDomain(),
        messages = messages.map { it.toDomain() }
    )
}

fun ChatDeleteSessionResponseDto.toDomain(): String = sessionId

