package com.example.lingora_fe.user.notification.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.notification.domain.model.Notification
import com.example.lingora_fe.user.notification.domain.model.NotificationFilterOptions
import com.example.lingora_fe.user.notification.domain.model.NotificationListMetadata
import com.example.lingora_fe.user.notification.domain.model.UnreadCountMetadata

interface NotificationRepository {

    /**
     * Get list of notifications
     */
    suspend fun getNotifications(
        token: String,
        filterOptions: NotificationFilterOptions
    ): Either<AppFailure, NotificationListMetadata>

    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(
        token: String
    ): Either<AppFailure, UnreadCountMetadata>

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(
        token: String,
        notificationId: Int
    ): Either<AppFailure, Notification>

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(
        token: String
    ): Either<AppFailure, Unit>
}

