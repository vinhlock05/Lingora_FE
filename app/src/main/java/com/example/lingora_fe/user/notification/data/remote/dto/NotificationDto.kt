package com.example.lingora_fe.user.notification.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

// MetaData DTOs
data class NotificationListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("unreadCount")
    val unreadCount: Int,
    @SerializedName("notifications")
    val notifications: List<NotificationDto>
)

data class NotificationDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("message")
    val message: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("readAt")
    val readAt: String?,
    @SerializedName("data")
    val data: JsonObject?,
    @SerializedName("target")
    val target: String?,
    @SerializedName("createdAt")
    val createdAt: String
)