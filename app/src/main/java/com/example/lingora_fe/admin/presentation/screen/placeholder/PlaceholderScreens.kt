package com.example.lingora_fe.admin.presentation.screen.placeholder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    PlaceholderScreen(
        icon = Icons.Default.Dashboard,
        title = "Dashboard",
        description = "Overview and statistics will be displayed here.\n\nThis screen will include:\n• User statistics\n• Activity graphs\n• Recent activities\n• System health"
    )
}

@Composable
fun ContentManagementScreen() {
    PlaceholderScreen(
        icon = Icons.Default.Article,
        title = "Content Management",
        description = "Manage learning content here.\n\nFeatures:\n• Vocabulary management\n• Course management\n• Material uploads\n• Content organization"
    )
}

@Composable
fun ForumManagementScreen() {
    PlaceholderScreen(
        icon = Icons.Default.Forum,
        title = "Forum Management",
        description = "Manage forum posts and discussions.\n\nFeatures:\n• Monitor posts\n• Moderate comments\n• Ban/unban users\n• Manage categories"
    )
}

@Composable
fun AnalyticsScreen() {
    PlaceholderScreen(
        icon = Icons.Default.Analytics,
        title = "Analytics",
        description = "View detailed analytics and reports.\n\nIncludes:\n• User engagement metrics\n• Learning progress statistics\n• Popular content\n• Performance reports"
    )
}

@Composable
fun SettingsScreen() {
    PlaceholderScreen(
        icon = Icons.Default.Settings,
        title = "Settings",
        description = "System settings and configuration.\n\nOptions:\n• General settings\n• Notification settings\n• Email templates\n• System maintenance"
    )
}

@Composable
private fun PlaceholderScreen(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 500.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Divider()
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Coming Soon",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

