package com.example.lingora_fe.admin.user.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.user.domain.model.UserRoleType
import com.example.lingora_fe.core.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.UserStatus
import com.example.lingora_fe.admin.user.presentation.UserManagementEvent
import com.example.lingora_fe.admin.user.presentation.UserManagementViewModel
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditUserScreen(
    userId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    
    val isEditMode = userId != null

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadUserIntoForm(userId)
        } else {
            viewModel.resetFormState()
        }
    }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess != null) {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information Section
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = GradientStart,
                fontWeight = FontWeight.Bold
            )

            // Username
            OutlinedTextField(
                value = formState.username,
                onValueChange = { 
                    viewModel.updateFormState(formState.copy(username = it))
                },
                label = { Text("Username") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Person, 
                        "Username",
                        tint = GradientStart
                    ) 
                },
                isError = formState.usernameError != null,
                supportingText = formState.usernameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Email
            OutlinedTextField(
                value = formState.email,
                onValueChange = { 
                    viewModel.updateFormState(formState.copy(email = it))
                },
                label = { Text("Email") },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Email, 
                        "Email",
                        tint = GradientStart
                    ) 
                },
                isError = formState.emailError != null,
                supportingText = formState.emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Password Section
            if (!isEditMode || formState.password.isNotBlank()) {
                Text(
                    text = if (isEditMode) "Change Password (Optional)" else "Password",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = GradientStart,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = formState.password,
                    onValueChange = { 
                        viewModel.updateFormState(formState.copy(password = it))
                    },
                    label = { Text(if (isEditMode) "New Password" else "Password") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Lock, 
                            "Password",
                            tint = GradientStart
                        ) 
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = formState.passwordError != null,
                    supportingText = formState.passwordError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var confirmPasswordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = formState.confirmPassword,
                    onValueChange = { 
                        viewModel.updateFormState(formState.copy(confirmPassword = it))
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Lock, 
                            "Confirm Password",
                            tint = GradientStart
                        ) 
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = formState.confirmPasswordError != null,
                    supportingText = formState.confirmPasswordError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Proficiency Level
            Text(
                text = "Proficiency Level",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = GradientStart,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProficiencyLevel.values().forEach { level ->
                    val chipColor = when (level.value) {
                        "BEGINNER" -> Color(0xFF2196F3)
                        "INTERMEDIATE" -> Color(0xFFFF9800)
                        "ADVANCED" -> GradientEnd
                        else -> Color.Gray
                    }
                    FilterChip(
                        selected = formState.selectedProficiency == level,
                        onClick = { 
                            viewModel.updateFormState(formState.copy(selectedProficiency = level))
                        },
                        label = { 
                            Text(
                                level.value,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                fontWeight = if (formState.selectedProficiency == level) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.2f),
                            selectedLabelColor = chipColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Roles
            Text(
                text = "User Roles",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = GradientStart,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            UserRoleType.values().forEach { roleType ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (formState.selectedRoleIds.contains(roleType.id)) {
                            GradientStart.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = formState.selectedRoleIds.contains(roleType.id),
                            onCheckedChange = { checked ->
                                val newRoleIds = if (checked) {
                                    formState.selectedRoleIds + roleType.id
                                } else {
                                    formState.selectedRoleIds - roleType.id
                                }
                                viewModel.updateFormState(formState.copy(selectedRoleIds = newRoleIds))
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GradientStart
                            )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = roleType.value,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (formState.selectedRoleIds.contains(roleType.id)) {
                                    GradientStart
                                } else {
                                    MainText
                                }
                            )
                            Text(
                                text = when (roleType) {
                                    UserRoleType.ADMIN -> "Full access to all features"
                                    UserRoleType.LEARNER -> "Standard user access"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = NavBarText
                            )
                        }
                    }
                }
            }

            // Status (only for edit mode)
            if (isEditMode) {
                Text(
                    text = "Account Status",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = GradientStart,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserStatus.values().filter { it != UserStatus.DELETED }.forEach { status ->
                        val statusColor = when (status) {
                            UserStatus.ACTIVE -> GradientStart
                            UserStatus.INACTIVE -> Color(0xFFeab308)
                            UserStatus.SUSPENDED -> Color(0xFFf97316)
                            UserStatus.BANNED -> Color(0xFFef4444)
                            UserStatus.DELETED -> Color(0xFF6b7280)
                        }
                        val isSelected = formState.selectedStatus == status
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    statusColor.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                }
                            ),
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, statusColor)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            },
                            onClick = { 
                                viewModel.updateFormState(formState.copy(selectedStatus = status))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { 
                                        viewModel.updateFormState(formState.copy(selectedStatus = status))
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = statusColor
                                    )
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = status.value,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) statusColor else MainText
                                    )
                                    Text(
                                        text = when (status) {
                                            UserStatus.ACTIVE -> "User can access all features"
                                            UserStatus.INACTIVE -> "User account is inactive"
                                            UserStatus.SUSPENDED -> "Temporarily restricted access"
                                            UserStatus.BANNED -> "Permanently banned from platform"
                                            UserStatus.DELETED -> "Account has been deleted"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                        color = NavBarText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error Message
            if (state.actionError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.actionError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isEnable = formState.isValid && !state.isCreating && !state.isUpdating
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isCreating && !state.isUpdating
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (isEditMode && userId != null) {
                            viewModel.onEvent(
                                UserManagementEvent.UpdateUser(
                                    userId = userId,
                                    username = formState.username.takeIf { it != state.selectedUser?.username },
                                    email = formState.email.takeIf { it != state.selectedUser?.email },
                                    newPassword = formState.password.takeIf { it.isNotBlank() },
                                    roleIds = formState.selectedRoleIds.takeIf { 
                                        it != state.selectedUser?.roles?.map { role -> role.id } 
                                    },
                                    proficiency = formState.selectedProficiency.value.takeIf { 
                                        it != state.selectedUser?.proficiency 
                                        },
                                    status = formState.selectedStatus.value.takeIf { 
                                        it != state.selectedUser?.status 
                                    }
                                )
                            )
                        } else {
                            viewModel.onEvent(
                                UserManagementEvent.CreateUser(
                                    username = formState.username,
                                    email = formState.email,
                                    password = formState.password,
                                    roleIds = formState.selectedRoleIds,
                                    proficiency = formState.selectedProficiency.value
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            alpha = if (isEnable) 1f else 0.5f
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    enabled = isEnable
                ) {
                    if (state.isCreating || state.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            if (isEditMode) "Update User" else "Create User",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Loading Overlay
        if (state.isUserDetailsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

