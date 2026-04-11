package com.example.lingora_fe.user.classroom.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuizSessionScreen(
    navController: NavController,
    viewModel: QuizSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Handle back press to warn about leaving
    var showExitWarning by remember { mutableStateOf(false) }
    BackHandler(enabled = !state.isFinished) {
        showExitWarning = true
    }

    Scaffold(
        topBar = {
            if (!state.isFinished) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Câu hỏi ${state.currentQuestionIndex + 1} / ${state.totalQuestions}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinearProgressIndicator(
                                progress = if (state.totalQuestions > 0) 
                                    (state.currentQuestionIndex + 1).toFloat() / state.totalQuestions 
                                    else 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, end = 16.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF5CB85C),
                                trackColor = Color(0xFFF0F0F0)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showExitWarning = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Thoát")
                        }
                    },
                    actions = {
                        TimerView(seconds = state.timeLeftSeconds)
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.isFinished -> {
                    QuizResultView(
                        score = state.score,
                        total = state.totalQuestions,
                        isPassing = state.isPassing,
                        onBack = { navController.popBackStack() }
                    )
                }
                state.quiz != null -> {
                    val currentQuestion = state.quiz!!.questions?.getOrNull(state.currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuestionContent(
                            question = currentQuestion,
                            selectedChoice = state.userChoices[currentQuestion.id],
                            onChoiceSelect = { viewModel.selectOption(currentQuestion.id, it) },
                            onNext = { viewModel.nextQuestion() },
                            onPrevious = { viewModel.previousQuestion() },
                            onFinish = { viewModel.finishQuiz() },
                            isLast = state.currentQuestionIndex == state.totalQuestions - 1,
                            isFirst = state.currentQuestionIndex == 0
                        )
                    }
                }
            }
        }
    }

    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            title = { Text("Bạn muốn thoát?") },
            text = { Text("Tiến trình làm bài của bạn sẽ không được lưu. Bạn có chắc chắn muốn thoát?") },
            confirmButton = {
                TextButton(onClick = { navController.popBackStack() }) { 
                    Text("Thoát", color = Color.Red) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarning = false }) { Text("Tiếp tục làm bài") }
            }
        )
    }
}

@Composable
fun TimerView(seconds: Int) {
    val minutes = seconds / 60
    val secs = seconds % 60
    val isUrgent = seconds < 60
    
    Row(
        modifier = Modifier
            .padding(end = 16.dp)
            .background(
                if (isUrgent) Color(0xFFFFEBEE) else Color(0xFFF5F7FA),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isUrgent) Color.Red else Color.Gray
        )
        Text(
            text = String.format("%02d:%02d", minutes, secs),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isUrgent) Color.Red else Color.Black
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun QuestionContent(
    question: ClassroomQuizQuestion,
    selectedChoice: String?,
    onChoiceSelect: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onFinish: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    question.options.forEach { option ->
                        val isSelected = selectedChoice == option
                        ChoiceCard(
                            text = option,
                            isSelected = isSelected,
                            onSelect = { onChoiceSelect(option) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isFirst) {
                TextButton(
                    onClick = onPrevious,
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Quay lại", fontSize = 16.sp)
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            if (isLast) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB85C))
                ) {
                    Text("Nộp bài", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CB85C)),
                    enabled = selectedChoice != null
                ) {
                    Text("Câu tiếp theo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChoiceCard(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF5CB85C) else Color(0xFFF0F0F0),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFE8F5E9) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) Color(0xFF2E7D32) else MainText,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF5CB85C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuizResultView(score: Int, total: Int, isPassing: Boolean, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = if (isPassing) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPassing) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (isPassing) Color(0xFF5CB85C) else Color.Red
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isPassing) "Chúc mừng!" else "Chưa đạt",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPassing) Color(0xFF2E7D32) else Color.Red
        )
        
        Text(
            text = if (isPassing) "Bạn đã vượt qua bài kiểm tra này" else "Hãy ôn tập lại và thử lại nhé",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ĐIỂM CỦA BẠN", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = "$score / $total",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Quay lại lớp học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
