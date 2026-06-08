package com.example.lingora_fe.user.notification.domain.model

import com.google.gson.JsonObject

data class Notification(
    val id: Int,
    val isRead: Boolean,
    val readAt: String?,
    val type: NotificationType,
    val message: String?,
    val data: JsonObject?,
    val target: NotificationTarget?,
    val createdAt: String
)

data class NotificationListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val unreadCount: Int,
    val notifications: List<Notification>
)

// Filter data for queries
data class NotificationFilterOptions(
    val page: Int = 1,
    val limit: Int = 10
)

// Enums for type safety
enum class NotificationType(val value: String) {
    CHANGE_PASSWORD("Change password"),
    LIKE("Like"),
    COMMENT("Comment"),
    ORDER("Order"),
    WARNING("Warning"),
    CONTENT_DELETED("Content deleted"),
    CLASSROOM_APPROVED("Classroom approved"),
    CLASSROOM_JOIN_REQUEST("Classroom join request"),
    CLASSROOM_MEMBER_JOINED("Classroom member joined"),
    CLASSROOM_NEW_LESSON("Classroom new lesson"),
    // Withdrawal notifications
    WITHDRAWAL_PROCESSING("Withdrawal processing"),
    WITHDRAWAL_COMPLETED("Withdrawal completed"),
    WITHDRAWAL_REJECTED("Withdrawal rejected"),
    WITHDRAWAL_FAILED("Withdrawal failed")
}

enum class NotificationTarget(val value: String) {
    ALL("All"),
    ONLY_USER("Only user"),
    SEGMENT("Segment")
}

