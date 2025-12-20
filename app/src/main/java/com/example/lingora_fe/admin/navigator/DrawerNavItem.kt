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
    
    object CategoryManagement : DrawerNavItem(
        route = "admin_category_management",
        title = "Category Management",
        icon = Icons.Default.Category,
        description = "Manage learning categories"
    )
    
    object TopicManagement : DrawerNavItem(
        route = "admin_topic_management",
        title = "Topic Management",
        icon = Icons.Default.Topic,
        description = "Manage learning topics"
    )
    
    object WordManagement : DrawerNavItem(
        route = "admin_word_management",
        title = "Word Management",
        icon = Icons.Default.TextFields,
        description = "Manage words"
    )
    
    object ReportManagement : DrawerNavItem(
        route = "admin_report_management",
        title = "Report Management",
        icon = Icons.Default.Report,
        description = "Manage user reports and violations"
    )
    
    object WithdrawalManagement : DrawerNavItem(
        route = "admin_withdrawal_management",
        title = "Withdrawal Management",
        icon = Icons.Default.AccountBalance,
        description = "Manage withdrawal requests"
    )

    companion object {
        val items = listOf(
            Dashboard,
            UserManagement,
            CategoryManagement,
            TopicManagement,
            WordManagement,
            ReportManagement,
            WithdrawalManagement
        )
    }
}

