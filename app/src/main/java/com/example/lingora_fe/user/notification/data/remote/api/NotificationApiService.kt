package com.example.lingora_fe.user.notification.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.notification.data.remote.dto.NotificationDto
import com.example.lingora_fe.user.notification.data.remote.dto.NotificationListMetaData
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<NotificationListMetaData>

    @PATCH("notifications/{id}")
    suspend fun markAsRead(
        @Path("id") notificationId: Int
    ): ApiResponse<NotificationDto>
}

