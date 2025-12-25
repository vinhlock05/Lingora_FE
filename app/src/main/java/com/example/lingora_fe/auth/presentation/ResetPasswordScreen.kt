package com.example.lingora_fe.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.R
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.navigation.Route

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    resetToken: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Navigate to login after successful password reset
    LaunchedEffect(authState.resetToken, authState.isLoading, authState.error) {
        if (authState.resetToken == null && 
            !authState.isLoading && 
            authState.error == null &&
            newPassword.isNotEmpty()) {
            // Password reset successful, navigate to login
            navController.navigate(Route.AuthScreen.route) {
                popUpTo(Route.AuthNavigation.route) { inclusive = true }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 40.dp)
                        .shadow(
                            elevation = 15.dp,
                            spotColor = Color(0x1A000000),
                            ambientColor = Color(0x1A000000),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xCCFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đặt lại mật khẩu",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ArimoFontFamily,
                            color = GradientStart,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Nhập mật khẩu mới của bạn",
                            fontSize = 14.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        // New Password Input
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                passwordError = null
                                viewModel.clearError()
                            },
                            label = { Text("Mật khẩu mới", fontFamily = ArimoFontFamily) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorBorderColor = Color.Red
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = passwordError != null || authState.error != null
                        )
                        
                        // Confirm Password Input
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                passwordError = null
                                viewModel.clearError()
                            },
                            label = { Text("Xác nhận mật khẩu", fontFamily = ArimoFontFamily) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorBorderColor = Color.Red
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = passwordError != null || authState.error != null
                        )
                        
                        // Error Messages
                        if (passwordError != null || authState.error != null) {
                            Text(
                                text = passwordError ?: authState.error ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Password validation hint
                        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text(
                                text = "Mật khẩu không khớp",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Reset Password Button
                        Button(
                            onClick = {
                                when {
                                    newPassword.isEmpty() -> passwordError = "Vui lòng nhập mật khẩu mới"
                                    newPassword.length < 6 -> passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
                                    confirmPassword.isEmpty() -> passwordError = "Vui lòng xác nhận mật khẩu"
                                    newPassword != confirmPassword -> passwordError = "Mật khẩu không khớp"
                                    else -> viewModel.confirmPasswordReset(resetToken, newPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !authState.isLoading
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Đặt lại mật khẩu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ArimoFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Lingora Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
