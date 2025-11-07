package com.example.lingora_fe.user.practice.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.components.FocusComponent
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import kotlinx.coroutines.delay

data class WritingTask(
    val number: Int,
    val title: String,
    val description: String,
    val requirement: String,
    val minWords: Int,
    val timeMinutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingPracticeScreen(
    navController: NavController
) {
    // Sample tasks
    val tasks = remember {
        listOf(
            WritingTask(
                number = 1,
                title = "Describe a Chart",
                description = "The chart below shows the percentage of households in different income brackets in a country from 2010 to 2020.",
                requirement = "Summarize the information by selecting and reporting the main features, and make comparisons where relevant.",
                minWords = 150,
                timeMinutes = 20
            ),
            WritingTask(
                number = 2,
                title = "Essay Writing",
                description = "Some people think that the best way to improve public health is by increasing the number of sports facilities. Others believe there are better ways to improve it.",
                requirement = "Discuss both views and give your own opinion. Provide relevant examples from your own knowledge or experience.",
                minWords = 250,
                timeMinutes = 40
            )
        )
    }

    var currentTaskIndex by remember { mutableStateOf(0) }
    var essayText by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    
    val currentTask = tasks[currentTaskIndex]
    val wordCount = if (essayText.trim().isEmpty()) 0 else essayText.trim().split("\\s+".toRegex()).size
    
    // Timer
    var timeRemaining by remember { mutableStateOf(currentTask.timeMinutes * 60) }
    
    LaunchedEffect(currentTaskIndex) {
        timeRemaining = tasks[currentTaskIndex].timeMinutes * 60
    }
    
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
    }
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    
    // Back handler
    BackHandler {
        showExitDialog = true
    }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi bài tập?") },
            text = { Text("Bài viết của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Thoát", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục viết")
                }
            }
        )
    }

    FocusComponent {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Practice - Writing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MainText
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = timerText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C)
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
            // Task indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Task ${currentTask.number}: ${currentTask.title}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7E22CE)
                    )
                }

                Text(
                    text = "Task ${currentTaskIndex + 1}/${tasks.size}",
                    fontSize = 14.sp,
                    color = NavBarText
                )
            }

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = (currentTaskIndex + 1).toFloat() / tasks.size,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(end = 8.dp),
                    color = GradientStart,
                    trackColor = Color(0xFFE5E7EB)
                )
                Text(
                    text = "${((currentTaskIndex + 1).toFloat() / tasks.size * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NavBarText
                )
            }

            // Task Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FAFB)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Task ${currentTask.number}: ${currentTask.title}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MainText
                        )
                    }

                    Surface(
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${currentTask.timeMinutes} phút • Tối thiểu ${currentTask.minWords} từ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7E22CE)
                        )
                    }

                    Divider(color = Color(0xFFE5E7EB))

                    Text(
                        text = currentTask.description,
                        fontSize = 14.sp,
                        color = MainText,
                        lineHeight = 20.sp
                    )

                    Text(
                        text = currentTask.requirement,
                        fontSize = 14.sp,
                        color = MainText,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Write at least ${currentTask.minWords} words.",
                        fontSize = 14.sp,
                        color = Color(0xFFEA580C),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Instructions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hãy viết rõ ràng, so sánh các điểm dữ liệu và viết ít nhất 150 từ. Kiểm tra chính tả và ngữ pháp trước khi nộp bài.",
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Writing Area
            Text(
                text = "Viết bài của bạn",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MainText
            )

            OutlinedTextField(
                value = essayText,
                onValueChange = { essayText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = {
                    Text(
                        text = "Xem gợi ý viết bài",
                        color = NavBarText
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số từ đã viết",
                    fontSize = 14.sp,
                    color = NavBarText
                )
                Text(
                    text = "${wordCount}/${currentTask.minWords}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (wordCount >= currentTask.minWords) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            // Tip card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📝",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Bạn cần viết thêm ${if (wordCount < currentTask.minWords) currentTask.minWords - wordCount else 0} từ để đạt yêu cầu tối thiểu. Kiểm tra chính tả và ngữ pháp trước khi nộp bài.",
                        fontSize = 13.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentTaskIndex > 0) {
                            currentTaskIndex--
                            essayText = ""
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavBarText
                    ),
                    enabled = currentTaskIndex > 0
                ) {
                    Text(
                        text = "Task trước",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (currentTaskIndex < tasks.size - 1) {
                            currentTaskIndex++
                            essayText = ""
                        } else {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    )
                ) {
                    Text(
                        text = if (currentTaskIndex < tasks.size - 1) "Task tiếp" else "Hoàn thành",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
    }
}

