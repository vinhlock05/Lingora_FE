package com.example.lingora_fe.admin.navigator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DrawerNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    object Dashboard : DrawerNavItem(
        route = "admin_dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard,
        description = "Overview and statistics"
    )
    
    object UserManagement : DrawerNavItem(
        route = "admin_user_management",
        title = "User Management",
        icon = Icons.Default.People,
        description = "Manage users and permissions"
    )
    
    object ContentManagement : DrawerNavItem(
        route = "admin_content",
        title = "Content Management",
        icon = Icons.Default.Article,
        description = "Manage learning content"
    )
    
    object ForumManagement : DrawerNavItem(
        route = "admin_forum",
        title = "Forum Management",
        icon = Icons.Default.Forum,
        description = "Manage forum posts and comments"
    )
    
    object Analytics : DrawerNavItem(
        route = "admin_analytics",
        title = "Analytics",
        icon = Icons.Default.Analytics,
        description = "View statistics and reports"
    )
    
    object Settings : DrawerNavItem(
        route = "admin_settings",
        title = "Settings",
        icon = Icons.Default.Settings,
        description = "System settings"
    )

    companion object {
        val items = listOf(
            Dashboard,
            UserManagement,
            ContentManagement,
            ForumManagement,
            Analytics,
            Settings
        )
    }
}

