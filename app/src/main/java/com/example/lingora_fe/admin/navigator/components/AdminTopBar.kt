package com.example.lingora_fe.admin.navigator.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    title: String,
    onMenuClick: () -> Unit,
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { 
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = if (showBackButton) onBackClick else onMenuClick) {
                Icon(
                    if (showBackButton) Icons.Default.ArrowBack else Icons.Default.Menu,
                    contentDescription = if (showBackButton) "Back" else "Menu"
                )
            }
        },
        actions = {
            // Custom actions
            actions()
            
            // Notifications
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge {
                            Text(
                                text = if (notificationCount > 99) "99+" else notificationCount.toString()
                            )
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

