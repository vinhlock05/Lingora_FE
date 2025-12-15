package com.example.lingora_fe.user.notification.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.notification.data.remote.api.NotificationApiService
import com.example.lingora_fe.user.notification.data.remote.dto.toDomain
import com.example.lingora_fe.user.notification.domain.model.Notification
import com.example.lingora_fe.user.notification.domain.model.NotificationFilterOptions
import com.example.lingora_fe.user.notification.domain.model.NotificationListMetadata
import com.example.lingora_fe.user.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val apiService: NotificationApiService
) : NotificationRepository {

    override suspend fun getNotifications(
        token: String,
        filterOptions: NotificationFilterOptions
    ): Either<AppFailure, NotificationListMetadata> {
        return Either.catch {
            val response = apiService.getNotifications(
                page = filterOptions.page,
                limit = filterOptions.limit
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun markAsRead(
        token: String,
        notificationId: Int
    ): Either<AppFailure, Notification> {
        return Either.catch {
            val response = apiService.markAsRead(notificationId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
}

