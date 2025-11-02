package com.example.lingora_fe.user.practice.presentation

import android.content.Context
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import kotlinx.coroutines.delay

data class ListeningQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val audioResId: Int? = null // For future audio resource
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // Sample questions
    val questions = remember {
        listOf(
            ListeningQuestion(
                id = 1,
                question = "What is the woman looking for?",
                options = listOf("Her keys", "Her phone", "Her wallet", "Her bag"),
                correctAnswer = 2
            ),
            ListeningQuestion(
                id = 2,
                question = "Where did they agree to meet?",
                options = listOf("At the park", "At the cafe", "At the library", "At home"),
                correctAnswer = 1
            ),
            ListeningQuestion(
                id = 3,
                question = "What time is the appointment?",
                options = listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM"),
                correctAnswer = 1
            ),
            ListeningQuestion(
                id = 4,
                question = "How does the man feel?",
                options = listOf("Happy", "Angry", "Worried", "Excited"),
                correctAnswer = 2
            ),
            ListeningQuestion(
                id = 5,
                question = "What will they do next?",
                options = listOf("Go shopping", "Have dinner", "Watch a movie", "Go home"),
                correctAnswer = 1
            )
        )
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var playsRemaining by remember { mutableStateOf(2) }
    
    val currentQuestion = questions[currentQuestionIndex]
    
    // Audio simulation - simulate 30 seconds audio
    var audioProgress by remember { mutableStateOf(0f) }
    var audioCurrentTime by remember { mutableStateOf(0) }
    val audioTotalTime = 30 // 30 seconds
    
    // Timer (30 minutes total)
    var timeRemaining by remember { mutableStateOf(30 * 60) }
    
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
    }
    
    // Audio playback simulation
    LaunchedEffect(isPlaying) {
        if (isPlaying && audioCurrentTime < audioTotalTime) {
            delay(1000L)
            audioCurrentTime++
            audioProgress = audioCurrentTime.toFloat() / audioTotalTime
            if (audioCurrentTime >= audioTotalTime) {
                isPlaying = false
                audioCurrentTime = 0
                audioProgress = 0f
            }
        }
    }
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    
    val audioMinutes = audioCurrentTime / 60
    val audioSeconds = audioCurrentTime % 60
    val currentTimeText = String.format("%02d:%02d", audioMinutes, audioSeconds)
    
    val totalMinutes = audioTotalTime / 60
    val totalSeconds = audioTotalTime % 60
    val totalTimeText = String.format("%02d:%02d", totalMinutes, totalSeconds)
    
    // Back handler
    BackHandler {
        showExitDialog = true
    }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi bài tập?") },
            text = { Text("Tiến trình làm bài của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
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
                    Text("Tiếp tục làm bài")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Listening",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
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
            // Part indicator
            Surface(
                color = Color(0xFFDCECFE),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Part 1: Conversation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E40AF)
                    )
                }
            }

            // Question info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu ${currentQuestionIndex + 1}/${questions.size}",
                    fontSize = 14.sp,
                    color = NavBarText
                )
            }

            // Audio Player Card
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Play button
                    IconButton(
                        onClick = {
                            if (!isPlaying && playsRemaining > 0 && audioCurrentTime == 0) {
                                playsRemaining--
                            }
                            isPlaying = !isPlaying
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = if (playsRemaining > 0 || isPlaying) Color(0xFF3B82F6) else Color(0xFF9CA3AF),
                                shape = CircleShape
                            ),
                        enabled = playsRemaining > 0 || isPlaying
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Audio Recording",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )

                    Text(
                        text = if (playsRemaining > 0) "Còn $playsRemaining lần nghe" else "Hết lượt nghe",
                        fontSize = 13.sp,
                        color = if (playsRemaining > 0) NavBarText else Color(0xFFEF4444)
                    )

                    // Progress bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = audioProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF3B82F6),
                            trackColor = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentTimeText,
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                            Text(
                                text = totalTimeText,
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                        }
                    }
                }
            }

            // Question
            Text(
                text = currentQuestion.question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            // Answer options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentQuestion.options.forEachIndexed { index, answer ->
                    ListeningAnswerOption(
                        text = answer,
                        isSelected = selectedAnswer == index,
                        onClick = {
                            selectedAnswer = index
                        }
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
                        if (currentQuestionIndex > 0) {
                            currentQuestionIndex--
                            selectedAnswer = null
                            isPlaying = false
                            audioCurrentTime = 0
                            audioProgress = 0f
                            playsRemaining = 2
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavBarText
                    ),
                    enabled = currentQuestionIndex > 0
                ) {
                    Text(
                        text = "Câu trước",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null
                            isPlaying = false
                            audioCurrentTime = 0
                            audioProgress = 0f
                            playsRemaining = 2
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
                        text = if (currentQuestionIndex < questions.size - 1) "Câu tiếp" else "Hoàn thành",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ListeningAnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFDCECFE) else Color.White,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF3B82F6)
                )
            )
            Text(
                text = text,
                fontSize = 15.sp,
                color = MainText,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

