package com.example.lingora_fe.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.R
import com.example.lingora_fe.core.ui.theme.*
import com.example.lingora_fe.navigation.Route

@Composable
fun ProficiencySelectionScreen(
    navController: NavController,
    viewModel: ProficiencySelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedProficiency by remember { mutableStateOf<String?>(null) }

    // Navigate after successful update
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            // Navigate based on user role
            val destination = if (state.isAdmin) {
                Route.AdminNavigation.route
            } else {
                Route.UserNavigation.route
            }
            navController.navigate(destination) {
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
                            text = "Chọn trình độ của bạn",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ArimoFontFamily,
                            color = GradientStart,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Giúp chúng tôi cá nhân hóa trải nghiệm học tập cho bạn",
                            fontSize = 14.sp,
                            fontFamily = ArimoFontFamily,
                            color = NavBarText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Proficiency Options
                        ProficiencyOption(
                            title = "Người mới bắt đầu",
                            description = "Bạn mới bắt đầu học ngôn ngữ",
                            proficiency = "BEGINNER",
                            isSelected = selectedProficiency == "BEGINNER",
                            onClick = { selectedProficiency = "BEGINNER" },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ProficiencyOption(
                            title = "Trung cấp",
                            description = "Bạn đã có kiến thức cơ bản",
                            proficiency = "INTERMEDIATE",
                            isSelected = selectedProficiency == "INTERMEDIATE",
                            onClick = { selectedProficiency = "INTERMEDIATE" },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ProficiencyOption(
                            title = "Nâng cao",
                            description = "Bạn đã thành thạo và muốn nâng cao kỹ năng",
                            proficiency = "ADVANCED",
                            isSelected = selectedProficiency == "ADVANCED",
                            onClick = { selectedProficiency = "ADVANCED" },
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Error Message
                        if (state.error != null) {
                            Text(
                                text = state.error ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = ArimoFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Confirm Button
                        Button(
                            onClick = {
                                selectedProficiency?.let {
                                    viewModel.updateProficiency(it)
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
                            enabled = selectedProficiency != null && !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Xác nhận",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ArimoFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                // Logo overlapping the card
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

@Composable
fun ProficiencyOption(
    title: String,
    description: String,
    proficiency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            GradientStart.copy(alpha = 0.1f)
        } else {
            Color.White
        },
        border = if (isSelected) {
            BorderStroke(2.dp, GradientStart)
        } else {
            BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ArimoFontFamily,
                    color = if (isSelected) GradientStart else MainText
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    fontFamily = ArimoFontFamily,
                    color = NavBarText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GradientStart,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

