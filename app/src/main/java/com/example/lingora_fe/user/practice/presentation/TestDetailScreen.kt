package com.example.lingora_fe.user.practice.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun TestDetailScreen(
    navController: NavController,
    testId: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IELTS Practice Test 1",
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
            // Test Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoBox(
                            label = "Trình độ",
                            value = "Intermediate",
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            label = "Thời gian",
                            value = "2h 45m",
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            label = "Điểm tối đa",
                            value = "9",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Skills Section
            Text(
                text = "Chọn kỹ năng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            // Listening
            SkillCard(
                icon = Icons.Default.Headset,
                iconColor = Color(0xFF3B82F6),
                iconBackgroundColor = Color(0xFFDCECFE),
                title = "Listening",
                subtitle = "Nghe và trả lời câu hỏi",
                questions = "40 câu",
                duration = "30 phút",
                isCompleted = false,
                onClick = {
                    navController.navigate("practice/test/1/listening")
                }
            )

            // Reading
            SkillCard(
                icon = Icons.Default.MenuBook,
                iconColor = Color(0xFF10B981),
                iconBackgroundColor = Color(0xFFD1FAE5),
                title = "Reading",
                subtitle = "Đọc hiểu và trả lời câu hỏi",
                questions = "40 câu",
                duration = "60 phút",
                isCompleted = true,
                onClick = {
                    navController.navigate("practice/test/1/reading")
                }
            )

            // Writing
            SkillCard(
                icon = Icons.Default.Edit,
                iconColor = Color(0xFF9333EA),
                iconBackgroundColor = Color(0xFFF3E8FF),
                title = "Writing",
                subtitle = "Viết bài luận và mô tả",
                questions = "2 câu",
                duration = "60 phút",
                isCompleted = false,
                onClick = {
                    navController.navigate("practice/test/1/writing")
                }
            )
        }
    }
}

@Composable
fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = NavBarText
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MainText
        )
    }
}

@Composable
fun SkillCard(
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    questions: String,
    duration: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📝 $questions",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "⏱ $duration",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

