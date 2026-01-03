package com.example.lingora_fe.user.practice.presentation.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.practice.presentation.viewmodel.ReviewViewModel
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress
import com.example.lingora_fe.user.vocabulary.presentation.components.PronunciationQuizContent
import com.example.lingora_fe.user.vocabulary.presentation.components.QuizPhaseContent
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.QuestionType
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.QuizQuestion
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType
import com.example.lingora_fe.util.AudioPlayerHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val playAudio: (String?) -> Unit = remember {
        { url ->
            if (!url.isNullOrEmpty()) {
                AudioPlayerHelper.playAudio(
                    audioUrl = url,
                    onError = { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(error.message ?: "Không thể phát âm thanh")
                        }
                    }
                )
            }
        }
    }

    val stopAudio: () -> Unit = remember {
        { AudioPlayerHelper.stop() }
    }

    DisposableEffect(Unit) {
        onDispose { AudioPlayerHelper.stop() }
    }

    val quizQuestions = remember(uiState.words, uiState.selectedGameTypes) {
        generateReviewQuizQuestions(uiState.words, uiState.selectedGameTypes)
    }
    var currentQuestions by remember(quizQuestions) { mutableStateOf(quizQuestions) }
    var currentQuestionIndex by remember(quizQuestions) { mutableIntStateOf(0) }
    var answeredCorrect by remember(quizQuestions) { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var typedAnswer by remember { mutableStateOf("") }
    var isAnswerChecked by remember { mutableStateOf(false) }
    val wrongCounts = remember { mutableStateMapOf<Int, Int>() }
    var showExitDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(quizQuestions) {
        currentQuestions = quizQuestions
        currentQuestionIndex = 0
        answeredCorrect = 0
        selectedAnswer = null
        typedAnswer = ""
        isAnswerChecked = false
        wrongCounts.clear()
        showCompletionDialog = false
    }

    LaunchedEffect(updateError) {
        updateError?.let { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            updateError = null
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.clearError()
        }
    }

    BackHandler(enabled = !uiState.isLoading) {
        showExitDialog = true
    }

    val totalQuestions = remember(quizQuestions) { quizQuestions.size }
    val progressFraction = remember(answeredCorrect, totalQuestions) {
        if (totalQuestions > 0) answeredCorrect.toFloat() / totalQuestions.toFloat() else 0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = "Phiên ôn tập",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.06f),
                            GradientEnd.copy(alpha = 0.02f)
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GradientStart)
                    }
                }

                uiState.words.isEmpty() -> {
                    EmptyReviewPlaceholder(onRetry = { viewModel.loadWordsForReview() })
                }

                currentQuestions.isEmpty() -> {
                    // No content, completion dialog will handle navigation
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tiến độ",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MainText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = progressFraction,
                                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                    trackColor = Color(0xFFE2E8F0),
                                    color = GradientStart
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${answeredCorrect}/${totalQuestions} câu đã hoàn thành",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val currentQuestion = currentQuestions.getOrNull(currentQuestionIndex)
                        if (currentQuestion != null) {
                            // Render PronunciationQuizContent for pronunciation questions
                            if (currentQuestion.type == QuestionType.PRONUNCIATION) {
                                PronunciationQuizContent(
                                    word = currentQuestion.word,
                                    attemptCount = currentQuestion.attemptCount,
                                    onResult = { isCorrect, recognizedText ->
                                        Log.d("ReviewScreen", "Pronunciation result: correct=$isCorrect, text=$recognizedText")
                                        
                                        if (isCorrect) {
                                            answeredCorrect += 1
                                            
                                            val remaining = currentQuestions.filterIndexed { index, _ -> index != currentQuestionIndex }
                                            currentQuestions = remaining
                                            
                                            if (currentQuestions.isEmpty()) {
                                                showCompletionDialog = true
                                            } else {
                                                currentQuestionIndex = if (currentQuestionIndex < currentQuestions.size) {
                                                    currentQuestionIndex.coerceAtMost(currentQuestions.lastIndex)
                                                } else {
                                                    0
                                                }
                                            }

                                        } else {
                                            stopAudio()
                                            val newAttemptCount = currentQuestion.attemptCount + 1
                                            val remaining = currentQuestions.filterIndexed { index, _ -> index != currentQuestionIndex }
                                            
                                            if (newAttemptCount >= 2) {
                                                // Đã thử 2 lần - bỏ qua
                                                Log.d("ReviewScreen", "Skipping pronunciation after 2 attempts")
                                                currentQuestions = remaining
                                                
                                                if (currentQuestions.isEmpty()) {
                                                    showCompletionDialog = true
                                                } else {
                                                    currentQuestionIndex = if (currentQuestionIndex < currentQuestions.size) {
                                                        currentQuestionIndex.coerceAtMost(currentQuestions.lastIndex)
                                                    } else {
                                                        0
                                                    }
                                                }
                                            } else {
                                                // Đưa xuống cuối với attemptCount tăng
                                                val updatedQuestion = currentQuestion.copy(attemptCount = newAttemptCount)
                                                currentQuestions = remaining + updatedQuestion
                                                val wordId = currentQuestion.word.id
                                                wrongCounts[wordId] = (wrongCounts[wordId] ?: 0) + 1
                                                
                                                currentQuestionIndex = if (currentQuestionIndex < currentQuestions.size) {
                                                    currentQuestionIndex
                                                } else {
                                                    0
                                                }
                                            }
                                        }
                                        
                                        selectedAnswer = null
                                        typedAnswer = ""
                                        isAnswerChecked = false
                                    },
                                    onListenClick = {
                                        playAudio(currentQuestion.word.audioUrl)
                                    }
                                )
                            } else {
                                // Render QuizPhaseContent for other question types
                                QuizPhaseContent(
                                    question = currentQuestion,
                                    answeredCount = answeredCorrect,
                                    totalQuestions = totalQuestions,
                                    selectedAnswer = selectedAnswer,
                                    typedAnswer = typedAnswer,
                                    isAnswerChecked = isAnswerChecked,
                                    onAnswerSelected = { answer ->
                                        if (!isAnswerChecked) {
                                            selectedAnswer = answer
                                        }
                                    },
                                    onTypedAnswerChanged = { answer ->
                                        if (!isAnswerChecked) {
                                            typedAnswer = answer
                                        }
                                    },
                                    onCheckAnswer = {
                                        val userAnswer = if (currentQuestion.type == QuestionType.LISTEN_FILL) {
                                            typedAnswer.trim()
                                        } else {
                                            selectedAnswer
                                        }
                                        if (!userAnswer.isNullOrEmpty()) {
                                            val isCorrect = userAnswer.equals(currentQuestion.correctAnswer, ignoreCase = true)
                                            if (isCorrect && !isAnswerChecked) {
                                                answeredCorrect += 1
                                            }
                                            isAnswerChecked = true
                                        }
                                    },
                                    onNextQuestion = {
                                        val question = currentQuestions.getOrNull(currentQuestionIndex) ?: return@QuizPhaseContent
                                        val userAnswer = if (question.type == QuestionType.LISTEN_FILL) {
                                            typedAnswer.trim()
                                        } else {
                                            selectedAnswer
                                        }
                                        val isCorrect = userAnswer?.equals(question.correctAnswer, ignoreCase = true) == true

                                        if (isCorrect) {
                                            val remaining = currentQuestions.filterIndexed { index, _ -> index != currentQuestionIndex }
                                            currentQuestions = remaining
                                            if (currentQuestions.isEmpty()) {
                                                showCompletionDialog = true
                                            } else {
                                                currentQuestionIndex = if (currentQuestionIndex < currentQuestions.size) {
                                                    currentQuestionIndex.coerceAtMost(currentQuestions.lastIndex)
                                                } else {
                                                    0
                                                }
                                            }
                                        } else {
                                            val remaining = currentQuestions.filterIndexed { index, _ -> index != currentQuestionIndex }
                                            currentQuestions = remaining + question
                                            val wordId = question.word.id
                                            wrongCounts[wordId] = (wrongCounts[wordId] ?: 0) + 1
                                            currentQuestionIndex = if (currentQuestionIndex < currentQuestions.size) {
                                                currentQuestionIndex
                                            } else {
                                                0
                                            }
                                        }

                                        selectedAnswer = null
                                        typedAnswer = ""
                                        isAnswerChecked = false
                                    },
                                    onPronunciationClick = {
                                        playAudio(currentQuestion.word.audioUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát khỏi phiên ôn?", fontWeight = FontWeight.Bold) },
            text = { Text("Tiến trình của bạn sẽ không được lưu. Bạn có chắc chắn muốn thoát?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) {
                    Text("Thoát", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục")
                }
            }
        )
    }

    if (showCompletionDialog) {
        CompletionDialog(
            totalQuestions = totalQuestions,
            correctAnswers = answeredCorrect,
            isUpdating = uiState.isUpdating,
            onConfirm = {
                viewModel.updateWordProgress(wrongCounts.toMap()) { success, error ->
                    Log.d("ReviewScreen", "Update progress result: success=$success, error=$error")
                    if (success) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("refreshReviewSummary", true)
                        navController.popBackStack()
                    } else {
                        updateError = error
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyReviewPlaceholder(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Không có từ nào để ôn",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MainText
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hãy kiểm tra lại sau khi hệ thống sắp xếp lịch ôn tập mới.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
        ) {
            Text(text = "Thử lại", color = Color.White)
        }
    }
}

@Composable
private fun CompletionDialog(
    totalQuestions: Int,
    correctAnswers: Int,
    isUpdating: Boolean,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Hoàn thành! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bạn đã hoàn thành phiên ôn tập",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$correctAnswers/$totalQuestions câu trả lời chính xác",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GradientStart
                )
                val accuracy = if (totalQuestions > 0) {
                    (correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
                } else {
                    0
                }
                Text(
                    text = "Độ chính xác: $accuracy%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Lưu kết quả", color = Color.White)
                }
            }
        }
    )
}

private fun generateReviewQuizQuestions(
    words: List<WordWithProgress>,
    selectedGameTypes: Set<GameType>
): List<QuizQuestion> {
    if (words.isEmpty()) return emptyList()
    val domainWords = words.map { it.toDomainWord() }
    val questions = mutableListOf<QuizQuestion>()
    val questionTypes = selectedGameTypes.map { gameType ->
        when (gameType) {
            GameType.LISTEN_FILL -> QuestionType.LISTEN_FILL
            GameType.LISTEN_CHOOSE -> QuestionType.LISTEN_CHOOSE
            GameType.TRUE_FALSE -> QuestionType.TRUE_FALSE
            GameType.SEE_WORD_CHOOSE_MEANING -> QuestionType.SEE_WORD_CHOOSE_MEANING
            GameType.SEE_MEANING_CHOOSE_WORD -> QuestionType.SEE_MEANING_CHOOSE_WORD
            GameType.PRONUNCIATION -> QuestionType.PRONUNCIATION
        }
    }

    domainWords.forEach { word ->
        questionTypes.forEach { questionType ->
            when (questionType) {
                QuestionType.LISTEN_FILL -> {
                    if (!word.audioUrl.isNullOrEmpty()) {
                        word.meaning?.let { meaning ->
                            questions.add(
                                QuizQuestion(
                                    type = QuestionType.LISTEN_FILL,
                                    question = "Nghe và viết lại từ bạn nghe được",
                                    correctAnswer = word.word,
                                    options = emptyList(),
                                    word = word
                                )
                            )
                        }
                    }
                }

                QuestionType.LISTEN_CHOOSE -> {
                    if (!word.audioUrl.isNullOrEmpty()) {
                        val otherWords = domainWords.filter { it.id != word.id }.shuffled().take(3)
                        val options = (listOf(word.word) + otherWords.map { it.word }).shuffled()
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.LISTEN_CHOOSE,
                                question = "Nghe và chọn từ đúng",
                                correctAnswer = word.word,
                                options = options,
                                word = word
                            )
                        )
                    }
                }

                QuestionType.TRUE_FALSE -> {
                    word.meaning?.let { meaning ->
                        val isTrue = Math.random() > 0.5
                        val displayedMeaning = if (isTrue) {
                            meaning
                        } else {
                            domainWords.filter { it.id != word.id && it.meaning != null }
                                .randomOrNull()?.meaning ?: meaning
                        }
                        if (!displayedMeaning.isNullOrEmpty()) {
                            questions.add(
                                QuizQuestion(
                                    type = QuestionType.TRUE_FALSE,
                                    question = "\"${word.word}\" có nghĩa là \"$displayedMeaning\"",
                                    correctAnswer = if (isTrue) "Đúng" else "Sai",
                                    options = listOf("Đúng", "Sai"),
                                    word = word
                                )
                            )
                        }
                    }
                }

                QuestionType.SEE_WORD_CHOOSE_MEANING -> {
                    word.meaning?.let { meaning ->
                        val otherWords = domainWords.filter { it.id != word.id && it.meaning != null }.shuffled().take(3)
                        val options = (listOf(meaning) + otherWords.mapNotNull { it.meaning }).shuffled()
                        if (options.isNotEmpty()) {
                            questions.add(
                                QuizQuestion(
                                    type = QuestionType.SEE_WORD_CHOOSE_MEANING,
                                    question = "Nghĩa nào đúng với từ \"${word.word}\"?",
                                    correctAnswer = meaning,
                                    options = options,
                                    word = word
                                )
                            )
                        }
                    }
                }

                QuestionType.SEE_MEANING_CHOOSE_WORD -> {
                    word.meaning?.let { meaning ->
                        val otherWords = domainWords.filter { it.id != word.id }.shuffled().take(3)
                        val options = (listOf(word.word) + otherWords.map { it.word }).shuffled()
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.SEE_MEANING_CHOOSE_WORD,
                                question = "Từ tiếng Anh nào khớp với nghĩa \"$meaning\"?",
                                correctAnswer = word.word,
                                options = options,
                                word = word
                            )
                        )
                    }
                }
                
                QuestionType.PRONUNCIATION -> {
                    if (!word.audioUrl.isNullOrEmpty()) {
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.PRONUNCIATION,
                                question = "Phát âm từ \"${word.word}\"",
                                correctAnswer = word.word,
                                options = emptyList(),
                                word = word
                            )
                        )
                    }
                }
            }
        }
        
        // Fallback: If no questions were generated for this word (due to missing audio for selected types),
        // add a non-audio question (SEE_MEANING_CHOOSE_WORD) so the word is not skipped.
        if (questions.none { it.word.id == word.id }) {
             word.meaning?.let { meaning ->
                val otherWords = domainWords.filter { it.id != word.id }.shuffled().take(3)
                val options = (listOf(word.word) + otherWords.map { it.word }).shuffled()
                questions.add(
                    QuizQuestion(
                        type = QuestionType.SEE_MEANING_CHOOSE_WORD,
                        question = "Từ tiếng Anh nào khớp với nghĩa \"$meaning\"?",
                        correctAnswer = word.word,
                        options = options,
                        word = word
                    )
                )
             }
        }
    }

    return questions.shuffled()
}

private fun WordWithProgress.toDomainWord(): Word {
    return Word(
        id = id,
        topicId = 0,
        cefrLevel = null,
        type = type,
        phonetic = phonetic,
        word = word,
        meaning = meaning,
        vnMeaning = vnMeaning,
        example = example,
        exampleTranslation = exampleTranslation,
        audioUrl = audioUrl,
        imageUrl = imageUrl
    )
}
