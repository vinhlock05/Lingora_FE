package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.components.LearnPhaseContent
import com.example.lingora_fe.user.vocabulary.presentation.components.PracticePhaseContent
import com.example.lingora_fe.user.vocabulary.presentation.components.QuizPhaseContent

// Data class for Word
data class Word(
    val meaning: String,
    val pronunciation: String,
    val translation: String,
    val example: String,
    val exampleTranslation: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null
)

// Learning phases
enum class LearningPhase {
    LEARN,      // Learning new words
    PRACTICE,   // Flashcard practice
    QUIZ        // Quiz testing
}

// Quiz question types
enum class QuestionType {
    CHOOSE_MEANING,      // Chọn nghĩa đúng
    FILL_WORD,           // Điền từ
    TRUE_FALSE,          // Đúng/Sai
    LISTEN_FILL          // Nghe và điền
}

// Quiz question data class
data class QuizQuestion(
    val type: QuestionType,
    val question: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val word: Word
)

// Learning state
data class LearningState(
    val phase: LearningPhase = LearningPhase.LEARN,
    val currentWordIndex: Int = 0,
    val isFlashcardRevealed: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val typedAnswer: String = "",
    val isAnswerChecked: Boolean = false,
    val correctAnswers: Int = 0,
    val showCompletionDialog: Boolean = false,
    val showExitDialog: Boolean = false
)

