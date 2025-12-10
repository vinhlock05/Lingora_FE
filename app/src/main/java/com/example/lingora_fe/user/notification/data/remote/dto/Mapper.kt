package com.example.lingora_fe.user.notification.data.remote.dto

import com.example.lingora_fe.user.notification.domain.model.Notification
import com.example.lingora_fe.user.notification.domain.model.NotificationListMetadata
import com.example.lingora_fe.user.notification.domain.model.NotificationType
import com.example.lingora_fe.user.notification.domain.model.NotificationTarget

// DTO to Domain
fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        isRead = isRead,
        readAt = readAt,
        type = NotificationType.values().find { it.value == type } ?: NotificationType.LIKE,
        message = message,
        data = data,
        target = NotificationTarget.values().find { it.value == target },
        createdAt = createdAt
    )
}

fun NotificationListMetaData.toDomain(): NotificationListMetadata {
    return NotificationListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        unreadCount = unreadCount,
        notifications = notifications.map { it.toDomain() }
    )
}
