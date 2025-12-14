package com.example.lingora_fe.user.notification.presentation

import com.example.lingora_fe.user.notification.domain.model.Notification

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val unreadCount: Int = 0,
    val isMarkingReadId: Int? = null
)