@Composable
fun LearnWordScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample word list
    val words = remember {
        listOf(
            Word(
                meaning = "Hello",
                pronunciation = "/həˈloʊ/",
                translation = "Xin chào",
                example = "Hello! How are you today?",
                exampleTranslation = "Xin chào! Bạn khỏe không?",
                imageUrl = "https://picsum.photos/200/200?random=1"
            ),
            Word(
                meaning = "Goodbye",
                pronunciation = "/ɡʊdˈbaɪ/",
                translation = "Tạm biệt",
                example = "Goodbye! See you tomorrow.",
                exampleTranslation = "Tạm biệt! Hẹn gặp lại bạn ngày mai.",
                imageUrl = "https://picsum.photos/200/200?random=2"
            ),
            Word(
                meaning = "Thank you",
                pronunciation = "/θæŋk juː/",
                translation = "Cảm ơn",
                example = "Thank you for your help!",
                exampleTranslation = "Cảm ơn sự giúp đỡ của bạn!",
                imageUrl = "https://picsum.photos/200/200?random=3"
            ),
            Word(
                meaning = "Please",
                pronunciation = "/pliːz/",
                translation = "Làm ơn",
                example = "Please sit down.",
                exampleTranslation = "Làm ơn ngồi xuống.",
                imageUrl = "https://picsum.photos/200/200?random=4"
            ),
            Word(
                meaning = "Beautiful",
                pronunciation = "/ˈbjuːtɪfl/",
                translation = "Đẹp",
                example = "What a beautiful day!",
                exampleTranslation = "Thật là một ngày đẹp trời!",
                imageUrl = "https://picsum.photos/200/200?random=5"
            )
        )
    }

    // Learning state
    var state by remember { mutableStateOf(LearningState()) }
    
    // Generate quiz questions
    val quizQuestions = remember(words) {
        generateQuizQuestions(words)
    }

    // Calculate progress - Fixed to reach 100% only when last quiz question is completed
    val totalSteps = words.size + words.size + quizQuestions.size
    val currentStep = when (state.phase) {
        LearningPhase.LEARN -> state.currentWordIndex
        LearningPhase.PRACTICE -> words.size + state.currentWordIndex
        LearningPhase.QUIZ -> {
            val baseStep = words.size + words.size + state.currentQuestionIndex
            // Add 1 only when answer is checked for the last question
            if (state.currentQuestionIndex == quizQuestions.size - 1 && state.isAnswerChecked) {
                baseStep + 1
            } else {
                baseStep
            }
        }
    }
    val progressFraction = (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    // Get phase name
    val phaseName = when (state.phase) {
        LearningPhase.LEARN -> "Học từ mới"
        LearningPhase.PRACTICE -> "Luyện tập Flashcard"
        LearningPhase.QUIZ -> "Quiz"
    }

    // Back handler
    BackHandler {
        state = state.copy(showExitDialog = true)
    }

    // Exit confirmation dialog
    if (state.showExitDialog) {
        AlertDialog(
            onDismissRequest = { state = state.copy(showExitDialog = false) },
            title = { Text("Thoát khỏi bài học?") },
            text = { Text("Tiến trình của bạn sẽ không được lưu. Bạn có chắc muốn thoát?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        state = state.copy(showExitDialog = false)
                        onBackClick()
                    }
                ) {
                    Text("Thoát", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { state = state.copy(showExitDialog = false) }) {
                    Text("Tiếp tục học")
                }
            }
        )
    }

    // Completion dialog
    if (state.showCompletionDialog) {
        val accuracy = (state.correctAnswers.toFloat() / quizQuestions.size.toFloat() * 100).toInt()
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    "Chúc mừng! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Column {
                    Text(
                        "Bạn đã hoàn thành bài học!",
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
                        "${state.correctAnswers}/${quizQuestions.size} câu đúng",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Text("Hoàn thành")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Progress bar with phase name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Phase name
                Text(
                    text = phaseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .background(TopBarBorder, shape = RoundedCornerShape(4.dp))
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            // Content based on phase
            when (state.phase) {
                LearningPhase.LEARN -> {
                    LearnPhaseContent(
                        word = words[state.currentWordIndex],
                        currentIndex = state.currentWordIndex,
                        totalWords = words.size,
                        onPronunciationClick = { /* Handle pronunciation */ },
                        onPreviousClick = {
                            if (state.currentWordIndex > 0) {
                                state = state.copy(currentWordIndex = state.currentWordIndex - 1)
                            }
                        },
                        onNextClick = {
                            if (state.currentWordIndex < words.size - 1) {
                                state = state.copy(currentWordIndex = state.currentWordIndex + 1)
                            } else {
                                // Move to Practice phase
                                state = state.copy(
                                    phase = LearningPhase.PRACTICE,
                                    currentWordIndex = 0,
                                    isFlashcardRevealed = false
                                )
                            }
                        }
                    )
                }
                
                LearningPhase.PRACTICE -> {
                    PracticePhaseContent(
                        word = words[state.currentWordIndex],
                        currentIndex = state.currentWordIndex,
                        totalWords = words.size,
                        isRevealed = state.isFlashcardRevealed,
                        onCardClick = {
                            state = state.copy(isFlashcardRevealed = !state.isFlashcardRevealed)
                        },
                        onPronunciationClick = { /* Handle pronunciation */ },
                        onPreviousClick = {
                            if (state.currentWordIndex > 0) {
                                state = state.copy(
                                    currentWordIndex = state.currentWordIndex - 1,
                                    isFlashcardRevealed = false
                                )
                            }
                        },
                        onNextClick = {
                            if (state.currentWordIndex < words.size - 1) {
                                state = state.copy(
                                    currentWordIndex = state.currentWordIndex + 1,
                                    isFlashcardRevealed = false
                                )
                            } else {
                                // Move to Quiz phase
                                state = state.copy(
                                    phase = LearningPhase.QUIZ,
                                    currentQuestionIndex = 0,
                                    selectedAnswer = null,
                                    typedAnswer = "",
                                    isAnswerChecked = false,
                                    correctAnswers = 0
                                )
                            }
                        }
                    )
                }
                
                LearningPhase.QUIZ -> {
                    QuizPhaseContent(
                        question = quizQuestions[state.currentQuestionIndex],
                        currentIndex = state.currentQuestionIndex,
                        totalQuestions = quizQuestions.size,
                        selectedAnswer = state.selectedAnswer,
                        typedAnswer = state.typedAnswer,
                        isAnswerChecked = state.isAnswerChecked,
                        onAnswerSelected = { answer ->
                            if (!state.isAnswerChecked) {
                                state = state.copy(selectedAnswer = answer)
                            }
                        },
                        onTypedAnswerChanged = { answer ->
                            if (!state.isAnswerChecked) {
                                state = state.copy(typedAnswer = answer)
                            }
                        },
                        onCheckAnswer = {
                            val currentQuestion = quizQuestions[state.currentQuestionIndex]
                            val userAnswer = if (currentQuestion.type == QuestionType.FILL_WORD) {
                                state.typedAnswer.trim()
                            } else {
                                state.selectedAnswer
                            }
                            
                            if (userAnswer != null && userAnswer.isNotEmpty()) {
                                val isCorrect = userAnswer.equals(currentQuestion.correctAnswer, ignoreCase = true)
                                state = state.copy(
                                    isAnswerChecked = true,
                                    correctAnswers = if (isCorrect) state.correctAnswers + 1 else state.correctAnswers
                                )
                            }
                        },
                        onNextQuestion = {
                            if (state.currentQuestionIndex < quizQuestions.size - 1) {
                                state = state.copy(
                                    currentQuestionIndex = state.currentQuestionIndex + 1,
                                    selectedAnswer = null,
                                    typedAnswer = "",
                                    isAnswerChecked = false
                                )
                            } else {
                                // Show completion dialog
                                state = state.copy(showCompletionDialog = true)
                            }
                        },
                        onPronunciationClick = { /* Handle pronunciation */ }
                    )
                }
            }
        }
    }
}

// Helper function to generate quiz questions
private fun generateQuizQuestions(words: List<Word>): List<QuizQuestion> {
    val questions = mutableListOf<QuizQuestion>()
    
    words.forEach { word ->
        // For each word, generate different question types
        
        // 1. Choose meaning (Chọn nghĩa đúng)
        val otherWords = words.filter { it != word }.shuffled().take(3)
        questions.add(
            QuizQuestion(
                type = QuestionType.CHOOSE_MEANING,
                question = "Nghĩa của từ \"${word.meaning}\" là gì?",
                correctAnswer = word.translation,
                options = (listOf(word.translation) + otherWords.map { it.translation }).shuffled(),
                word = word
            )
        )
        
        // 2. Fill word (Điền từ - text input)
        questions.add(
            QuizQuestion(
                type = QuestionType.FILL_WORD,
                question = "Điền từ tiếng Anh của \"${word.translation}\":",
                correctAnswer = word.meaning,
                options = emptyList(), // No options for text input
                word = word
            )
        )
    }
    
    // Add True/False questions
    words.take(2).forEach { word ->
        val isTrue = Math.random() > 0.5
        val displayTranslation = if (isTrue) {
            word.translation
        } else {
            words.filter { it != word }.random().translation
        }
        
        questions.add(
            QuizQuestion(
                type = QuestionType.TRUE_FALSE,
                question = "\"${word.meaning}\" có nghĩa là \"$displayTranslation\"",
                correctAnswer = if (isTrue) "Đúng" else "Sai",
                options = listOf("Đúng", "Sai"),
                word = word
            )
        )
    }
    
    // Add Listen and fill questions
    words.take(3).forEach { word ->
        val otherWords = words.filter { it != word }.shuffled().take(3)
        questions.add(
            QuizQuestion(
                type = QuestionType.LISTEN_FILL,
                question = "Nghe và chọn từ đúng",
                correctAnswer = word.meaning,
                options = (listOf(word.meaning) + otherWords.map { it.meaning }).shuffled(),
                word = word
            )
        )
    }
    
    return questions.shuffled()
}
