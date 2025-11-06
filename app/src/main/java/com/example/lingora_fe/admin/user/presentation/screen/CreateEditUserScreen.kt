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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lingora_fe.admin.user.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.UserRoleType
import com.example.lingora_fe.admin.user.domain.model.UserStatus
import com.example.lingora_fe.admin.user.presentation.UserManagementEvent
import com.example.lingora_fe.admin.user.presentation.UserManagementViewModel

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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Username
            OutlinedTextField(
                value = formState.username,
                onValueChange = { 
                    viewModel.updateFormState(formState.copy(username = it))
                },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, "Username") },
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
                leadingIcon = { Icon(Icons.Default.Email, "Email") },
                isError = formState.emailError != null,
                supportingText = formState.emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Password Section
            if (!isEditMode || formState.password.isNotBlank()) {
                Text(
                    text = if (isEditMode) "Change Password (Optional)" else "Password",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = formState.password,
                    onValueChange = { 
                        viewModel.updateFormState(formState.copy(password = it))
                    },
                    label = { Text(if (isEditMode) "New Password" else "Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, "Password") },
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
                    leadingIcon = { Icon(Icons.Default.Lock, "Confirm Password") },
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProficiencyLevel.values().forEach { level ->
                    FilterChip(
                        selected = formState.selectedProficiency == level,
                        onClick = { 
                            viewModel.updateFormState(formState.copy(selectedProficiency = level))
                        },
                        label = { Text(level.value) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Roles
            Text(
                text = "User Roles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            UserRoleType.values().forEach { roleType ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        }
                    )
                    Column {
                        Text(
                            text = roleType.value,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = when (roleType) {
                                UserRoleType.ADMIN -> "Full access to all features"
                                UserRoleType.LEARNER -> "Standard user access"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Status (only for edit mode)
            if (isEditMode) {
                Text(
                    text = "Account Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserStatus.values().forEach { status ->
                        if (status != UserStatus.DELETED) {
                            FilterChip(
                                selected = formState.selectedStatus == status,
                                onClick = { 
                                    viewModel.updateFormState(formState.copy(selectedStatus = status))
                                },
                                label = { Text(status.value) },
                                modifier = Modifier.weight(1f)
                            )
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
                    modifier = Modifier.weight(1f),
                    enabled = formState.isValid && !state.isCreating && !state.isUpdating
                ) {
                    if (state.isCreating || state.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEditMode) "Update User" else "Create User")
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

