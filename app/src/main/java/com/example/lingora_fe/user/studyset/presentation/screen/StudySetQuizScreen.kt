package com.example.lingora_fe.user.studyset.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.domain.model.QuizType
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetQuizViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetQuizScreen(
    studySetId: Int,
    onBackClick: () -> Unit,
    viewModel: StudySetQuizViewModel = hiltViewModel()
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val studySet = uiState.studySet
        val quizzes = studySet?.quizzes ?: emptyList()

    if (quizzes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Không có câu hỏi quiz nào")
                Button(onClick = onBackClick) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    val currentQuiz = quizzes.getOrNull(uiState.currentIndex) ?: return
    val selectedAnswer = uiState.selectedAnswers[uiState.currentIndex]
    val isChecked = uiState.checkedAnswers.contains(uiState.currentIndex)
    val isCorrect = isChecked && selectedAnswer == currentQuiz.correctAnswer
    val isLastQuestion = uiState.currentIndex == quizzes.size - 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${studySet?.title ?: "Quiz"} - Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.06f),
                            GradientEnd.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Câu ${uiState.currentIndex + 1}/${quizzes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavBarText
                    )
                    Text(
                        text = "${uiState.checkedAnswers.size}/${quizzes.size} đã làm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavBarText
                    )
                }

                // Question
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3F4F6)
                    )
                ) {
                    Text(
                        text = currentQuiz.question,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                }

                // Answer options
                when (currentQuiz.type) {
                    QuizType.MULTIPLE_CHOICE -> {
                        currentQuiz.options.forEachIndexed { index, option ->
                            val isSelected = selectedAnswer == option
                            val isCorrectAnswer = isChecked && option == currentQuiz.correctAnswer
                            val isWrongAnswer = isChecked && isSelected && option != currentQuiz.correctAnswer

                            AnswerOption(
                                text = option,
                                isSelected = isSelected,
                                isCorrect = isCorrectAnswer,
                                isWrong = isWrongAnswer,
                                onClick = {
                                    if (!isChecked) {
                                        viewModel.selectAnswer(uiState.currentIndex, option)
                                    }
                                },
                                isChecked = isChecked
                            )
                        }
                    }
                    QuizType.TRUE_FALSE -> {
                        listOf("Đúng", "Sai").forEach { option ->
                            val isSelected = selectedAnswer == option
                            val isCorrectAnswer = isChecked && option == currentQuiz.correctAnswer
                            val isWrongAnswer = isChecked && isSelected && option != currentQuiz.correctAnswer

                            AnswerOption(
                                text = option,
                                isSelected = isSelected,
                                isCorrect = isCorrectAnswer,
                                isWrong = isWrongAnswer,
                                onClick = {
                                    if (!isChecked) {
                                        viewModel.selectAnswer(uiState.currentIndex, option)
                                    }
                                },
                                isChecked = isChecked
                            )
                        }
                    }
                    QuizType.SHORT_ANSWER -> {
                        OutlinedTextField(
                            value = selectedAnswer ?: "",
                            onValueChange = {
                                if (!isChecked) {
                                    viewModel.selectAnswer(uiState.currentIndex, it)
                                }
                            },
                            label = { Text("Nhập đáp án") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChecked,
                            readOnly = isChecked
                        )
                        if (isChecked) {
                            Text(
                                text = "Đáp án đúng: ${currentQuiz.correctAnswer}",
                                color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Check button (only show if answer selected and not checked yet)
                if (selectedAnswer != null && !isChecked) {
                    Button(
                        onClick = { viewModel.checkAnswer(uiState.currentIndex) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kiểm tra")
                    }
                }
            }

            // Bottom Feedback Card with Next Button
            if (uiState.showFeedback && isChecked) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeedbackCard(
                        isCorrect = isCorrect,
                        correctAnswer = currentQuiz.correctAnswer,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isLastQuestion) "Xem kết quả" else "Câu tiếp theo")
                    }
                }
            }
        }
    }

    // Results dialog (only show at the end)
    if (uiState.showResults && uiState.currentIndex == quizzes.size - 1) {
        QuizResultDialog(
            correctCount = uiState.correctCount,
            totalCount = quizzes.size,
            onRetake = {
                viewModel.resetQuiz()
            },
            onBack = onBackClick
        )
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
    isChecked: Boolean
) {
    val backgroundColor = when {
        isCorrect -> Color(0xFFDCFCE7)
        isWrong -> Color(0xFFFEE2E2)
        isSelected -> Color(0xFFE0F2FE)
        else -> Color.White
    }

    val borderColor = when {
        isCorrect -> Color(0xFF10B981)
        isWrong -> Color(0xFFEF4444)
        isSelected -> GradientStart
        else -> Color(0xFFE5E7EB)
    }

    val borderWidth = if (isSelected || isCorrect || isWrong) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isChecked) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected || isCorrect || isWrong) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MainText,
                modifier = Modifier.weight(1f)
            )
            if (isChecked) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    isCorrect: Boolean,
    correctAnswer: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCorrect) "Chính xác!" else "Sai rồi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                if (!isCorrect) {
                    Text(
                        text = "Đáp án đúng: $correctAnswer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavBarText
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultDialog(
    correctCount: Int,
    totalCount: Int,
    onRetake: () -> Unit,
    onBack: () -> Unit
) {
    val percentage = (correctCount * 100) / totalCount
    val feedback = when {
        percentage >= 80 -> "Xuất sắc!"
        percentage >= 60 -> "Tốt"
        percentage >= 40 -> "Cần cải thiện"
        else -> "Cần cải thiện"
    }

    Dialog(onDismissRequest = onBack) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Kết quả Quiz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Bạn đã trả lời đúng $correctCount/$totalCount câu hỏi",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRetake,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Làm lại Quiz")
                    }
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Quay về học liệu")
                    }
                }
            }
        }
    }
}
