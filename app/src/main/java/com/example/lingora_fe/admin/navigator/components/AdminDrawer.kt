package com.example.lingora_fe.admin.navigator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.admin.navigator.DrawerNavItem

@Composable
fun AdminDrawerContent(
    currentRoute: String,
    userName: String,
    userEmail: String,
    onNavigateToItem: (String) -> Unit,
    onLogout: () -> Unit,
    canSwitchToUser: Boolean = false,
    onSwitchToUser: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Scrollable content (header + nav items)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with admin profile
                AdminDrawerHeader(
                    userName = userName,
                    userEmail = userEmail
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Navigation items
                DrawerNavItem.items.forEach { item ->
                    DrawerMenuItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigateToItem(item.route) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Switch to User View button (if user has LEARNER role)
            if (canSwitchToUser && onSwitchToUser != null) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.School, contentDescription = null) },
                    label = { 
                        Column {
                            Text(
                                text = "Chuyển sang chế độ Người học",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Sử dụng ứng dụng như người dùng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    selected = false,
                    onClick = onSwitchToUser,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Logout button (fixed at bottom)
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                label = { Text("Logout") },
                selected = false,
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

@Composable
private fun AdminDrawerHeader(
    userName: String,
    userEmail: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Admin avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Administrator",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    item: DrawerNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { 
            Icon(
                item.icon,
                contentDescription = null
            )
        },
        label = { 
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

