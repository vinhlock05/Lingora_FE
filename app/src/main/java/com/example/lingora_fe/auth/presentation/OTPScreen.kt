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
import kotlinx.coroutines.delay

@Composable
fun OTPScreen(
    navController: NavController,
    email: String = "test004@gmail.com",
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    var otpValue by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(41) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptCount by remember { mutableStateOf(0) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }
    
    LaunchedEffect(authState.error) {
        authState.error?.let {
            errorMessage = it
            attemptCount++
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
            // Box to contain Card with overlapping Logo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Card Container with Shadow
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
                        // Title
                        Text(
                            text = "Xác minh Email",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ArimoFontFamily,
                            color = GradientStart,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Email Display in Card
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
                                    fontSize = 12.sp,
                                    fontFamily = ArimoFontFamily,
                                    color = NavBarText,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = email,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ArimoFontFamily,
                                    color = MainText
                                )
                            }
                        }

                        // OTP Input Label
                        Text(
                            text = "Nhập mã xác minh",
                            fontSize = 14.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // OTP Input Box
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

                        // Error Message or Countdown
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

                        // Resend OTP Button
                        TextButton(
                            onClick = { 
                                if (countdown == 0) {
                                    countdown = 41
                                    errorMessage = null
                                    viewModel.resendOTP(email)
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

                        // Verify Button with Gradient
                        Button(
                            onClick = {
                                if (otpValue.length == 6) {
                                    viewModel.verifyOTP(email, otpValue)
                                    // Navigate on success
                                    navController.navigate(Route.UserNavigation.route) {
                                        popUpTo(Route.AuthNavigation.route) { inclusive = true }
                                    }
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
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Note
                        Text(
                            text = "Kiểm tra hộp thư spam nếu bạn không thấy email",
                            fontSize = 11.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Logo overlapping the card (half outside, half inside)
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
