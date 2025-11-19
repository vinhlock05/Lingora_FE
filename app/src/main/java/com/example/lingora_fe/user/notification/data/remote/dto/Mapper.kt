package com.example.lingora_fe.user.notification.data.remote.dto

import com.example.lingora_fe.user.notification.domain.model.Notification
import com.example.lingora_fe.user.notification.domain.model.NotificationListMetadata
import com.example.lingora_fe.user.notification.domain.model.NotificationType
import com.example.lingora_fe.user.notification.domain.model.UnreadCountMetadata

// DTO to Domain
fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        relatedId = relatedId,
        title = title,
        message = message,
        type = NotificationType.values().find { it.value == type } ?: NotificationType.STUDY_SET,
        isRead = isRead,
        createdAt = createdAt
    )
}

fun NotificationListMetaData.toDomain(): NotificationListMetadata {
    return NotificationListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        notifications = notifications.map { it.toDomain() }
    )
}

fun UnreadCountMetaData.toDomain(): UnreadCountMetadata {
    return UnreadCountMetadata(
        unreadCount = unreadCount
    )
}

