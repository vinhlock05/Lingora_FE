package com.example.lingora_fe.user.notification.data.remote.dto

import com.google.gson.annotations.SerializedName

// MetaData DTOs
data class NotificationListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("notifications")
    val notifications: List<NotificationDto>
)

data class NotificationDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("relatedId")
    val relatedId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
)

data class UnreadCountMetaData(
    @SerializedName("unreadCount")
    val unreadCount: Int
)

