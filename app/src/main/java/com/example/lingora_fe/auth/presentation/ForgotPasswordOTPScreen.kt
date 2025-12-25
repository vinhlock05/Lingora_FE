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
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordOTPScreen(
    navController: NavController,
    email: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    var otpValue by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }
    
    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            errorMessage = authState.error
            attemptCount++
        } else {
            errorMessage = null
        }
    }
    
    // Navigate to reset password screen after successful OTP verification
    LaunchedEffect(authState.resetToken, authState.isLoading, authState.error) {
        if (authState.resetToken != null && 
            !authState.isLoading && 
            authState.error == null) {
            navController.navigate(Route.resetPassword(authState.resetToken!!)) {
                popUpTo(Route.ForgotPassword.route) { inclusive = true }
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
                            text = "Xác minh OTP",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ArimoFontFamily,
                            color = GradientStart,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Email Display
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(bottom = 8.dp),
                                    tint = GradientStart
                                )
                                
                                Text(
                                    text = "Chúng tôi đã gửi mã xác minh 6 số đến",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = email,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ArimoFontFamily,
                                    color = MainText
                                )
                            }
                        }
                        
                        Text(
                            text = "Nhập mã xác minh",
                            fontSize = 14.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // OTP Input
                        OutlinedTextField(
                            value = otpValue,
                            onValueChange = { 
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    otpValue = it
                                    errorMessage = null
                                    viewModel.clearError()
                                }
                            },
                            modifier = Modifier
                                .width(220.dp)
                                .padding(bottom = 16.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = ArimoFontFamily,
                                textAlign = TextAlign.Center,
                                letterSpacing = 8.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GradientStart,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorBorderColor = Color.Red
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = errorMessage != null,
                            shape = RoundedCornerShape(12.dp),
                            placeholder = {
                                Text(
                                    text = "• • • • • •",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                            }
                        )
                        
                        // Error or Countdown
                        if (errorMessage != null) {
                            Text(
                                text = "Mã OTP không chính xác. Còn lại ${3 - attemptCount} lần thử!",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else {
                            Text(
                                text = "Gửi lại mã sau  0:${countdown.toString().padStart(2, '0')}",
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                color = NavBarText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Resend Button
                        TextButton(
                            onClick = { 
                                if (countdown == 0) {
                                    countdown = 60
                                    errorMessage = null
                                    viewModel.sendPasswordResetEmail(email)
                                }
                            },
                            enabled = countdown == 0 && !authState.isLoading
                        ) {
                            Text(
                                text = "Gửi lại mã OTP",
                                color = if (countdown == 0) GradientStart else Color.Gray,
                                fontSize = 14.sp,
                                fontFamily = ArimoFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Verify Button
                        Button(
                            onClick = {
                                if (otpValue.length == 6) {
                                    viewModel.verifyPasswordResetOtp(email, otpValue)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .graphicsLayer {
                                    alpha = if (otpValue.length == 6 && !authState.isLoading) 1f else 0.5f
                                }
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.White.copy(alpha = 0.7f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = otpValue.length == 6 && !authState.isLoading
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Xác minh",
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
