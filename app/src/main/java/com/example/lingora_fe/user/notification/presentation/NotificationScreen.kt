package com.example.lingora_fe.user.notification.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.notification.domain.model.Notification
import com.example.lingora_fe.user.notification.domain.model.NotificationType
import com.example.lingora_fe.util.DateFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNavigateTo: (String) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel(),
    showInternalTopBar: Boolean = true
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = if (showInternalTopBar) ({
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Thông báo",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (state.unreadCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(
                                    text = if (state.unreadCount > 99) "99+" else state.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }) else ({}) ,
        containerColor = Color(0xFFF0F9F4)
    ) { paddingValues ->
        if (state.isLoading && state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "Chưa có thông báo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F9F4)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = state.notifications,
                key = { it.id }
            ) { notification ->
                NotificationItem(
                    notification = notification,
                    isMarking = state.isMarkingReadId == notification.id,
                    onClick = {
                        val route = viewModel.navigateRouteFor(notification)
                        if (route != null) {
                            onNavigateTo(route)
                            viewModel.markAsRead(notification.id)
                        } else {
                            onNavigateTo(Route.StudySetList.route)
                            viewModel.markAsRead(notification.id)
                        }
                    }
                )
                if (notification != state.notifications.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }

            if (state.currentPage < state.totalPages) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.loadMore() },
                                modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                Text("Tải thêm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.CHANGE_PASSWORD -> Icons.Default.Lock
    NotificationType.LIKE -> Icons.Default.Favorite
    NotificationType.COMMENT -> Icons.Default.Comment
    NotificationType.ORDER -> Icons.Default.ShoppingCart
    NotificationType.WARNING -> Icons.Default.Warning
    NotificationType.CONTENT_DELETED -> Icons.Default.Delete
    NotificationType.CLASSROOM_APPROVED -> Icons.Default.School
    // Withdrawal notifications
    NotificationType.WITHDRAWAL_PROCESSING -> Icons.Default.AccountBalanceWallet
    NotificationType.WITHDRAWAL_COMPLETED -> Icons.Default.CheckCircle
    NotificationType.WITHDRAWAL_REJECTED -> Icons.Default.Cancel
    NotificationType.WITHDRAWAL_FAILED -> Icons.Default.Error
}

@Composable
fun NotificationItem(
    notification: Notification,
    isMarking: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = !isMarking),
        color = if (!notification.isRead) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (!notification.isRead) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = if (!notification.isRead) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title with badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notification.type.value,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AnimatedVisibility(
                        visible = !notification.isRead,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // Message
                notification.message?.let { message ->
                    val formattedMessage = message
                        .replace("POST", "bài viết")
                        .replace("STUDY_SET", "học liệu")
                        .replace("COMMENT", "bình luận")
                        .replace("Post", "Bài viết")
                        .replace("Study set", "Học liệu")
                        .replace("Comment", "Bình luận")
                    
                    Text(
                        text = formattedMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Timestamp
                Text(
                    text = DateFormatHelper.formatTimeAgo(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Loading indicator when marking as read
            if (isMarking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
