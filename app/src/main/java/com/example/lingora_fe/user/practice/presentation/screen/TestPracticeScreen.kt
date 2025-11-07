package com.example.lingora_fe.user.practice.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPracticeScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện đề thi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // IELTS Practice Test
            TestCard(
                title = "IELTS Practice Test 1",
                subtitle = "Đề thi IELTS mẫu với đầy đủ 4 kỹ năng",
                level = "Intermediate",
                duration = "2h 45m",
                questions = "9",
                badge = "Mới",
                skills = listOf("Listening", "Reading", "Writing"),
                onClick = {
                    navController.navigate("practice/test/1")
                }
            )

            // TOEIC Reading & Listening
            TestCard(
                title = "TOEIC Reading & Listening",
                subtitle = "Đề thi TOEIC phần nghe và đọc",
                level = "All levels",
                duration = "2h 00m",
                questions = "950",
                badge = "Mới",
                skills = listOf("Listening", "Reading"),
                onClick = {
                    navController.navigate("practice/test/2")
                }
            )

            // Business English Test
            TestCard(
                title = "Business English Test",
                subtitle = "Kiểm tra tiếng Anh thương mại",
                level = "Advanced",
                duration = "1h 30m",
                questions = "100",
                badge = "Business",
                skills = listOf("Reading", "Writing", "Speaking"),
                onClick = {
                    navController.navigate("practice/test/3")
                }
            )
        }
    }
}

@Composable
fun TestCard(
    title: String,
    subtitle: String,
    level: String,
    duration: String,
    questions: String,
    badge: String,
    skills: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainText,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                }
            }

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = NavBarText,
                lineHeight = 20.sp
            )

            // Test Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TestInfoChip(
                    label = "Trình độ",
                    value = level,
                    color = Color(0xFFDCECFE)
                )
                TestInfoChip(
                    label = "Thời gian",
                    value = duration,
                    color = Color(0xFFF3E8FF)
                )
                TestInfoChip(
                    label = "Điểm tối đa",
                    value = questions,
                    color = Color(0xFFFEF3C7)
                )
            }

            // Skills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                skills.forEach { skill ->
                    Surface(
                        modifier = Modifier.border(width = 1.dp, color = Color(0x1A000000), shape = RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = skill,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }
                }
            }

            // Start Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Bắt đầu làm bài",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TestInfoChip(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(
                color = color,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = NavBarText
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MainText
        )
    }
}

