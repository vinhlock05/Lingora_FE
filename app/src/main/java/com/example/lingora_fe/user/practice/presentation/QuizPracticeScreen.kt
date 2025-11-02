package com.example.lingora_fe.user.practice.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.core.ui.theme.TopBarBorder

// Quiz question types
enum class QuizQuestionType {
    CHOOSE_MEANING,      // Chọn nghĩa đúng
    CHOOSE_WORD          // Chọn từ đúng
}

// Quiz question data class
data class QuizQuestion(
    val type: QuizQuestionType,
    val word: ReviewWord,
    val question: String,
    val correctAnswer: String,
    val options: List<String>
)

// Helper function to generate quiz questions from words
private fun generateQuizQuestions(words: List<ReviewWord>): List<QuizQuestion> {
    val questions = mutableListOf<QuizQuestion>()
    
    words.forEach { word ->
        // Generate "Choose meaning" question
        val otherWords = words.filter { it != word }.shuffled().take(3)
        questions.add(
            QuizQuestion(
                type = QuizQuestionType.CHOOSE_MEANING,
                word = word,
                question = "Nghĩa của từ \"${word.word}\" là gì?",
                correctAnswer = word.translation,
                options = (listOf(word.translation) + otherWords.map { it.translation }).shuffled()
            )
        )
        
        // Generate "Choose word" question
        questions.add(
            QuizQuestion(
                type = QuizQuestionType.CHOOSE_WORD,
                word = word,
                question = "Từ nào có nghĩa là \"${word.translation}\"?",
                correctAnswer = word.word,
                options = (listOf(word.word) + otherWords.map { it.word }).shuffled()
            )
        )
    }
    
    return questions.shuffled()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPracticeScreen(
    navController: NavController
) {
    // Sample word list for quiz
    val words = remember {
        listOf(
            ReviewWord(
                word = "Ambitious",
                pronunciation = "/æm'bɪʃəs/",
                translation = "Tham vọng, đầy tham vọng",
                example = "She is very ambitious and wants to become a CEO.",
                exampleTranslation = "Cô ấy rất tham vọng và muốn trở thành CEO.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Beautiful",
                pronunciation = "/'bju:tɪfəl/",
                translation = "Đẹp",
                example = "What a beautiful day!",
                exampleTranslation = "Thật là một ngày đẹp trời!",
                level = "Dễ"
            ),
            ReviewWord(
                word = "Confident",
                pronunciation = "/'kɒnfɪdənt/",
                translation = "Tự tin",
                example = "He feels confident about the exam.",
                exampleTranslation = "Anh ấy cảm thấy tự tin về kỳ thi.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Diligent",
                pronunciation = "/'dɪlɪdʒənt/",
                translation = "Siêng năng, cần cù",
                example = "She is a diligent student.",
                exampleTranslation = "Cô ấy là một học sinh siêng năng.",
                level = "Trung bình"
            ),
            ReviewWord(
                word = "Enthusiastic",
                pronunciation = "/ɪn,θju:zi'æstɪk/",
                translation = "Nhiệt tình, hăng hái",
                example = "The team is enthusiastic about the project.",
                exampleTranslation = "Nhóm rất nhiệt tình với dự án.",
                level = "Khó"
            )
        )
    }

    // Generate quiz questions
    val quizQuestions = remember(words) {
        generateQuizQuestions(words)
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val currentQuestion = quizQuestions[currentQuestionIndex]

    // Back handler
    BackHandler {
        showExitDialog = true
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi kiểm tra?") },
            text = { Text("Tiến trình kiểm tra của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
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
                    Text("Tiếp tục")
                }
            }
        )
    }

    // Completion dialog
    if (showCompletionDialog) {
        val accuracy = (correctCount.toFloat() / quizQuestions.size * 100).toInt()
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    "Hoàn thành! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Column {
                    Text(
                        "Bạn đã hoàn thành bài kiểm tra!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Kết quả: $accuracy%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart
                    )
                    Text(
                        "$correctCount/${quizQuestions.size} câu đúng",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Text("Hoàn thành")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kiểm tra Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
            val progressPercent = ((currentQuestionIndex + 1).toFloat() / quizQuestions.size * 100).toInt()
            val levelColor = when (currentQuestion.word.level) {
                "Dễ" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
                "Khó" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                else -> Color(0xFFFEF3C7) to Color(0xFF92400E)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = if (showResult) 420.dp else 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Câu ${currentQuestionIndex + 1}/${quizQuestions.size}",
                            fontSize = 14.sp,
                            color = NavBarText
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 14.sp,
                            color = NavBarText
                        )
                    }

                    LinearProgressIndicator(
                        progress = (currentQuestionIndex + 1).toFloat() / quizQuestions.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GradientStart,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }

                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0FDFA)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = levelColor.first,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentQuestion.word.level,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = levelColor.second
                            )
                        }

                        if (currentQuestion.type == QuizQuestionType.CHOOSE_MEANING) {
                            Text(
                                text = currentQuestion.word.word,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Play audio",
                                    tint = GradientStart,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = currentQuestion.word.pronunciation,
                                    fontSize = 16.sp,
                                    color = NavBarText
                                )
                            }
                        } else {
                            Text(
                                text = "\"${currentQuestion.word.translation}\"",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.question,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavBarText
                        )
                    }
                }

                // Answer Options
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentQuestion.options.forEach { option ->
                        AnswerOption(
                            text = option,
                            isSelected = selectedAnswer == option,
                            isCorrect = if (showResult) option == currentQuestion.correctAnswer else null,
                            onClick = {
                                if (!showResult) {
                                    selectedAnswer = option
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Action Buttons or Feedback Card
            if (!showResult) {
                // Navigation Buttons (when not showing result)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                                selectedAnswer = null
                                showResult = false
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
                            if (selectedAnswer != null) {
                                // Check answer
                                showResult = true
                                if (selectedAnswer == currentQuestion.correctAnswer) {
                                    correctCount++
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GradientStart
                        ),
                        enabled = selectedAnswer != null
                    ) {
                        Text(
                            text = "Xác nhận",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Bottom Feedback Card (when showing result)
                QuizBottomFeedbackCard(
                    isCorrect = selectedAnswer == currentQuestion.correctAnswer,
                    correctAnswer = currentQuestion.correctAnswer,
                    word = currentQuestion.word,
                    currentIndex = currentQuestionIndex,
                    totalQuestions = quizQuestions.size,
                    onNextClick = {
                        if (currentQuestionIndex < quizQuestions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null
                            showResult = false
                        } else {
                            // Show completion dialog
                            showCompletionDialog = true
                        }
                    },
                    onPronunciationClick = { /* TODO: Implement audio playback */ },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun QuizBottomFeedbackCard(
    isCorrect: Boolean,
    correctAnswer: String,
    word: ReviewWord,
    currentIndex: Int,
    totalQuestions: Int,
    onNextClick: () -> Unit,
    onPronunciationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) GradientStart.copy(alpha = 0.95f) else MaterialTheme.colorScheme.error.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Status header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Text(
                    text = if (isCorrect) "Chính xác! 🎉" else "Chưa đúng 😔",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (!isCorrect) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đáp án đúng: $correctAnswer",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Word details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Word
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pronunciation with speaker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPronunciationClick,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Pronounce",
                                tint = GradientEnd,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = word.pronunciation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GradientEnd
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Translation
                    Text(
                        text = word.translation,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Example
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TopBarBorder, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "\"${word.example}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${word.exampleTranslation}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next button
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentIndex < totalQuestions - 1) "Câu tiếp theo" else "Hoàn thành",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) GradientStart else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFFD1FAE5)
        isCorrect == false && isSelected -> Color(0xFFFEE2E2)
        isSelected -> Color(0xFFDCECFE)
        else -> Color.White
    }

    val borderColor = when {
        isCorrect == true -> Color(0xFF10B981)
        isCorrect == false && isSelected -> Color(0xFFEF4444)
        isSelected -> GradientStart
        else -> Color(0xFFE5E7EB)
    }

    val textColor = when {
        isCorrect != null -> MainText
        isSelected -> GradientStart
        else -> MainText
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 15.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

