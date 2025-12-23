package com.example.lingora_fe.user.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
import com.example.lingora_fe.user.notification.domain.model.NotificationFilterOptions
import com.example.lingora_fe.user.notification.domain.model.NotificationType
import com.example.lingora_fe.user.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val tokenManager: TokenManager,
    private val socketManager: NotificationSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    init {
        loadNotifications(1)
        connectSocket()
        observeSocket()
    }

    fun loadNotifications(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val token = tokenManager.getAccessToken() ?: ""
            repository.getNotifications(token, NotificationFilterOptions(page = page, limit = 20)).fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(isLoading = false, error = failure.message)
                },
                ifRight = { result ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        notifications = if (page == 1) result.notifications else _state.value.notifications + result.notifications,
                        currentPage = result.currentPage,
                        totalPages = result.totalPages,
                        total = result.total,
                        unreadCount = result.unreadCount
                    )
                }
            )
        }
    }

    fun navigateRouteFor(notification: com.example.lingora_fe.user.notification.domain.model.Notification): String? {
        val data = notification.data
        val type = notification.type

        fun getInt(key: String): Int? {
            val el = try { data?.get(key) } catch (_: Exception) { null }
            if (el == null) return null
            return try {
                el.asInt
            } catch (_: Exception) {
                try {
                    el.asString.toIntOrNull()
                } catch (_: Exception) { null }
            }
        }

        fun getNestedInt(objKey: String, idKey: String = "id"): Int? {
            val obj = try { data?.get(objKey)?.asJsonObject } catch (_: Exception) { null }
            if (obj == null) return null
            val el = try { obj.get(idKey) } catch (_: Exception) { null }
            if (el == null) return null
            return try {
                el.asInt
            } catch (_: Exception) {
                el.asString.toIntOrNull()
            }
        }

        val studySetId =
            getInt("studySetId")
                ?: getInt("studysetId")
                ?: getInt("studySetID")
                ?: getInt("study_set_id")
                ?: getNestedInt("studySet")
        val relatedId =
            getInt("relatedId")
                ?: getInt("relatedID")
                ?: getInt("related_id")
                ?: getNestedInt("related")
        val postId =
            getInt("postId")
                ?: getInt("postID")
                ?: getInt("post_id")
                ?: getNestedInt("post")
        val objectId =
            getInt("objectId")
                ?: getInt("targetId")

        return when (type) {
            NotificationType.LIKE,
            NotificationType.COMMENT,
            NotificationType.ORDER -> {
                val id = studySetId ?: relatedId ?: objectId
                if (id != null) Route.studySetDetail(id)
                else if (postId != null) Route.postDetail(postId)
                else null
            }
            NotificationType.WARNING -> {
                // WARNING notification contains targetType and targetId
                val targetType = try { data?.get("targetType")?.asString } catch (_: Exception) { null }
                val targetId = getInt("targetId")
                
                when (targetType) {
                    "POST" -> if (targetId != null) Route.postDetail(targetId) else null
                    "STUDY_SET" -> if (targetId != null) Route.studySetDetail(targetId) else null
                    "COMMENT" -> {
                        // For comments, try to navigate to parent post if available
                        if (postId != null) Route.postDetail(postId)
                        else null
                    }
                    else -> null
                }
            }
            NotificationType.CHANGE_PASSWORD -> Route.ProfileTab.route
            NotificationType.CONTENT_DELETED -> null // TODO: handle content deleted navigation
            // Withdrawal notifications - navigate to withdrawal detail if ID available
            NotificationType.WITHDRAWAL_PROCESSING,
            NotificationType.WITHDRAWAL_COMPLETED,
            NotificationType.WITHDRAWAL_REJECTED,
            NotificationType.WITHDRAWAL_FAILED -> {
                val withdrawalId = getInt("withdrawalId")
                if (withdrawalId != null) Route.withdrawalDetail(withdrawalId)
                else Route.WithdrawalList.route
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.currentPage < s.totalPages && !s.isLoading) {
            loadNotifications(s.currentPage + 1)
        }
    }

    fun refresh() {
        loadNotifications(1)
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            if (_state.value.isMarkingReadId == notificationId) return@launch
            _state.value = _state.value.copy(isMarkingReadId = notificationId)
            val token = tokenManager.getAccessToken() ?: ""
            repository.markAsRead(token, notificationId).fold(
                ifLeft = {
                    _state.value = _state.value.copy(isMarkingReadId = null)
                },
                ifRight = { updated ->
                    val updatedList = _state.value.notifications.map { existing ->
                        if (existing.id == notificationId) {
                            existing.copy(
                                isRead = updated.isRead,
                                readAt = updated.readAt,
                                type = existing.type,
                                message = existing.message ?: updated.message,
                                data = existing.data ?: updated.data,
                                target = existing.target ?: updated.target,
                                createdAt = existing.createdAt
                            )
                        } else existing
                    }
                    val newUnread = (_state.value.unreadCount - if (!updated.isRead) 0 else 1).coerceAtLeast(0)
                    _state.value = _state.value.copy(
                        isMarkingReadId = null,
                        notifications = updatedList,
                        unreadCount = newUnread
                    )
                }
            )
        }
    }

    private fun connectSocket() {
        val token = tokenManager.getAccessToken() ?: return
        if (!socketManager.isConnected()) {
            val userId = tokenManager.getUserId()
            socketManager.connect(token, com.example.lingora_fe.util.Constant.BASE_URL, userId)
        }
    }

    private fun observeSocket() {
        viewModelScope.launch {
            socketManager.notificationFlow().collect { noti ->
                val list = listOf(noti) + _state.value.notifications
                _state.value = _state.value.copy(
                    notifications = list,
                    unreadCount = _state.value.unreadCount + 1,
                    total = _state.value.total + 1
                )
            }
        }
    }
}

