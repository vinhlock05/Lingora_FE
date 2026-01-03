package com.example.lingora_fe.user.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val changePasswordState by viewModel.changePasswordState.collectAsState()
    
    // Form state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Visibility toggles
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Validation
    val hasPassword = viewModel.profileState.collectAsState().value.user?.hasPassword == true
    val isNewPasswordValid = newPassword.length >= 6
    val isConfirmPasswordValid = newPassword == confirmPassword
    // If user has password, current password is required. Otherwise (Set Password), it's not.
    val canSave = (!hasPassword || currentPassword.isNotBlank()) && 
                  newPassword.isNotBlank() && 
                  confirmPassword.isNotBlank() &&
                  isNewPasswordValid && 
                  isConfirmPasswordValid
    
    // Show success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(changePasswordState.updateSuccess) {
        if (changePasswordState.updateSuccess) {
            showSuccessDialog = true
        }
    }
    
    // Clear state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearChangePasswordState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (hasPassword) "Đổi mật khẩu" else "Đặt mật khẩu",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .imePadding()
            ) {
                // Password Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = if (hasPassword) "Thay đổi mật khẩu" else "Đặt mật khẩu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Mật khẩu mới phải có ít nhất 6 ký tự",
                            fontSize = 14.sp,
                            color = NavBarText,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        // Current password field - Only show if user has password
                        if (hasPassword) {
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Mật khẩu hiện tại") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = GradientStart
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showCurrentPassword) "Show password" else "Hide password",
                                            tint = NavBarText
                                        )
                                    }
                                },
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GradientStart,
                                    focusedLabelColor = GradientStart
                                ),
                                singleLine = true
                            )
                        }
                        
                        // New password field
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Mật khẩu mới") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GradientStart
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showNewPassword) "Show password" else "Hide password",
                                        tint = NavBarText
                                    )
                                }
                            },
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                focusedLabelColor = GradientStart,
                                errorBorderColor = Color.Red
                            ),
                            isError = newPassword.isNotBlank() && !isNewPasswordValid,
                            supportingText = if (newPassword.isNotBlank() && !isNewPasswordValid) {
                                { Text("Mật khẩu phải có ít nhất 6 ký tự", color = Color.Red) }
                            } else null,
                            singleLine = true
                        )
                        
                        // Confirm password field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu mới") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GradientStart
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword) "Show password" else "Hide password",
                                        tint = NavBarText
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                focusedLabelColor = GradientStart,
                                errorBorderColor = Color.Red
                            ),
                            isError = confirmPassword.isNotBlank() && !isConfirmPasswordValid,
                            supportingText = if (confirmPassword.isNotBlank() && !isConfirmPasswordValid) {
                                { Text("Mật khẩu xác nhận không khớp", color = Color.Red) }
                            } else null,
                            singleLine = true
                        )
                    }
                }
                
                // Error message
                changePasswordState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Save button
                Button(
                    onClick = {
                        viewModel.changePassword(
                            oldPassword = currentPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                // Success is handled by LaunchedEffect
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canSave && !changePasswordState.isUpdating,
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (changePasswordState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasPassword) "Đổi mật khẩu" else "Đặt mật khẩu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = if (hasPassword) "Đổi mật khẩu" else "Đặt mật khẩu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            title = {
                Text(
                    "Thành công",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Mật khẩu đã được thay đổi thành công!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
