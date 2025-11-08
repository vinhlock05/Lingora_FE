package com.example.lingora_fe.admin.user.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.common.presentation.components.ErrorContent
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.presentation.UserManagementEvent
import com.example.lingora_fe.admin.user.presentation.UserManagementViewModel
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    userId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.onEvent(UserManagementEvent.LoadUserDetails(userId))
    }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess != null) {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            state.isUserDetailsLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.onEvent(UserManagementEvent.LoadUserDetails(userId)) }
                )
            }
            state.selectedUser != null -> {
                UserDetailsContent(
                    user = state.selectedUser!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.onEvent(UserManagementEvent.DeleteUser(userId))
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            icon = { Icon(Icons.Default.Restore, "Restore") },
            title = { Text("Restore User") },
            text = { Text("Are you sure you want to restore this user?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(UserManagementEvent.RestoreUser(userId))
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    )
                ) {
                    Text("Restore", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserDetailsContent(
    user: AdminUser,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // User Avatar and Name
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(2).uppercase(),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = user.status)
                ProficiencyChip(proficiency = user.proficiency)
            }
        }

        Divider()

        // Basic Information
        InfoSection(title = "Basic Information") {
            InfoItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = user.email
            )
            InfoItem(
                icon = Icons.Default.Badge,
                label = "User ID",
                value = user.id.toString()
            )
            InfoItem(
                icon = Icons.Default.CalendarToday,
                label = "Created At",
                value = formatDate(user.createdAt)
            )
        }

        // Roles
        InfoSection(title = "Roles & Permissions") {
            user.roles.forEach { role ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GradientStart.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (role.name == "ADMIN") Icons.Default.AdminPanelSettings 
                            else Icons.Default.Person,
                            contentDescription = null,
                            tint = GradientStart,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = role.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold,
                                color = GradientStart
                            )
                            Text(
                                text = when (role.name) {
                                    "ADMIN" -> "Full access to all features"
                                    "LEARNER" -> "Standard user access"
                                    else -> "Custom role"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = NavBarText
                            )
                        }
                    }
                }
            }
        }

        // Proficiency Details
        InfoSection(title = "Learning Profile") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (user.proficiency) {
                        "BEGINNER" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                        "INTERMEDIATE" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        "ADVANCED" -> GradientEnd.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = when (user.proficiency) {
                            "BEGINNER" -> Color(0xFF2196F3)
                            "INTERMEDIATE" -> Color(0xFFFF9800)
                            "ADVANCED" -> GradientEnd
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Proficiency Level",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = NavBarText
                        )
                        Text(
                            text = user.proficiency,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = when (user.proficiency) {
                                "BEGINNER" -> Color(0xFF2196F3)
                                "INTERMEDIATE" -> Color(0xFFFF9800)
                                "ADVANCED" -> GradientEnd
                                else -> MainText
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = GradientStart
        )
        content()
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = GradientStart,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = NavBarText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Medium,
                color = MainText
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

