package com.example.lingora_fe.user.notification.domain.model

data class Notification(
    val id: Int,
    val relatedId: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: String
)

data class NotificationListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val notifications: List<Notification>
)

data class UnreadCountMetadata(
    val unreadCount: Int
)

// Filter data for queries
data class NotificationFilterOptions(
    val page: Int = 1,
    val limit: Int = 20,
    val isRead: Boolean? = null
)

// Enums for type safety
enum class NotificationType(val value: String) {
    STUDY_SET("STUDY_SET"),
    COMMENT("COMMENT"),
    LIKE("LIKE")
}

