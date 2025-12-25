package com.example.lingora_fe.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.R
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.navigation.Route

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    // Navigate to OTP screen after email is sent successfully
    LaunchedEffect(authState.passwordResetEmail, authState.isLoading, authState.error) {
        if (authState.passwordResetEmail != null && 
            !authState.isLoading && 
            authState.error == null) {
            navController.navigate(Route.forgotPasswordOtp(authState.passwordResetEmail!!)) {
                popUpTo(Route.ForgotPassword.route) { inclusive = false }
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
                            text = "Quên mật khẩu",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ArimoFontFamily,
                            color = GradientStart,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Nhập email của bạn để nhận mã xác thực",
                            fontSize = 14.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = null
                                viewModel.clearError()
                            },
                            label = { Text("Email", fontFamily = ArimoFontFamily) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = GradientStart
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorBorderColor = Color.Red
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = emailError != null || authState.error != null
                        )
                        
                        // Error Message
                        if (emailError != null || authState.error != null) {
                            Text(
                                text = emailError ?: authState.error ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Send Code Button
                        Button(
                            onClick = {
                                when {
                                    email.isEmpty() -> emailError = "Vui lòng nhập email"
                                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                                        emailError = "Email không hợp lệ"
                                    else -> viewModel.sendPasswordResetEmail(email)
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
                                    text = "Gửi mã xác thực",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ArimoFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Back to Login
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text(
                                text = "Quay lại đăng nhập",
                                color = GradientStart,
                                fontSize = 14.sp,
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Medium
                            )
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
