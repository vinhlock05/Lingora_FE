package com.example.lingora_fe.user.adaptivetest.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.core.domain.model.ProficiencyLevel
import com.example.lingora_fe.user.adaptivetest.domain.model.PublicAdaptiveQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTestScreen(
    navController: NavController,
    viewModel: AdaptiveTestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kiểm tra trình độ đầu vào",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
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
                .padding(paddingValues)
                .background(Color.White)
        ) {
            val currentQuestion = state.currentQuestion
            val error = state.error
            val lastAnsweredQuestion = state.lastAnsweredQuestion
            val showResult = state.showResult
            
            when {
                state.isLoading && currentQuestion == null && !showResult -> {
                    // Initial loading
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.isCompleted -> {
                    // Test completed screen - show result and wait for user to continue
                    TestCompletedContent(
                        finalProficiency = state.finalProficiency,
                        answeredCount = state.answeredCount,
                        onContinue = {
                            // Navigate to UserNavigation when user clicks continue
                            // Backend should have updated proficiency by now
                            navController.navigate(Route.UserNavigation.route) {
                                // Clear back stack including AdaptiveTest
                                popUpTo(Route.AdaptiveTest.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                showResult && lastAnsweredQuestion != null -> {
                    // Question screen - show result
                    // Find the evaluation for this question
                    val evaluation = state.answerEvaluations.find { it.questionId == state.lastQuestionId }
                    QuestionContent(
                        question = lastAnsweredQuestion,
                        answeredCount = state.answeredCount,
                        selectedAnswer = state.lastSelectedAnswer,
                        isLoading = state.isLoading,
                        showResult = true,
                        isCorrect = evaluation?.isCorrect ?: false,
                        correctAnswer = state.lastCorrectAnswer ?: evaluation?.correctAnswer,
                        onAnswerSelected = { },
                        onSubmitAnswer = { },
                        onContinue = { viewModel.continueToNextQuestion() }
                    )
                }
                currentQuestion != null -> {
                    // Question screen - normal state
                    QuestionContent(
                        question = currentQuestion,
                        answeredCount = state.answeredCount,
                        selectedAnswer = state.selectedAnswer,
                        isLoading = state.isLoading,
                        showResult = false,
                        isCorrect = false,
                        correctAnswer = null,
                        onAnswerSelected = { viewModel.selectAnswer(it) },
                        onSubmitAnswer = { viewModel.submitAnswer() },
                        onContinue = { }
                    )
                }
                error != null -> {
                    // Error screen
                    ErrorContent(
                        error = error,
                        onRetry = { viewModel.startTest() },
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: PublicAdaptiveQuestion,
    answeredCount: Int,
    selectedAnswer: String?,
    isLoading: Boolean,
    showResult: Boolean = false,
    isCorrect: Boolean = false,
    correctAnswer: String? = null,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onContinue: () -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress indicator
        Text(
            text = "Câu hỏi ${answeredCount + 1}",
            fontSize = 14.sp,
            color = NavBarText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Passage card (if exists)
        if (!question.passage.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Passage label
                    Text(
                        text = "Đọc đoạn văn:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    
                    // Passage text
                    Text(
                        text = question.passage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        lineHeight = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Skill badge
                Surface(
                    color = Color(0xFF10B981),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = question.skill,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Question text
                Text(
                    text = question.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MainText,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Answer options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                // Determine if this option is correct or wrong
                val isSelectedOption = selectedAnswer?.equals(option, ignoreCase = true) == true
                val isCorrectAnswerOption = if (showResult && correctAnswer != null) {
                    // Backend provided correct answer - compare with option
                    correctAnswer.equals(option, ignoreCase = true)
                } else if (showResult && isSelectedOption) {
                    // Backend didn't provide correct answer - use evaluation result
                    // If user answered correctly, the selected answer is correct
                    isCorrect
                } else {
                    false
                }
                val isWrongSelected = showResult && isSelectedOption && !isCorrectAnswerOption
                val isCorrectSelected = showResult && isCorrectAnswerOption
                
                AnswerOption(
                    text = option,
                    isSelected = isSelectedOption,
                    isCorrect = isCorrectSelected,
                    isWrong = isWrongSelected,
                    showResult = showResult,
                    onClick = { if (!isLoading && !showResult) onAnswerSelected(option) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button or Continue button
        if (showResult) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Tiếp tục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onSubmitAnswer,
                enabled = selectedAnswer != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFE5E7EB)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Gửi câu trả lời",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedAnswer != null && !isLoading) Color.White else NavBarText
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean = false,
    isWrong: Boolean = false,
    showResult: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Color(0xFFDCFCE7) // Green background for correct answer
        isWrong -> Color(0xFFFEE2E2) // Red background for wrong selected answer
        isSelected -> Color(0xFFE0F2FE) // Light blue for selected
        else -> Color.White
    }
    
    val borderColor = when {
        isCorrect -> Color(0xFF10B981) // Green border
        isWrong -> Color(0xFFEF4444) // Red border
        isSelected -> GradientStart
        else -> Color(0xFFE5E7EB)
    }
    
    val borderWidth = if (isSelected || isCorrect || isWrong) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!showResult) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio button indicator or result icon
            if (showResult) {
                // Show checkmark for correct answer or X for wrong answer
                if (isCorrect) {
                    // Green checkmark
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFF10B981)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (isWrong) {
                    // Red X
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFFEF4444)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Empty circle for unselected options
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFF9CA3AF)
                        )
                    ) {}
                }
            } else {
                // Radio button indicator (normal state)
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = if (isSelected) GradientStart else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = if (isSelected) GradientStart else Color(0xFF9CA3AF)
                    )
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(12.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.White
                            ) {}
                        }
                    }
                }
            }

            Text(
                text = text,
                fontSize = 16.sp,
                color = MainText,
                fontWeight = if (isCorrect || isWrong) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TestCompletedContent(
    finalProficiency: ProficiencyLevel?,
    answeredCount: Int,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Surface(
            modifier = Modifier.size(80.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color(0xFFDCFCE7)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 48.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hoàn thành kiểm tra!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MainText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bạn đã trả lời $answeredCount câu hỏi",
            fontSize = 16.sp,
            color = NavBarText
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (finalProficiency != null) {
            Surface(
                color = when (finalProficiency) {
                    ProficiencyLevel.BEGINNER -> Color(0xFFFEF3C7)
                    ProficiencyLevel.INTERMEDIATE -> Color(0xFFDBEAFE)
                    ProficiencyLevel.ADVANCED -> Color(0xFFE9D5FF)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trình độ của bạn",
                        fontSize = 14.sp,
                        color = NavBarText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (finalProficiency) {
                            ProficiencyLevel.BEGINNER -> "Cơ bản"
                            ProficiencyLevel.INTERMEDIATE -> "Trung bình"
                            ProficiencyLevel.ADVANCED -> "Nâng cao"
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (finalProficiency) {
                            ProficiencyLevel.BEGINNER -> Color(0xFF92400E)
                            ProficiencyLevel.INTERMEDIATE -> Color(0xFF1E40AF)
                            ProficiencyLevel.ADVANCED -> Color(0xFF6B21A8)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = "Tiếp tục",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Đã xảy ra lỗi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MainText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            fontSize = 14.sp,
            color = NavBarText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Đóng")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("Thử lại")
            }
        }
    }
}

