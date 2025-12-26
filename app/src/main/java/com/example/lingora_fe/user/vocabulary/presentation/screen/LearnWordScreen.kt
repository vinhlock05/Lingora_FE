package com.example.lingora_fe.user.vocabulary.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.presentation.components.LearnPhaseContent
import com.example.lingora_fe.user.vocabulary.presentation.components.PronunciationQuizContent
import com.example.lingora_fe.user.vocabulary.presentation.components.QuizPhaseContent
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.LearningPhase
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.LearningState
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.QuestionType
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.QuizQuestion
import com.example.lingora_fe.user.vocabulary.presentation.viewmodel.TopicDetailViewModel
import com.example.lingora_fe.util.AudioPlayerHelper

@Composable
fun LearnWordScreen(
    topicId: Int,
    wordCount: Int,
    gameTypes: Set<com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType>,
    onBackClick: () -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val playAudio: (String?) -> Unit = remember {
        { audioUrl ->
            if (!audioUrl.isNullOrEmpty()) {
                AudioPlayerHelper.playAudio(
                    audioUrl = audioUrl,
                    onError = { error ->
                        Log.e("LearnWordScreen", "Error playing audio", error)
                    }
                )
            }
        }
    }

    // Clean up MediaPlayer when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            AudioPlayerHelper.stop()
        }
    }
    
    // Load words for study when screen opens with user selected word count
    LaunchedEffect(topicId, wordCount) {
        viewModel.loadWordsForStudy(topicId, wordCount)
    }
    
    // Use study words directly from domain model
    val words = remember(uiState.studyWords) {
        Log.d("LearnWordScreen", "Using studyWords: ${uiState.studyWords.size} words")
        if (uiState.studyWords.isEmpty()) {
            Log.w("LearnWordScreen", "studyWords is empty!")
        }
        uiState.studyWords
    }

    // Show loading if words are being loaded
    if (uiState.isLoadingStudyWords && words.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Check if words are loaded
    if (words.isEmpty() && !uiState.isLoadingStudyWords) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Không có từ nào để học",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onBackClick) {
                    Text("Quay lại")
                }
            }
        }
        return
    }

    // Learning state
    var state by remember { mutableStateOf(LearningState()) }
    var lastPlayedLearnWordId by remember { mutableStateOf<Int?>(null) }
    
    // Generate quiz questions - only when words are available
    // Use gameTypes from navigation arguments instead of ViewModel state
    val quizQuestions = remember(words, gameTypes) {
        if (words.isNotEmpty()) {
            generateQuizQuestions(words, gameTypes)
        } else {
            emptyList()
        }
    }
    val initialQuizCount = remember(quizQuestions) { quizQuestions.size }

    // Track wrong questions to move to end
    var currentQuizQuestions by remember(quizQuestions) {
        mutableStateOf(quizQuestions)
    }

    LaunchedEffect(state.phase) {
        when (state.phase) {
            LearningPhase.LEARN -> {
                lastPlayedLearnWordId = null
            }
            LearningPhase.QUIZ -> {
                // No auto play in quiz phase
            }
        }
    }

    val currentLearnWordId = words.getOrNull(state.currentWordIndex)?.id
    LaunchedEffect(state.phase, state.currentWordIndex, currentLearnWordId) {
        if (state.phase == LearningPhase.LEARN) {
            val index = state.currentWordIndex
            val word = words.getOrNull(index)
            if (word != null && word.id != lastPlayedLearnWordId) {
                playAudio(word.audioUrl)
                lastPlayedLearnWordId = word.id
            }
        }
    }

    // Calculate progress based on learned words and correctly answered quiz questions
    val totalSteps = words.size + initialQuizCount
    val currentStep = when (state.phase) {
        LearningPhase.LEARN -> state.currentWordIndex
        LearningPhase.QUIZ -> {
            words.size + state.correctAnswers
        }
    }
    val progressFraction = if (totalSteps > 0) {
        (currentStep.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Get phase name
    val phaseName = when (state.phase) {
        LearningPhase.LEARN -> "Học từ mới"
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
        val totalQuestions = initialQuizCount
        val accuracy = if (totalQuestions > 0) {
            (state.correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
        } else {
            0
        }
        
        // Create WordProgress after learning is completed
        LaunchedEffect(Unit) {
            val wordIds = uiState.studyWords.map { it.id }
            if (wordIds.isNotEmpty()) {
                viewModel.createWordProgressAfterLearning(
                    wordIds = wordIds,
                    onSuccess = {
                        // Success - progress created
                    },
                    onError = { error ->
                        // Handle error if needed
                    }
                )
            }
        }
        
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
                        "${state.correctAnswers}/${totalQuestions} câu đúng",
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart.copy(alpha = 0.06f),
                        GradientEnd.copy(alpha = 0.02f)
                    )
                )
            )
    ) {
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
                    if (state.currentWordIndex < words.size) {
                        LearnPhaseContent(
                            word = words[state.currentWordIndex],
                            currentIndex = state.currentWordIndex,
                            totalWords = words.size,
                            isRevealed = state.isFlashcardRevealed,
                            onCardClick = {
                                state = state.copy(isFlashcardRevealed = !state.isFlashcardRevealed)
                            },
                            onPronunciationClick = {
                                val currentWord = words[state.currentWordIndex]
                                playAudio(currentWord.audioUrl)
                            },
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
                                    // Reset quiz questions
                                    currentQuizQuestions = quizQuestions
                                }
                            }
                        )
                    }
                }
                
                LearningPhase.QUIZ -> {
                    if (state.currentQuestionIndex < currentQuizQuestions.size) {
                        val currentQuestion = currentQuizQuestions[state.currentQuestionIndex]
                        
                        // Render PronunciationQuizContent for pronunciation questions
                        if (currentQuestion.type == QuestionType.PRONUNCIATION) {
                            PronunciationQuizContent(
                                word = currentQuestion.word,
                                attemptCount = currentQuestion.attemptCount,
                                onResult = { isCorrect, recognizedText ->
                                    Log.d("LearnWordScreen", "Pronunciation result: correct=$isCorrect, text=$recognizedText")
                                    
                                    if (isCorrect) {
                                        // Đúng - tăng correctAnswers và xóa câu hỏi
                                        state = state.copy(correctAnswers = state.correctAnswers + 1)
                                        
                                        val remainingQuestions = currentQuizQuestions.filterIndexed { index, _ ->
                                            index != state.currentQuestionIndex
                                        }
                                        currentQuizQuestions = remainingQuestions
                                        
                                        if (currentQuizQuestions.isEmpty()) {
                                            state = state.copy(showCompletionDialog = true)
                                        } else {
                                            val nextIndex = if (state.currentQuestionIndex < currentQuizQuestions.size) {
                                                state.currentQuestionIndex
                                            } else {
                                                0
                                            }
                                            state = state.copy(
                                                currentQuestionIndex = nextIndex,
                                                selectedAnswer = null,
                                                typedAnswer = "",
                                                isAnswerChecked = false
                                            )
                                        }
                                    } else {
                                        // Sai - kiểm tra attempt count
                                        val newAttemptCount = currentQuestion.attemptCount + 1
                                        val remainingQuestions = currentQuizQuestions.filterIndexed { index, _ ->
                                            index != state.currentQuestionIndex
                                        }
                                        
                                        if (newAttemptCount >= 2) {
                                            // Đã thử 2 lần - bỏ qua câu này
                                            Log.d("LearnWordScreen", "Skipping pronunciation after 2 attempts")
                                            currentQuizQuestions = remainingQuestions
                                            
                                            if (currentQuizQuestions.isEmpty()) {
                                                state = state.copy(showCompletionDialog = true)
                                            } else {
                                                val nextIndex = if (state.currentQuestionIndex < currentQuizQuestions.size) {
                                                    state.currentQuestionIndex
                                                } else {
                                                    0
                                                }
                                                state = state.copy(
                                                    currentQuestionIndex = nextIndex,
                                                    selectedAnswer = null,
                                                    typedAnswer = "",
                                                    isAnswerChecked = false
                                                )
                                            }
                                        } else {
                                            // Đưa xuống cuối với attemptCount tăng
                                            val updatedQuestion = currentQuestion.copy(attemptCount = newAttemptCount)
                                            currentQuizQuestions = remainingQuestions + updatedQuestion
                                            
                                            val nextIndex = if (state.currentQuestionIndex < currentQuizQuestions.size) {
                                                state.currentQuestionIndex
                                            } else {
                                                0
                                            }
                                            state = state.copy(
                                                currentQuestionIndex = nextIndex,
                                                selectedAnswer = null,
                                                typedAnswer = "",
                                                isAnswerChecked = false
                                            )
                                        }
                                    }
                                },
                                onListenClick = {
                                    playAudio(currentQuestion.word.audioUrl)
                                }
                            )
                        } else {
                            // Render QuizPhaseContent for other question types
                            QuizPhaseContent(
                                question = currentQuestion,
                                answeredCount = state.correctAnswers,
                                totalQuestions = initialQuizCount,
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
                                    val userAnswer = if (currentQuestion.type == QuestionType.LISTEN_FILL) {
                                        state.typedAnswer.trim()
                                    } else {
                                        state.selectedAnswer
                                    }
                                    
                                    if (userAnswer != null && userAnswer.isNotEmpty()) {
                                        val isCorrect = userAnswer.equals(currentQuestion.correctAnswer, ignoreCase = true)
                                        
                                        if (isCorrect) {
                                            state = state.copy(
                                                isAnswerChecked = true,
                                                correctAnswers = state.correctAnswers + 1
                                            )
                                        } else {
                                            state = state.copy(
                                                isAnswerChecked = true
                                            )
                                        }
                                    }
                                },
                                onNextQuestion = {
                                    val userAnswer = if (currentQuestion.type == QuestionType.LISTEN_FILL) {
                                        state.typedAnswer.trim()
                                    } else {
                                        state.selectedAnswer
                                    }
                                    val isCorrect = userAnswer?.equals(currentQuestion.correctAnswer, ignoreCase = true) == true
                                    
                                    if (isCorrect) {
                                        val remainingQuestions = currentQuizQuestions.filterIndexed { index, _ ->
                                            index != state.currentQuestionIndex
                                        }
                                        currentQuizQuestions = remainingQuestions
                                        
                                        if (currentQuizQuestions.isEmpty()) {
                                            state = state.copy(showCompletionDialog = true)
                                        } else {
                                            val nextIndex = if (state.currentQuestionIndex < currentQuizQuestions.size) {
                                                state.currentQuestionIndex
                                            } else {
                                                0
                                            }
                                            state = state.copy(
                                                currentQuestionIndex = nextIndex,
                                                selectedAnswer = null,
                                                typedAnswer = "",
                                                isAnswerChecked = false
                                            )
                                        }
                                    } else {
                                        val remainingQuestions = currentQuizQuestions.filterIndexed { index, _ ->
                                            index != state.currentQuestionIndex
                                        }
                                        currentQuizQuestions = remainingQuestions + currentQuestion
                                        
                                        val nextIndex = if (state.currentQuestionIndex < currentQuizQuestions.size) {
                                            state.currentQuestionIndex
                                        } else {
                                            0
                                        }
                                        state = state.copy(
                                            currentQuestionIndex = nextIndex,
                                            selectedAnswer = null,
                                            typedAnswer = "",
                                            isAnswerChecked = false
                                        )
                                    }
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

// Helper function to generate quiz questions based on selected game types
private fun generateQuizQuestions(
    words: List<com.example.lingora_fe.user.vocabulary.domain.model.Word>,
    selectedGameTypes: Set<com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType>
): List<QuizQuestion> {
    val questions = mutableListOf<QuizQuestion>()
    
    // Convert GameType to QuestionType
    val questionTypes = selectedGameTypes.map { gameType ->
        when (gameType) {
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.LISTEN_FILL -> QuestionType.LISTEN_FILL
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.LISTEN_CHOOSE -> QuestionType.LISTEN_CHOOSE
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.TRUE_FALSE -> QuestionType.TRUE_FALSE
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.SEE_WORD_CHOOSE_MEANING -> QuestionType.SEE_WORD_CHOOSE_MEANING
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.SEE_MEANING_CHOOSE_WORD -> QuestionType.SEE_MEANING_CHOOSE_WORD
            com.example.lingora_fe.user.vocabulary.presentation.viewmodel.GameType.PRONUNCIATION -> QuestionType.PRONUNCIATION
        }
    }
    
    words.forEach { word ->
        questionTypes.forEach { questionType ->
            when (questionType) {
                QuestionType.LISTEN_FILL -> {
                    // Nghe điền từ - text input
                    word.meaning?.let { meaning ->
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.LISTEN_FILL,
                                question = "Nghe và điền từ tiếng Anh của \"$meaning\":",
                                correctAnswer = word.word,
                                options = emptyList(), // Text input
                                word = word
                            )
                        )
                    }
                }
                QuestionType.LISTEN_CHOOSE -> {
                    // Nghe chọn từ - multiple choice
                    val otherWords = words.filter { it != word }.shuffled().take(3)
                    questions.add(
                        QuizQuestion(
                            type = QuestionType.LISTEN_CHOOSE,
                            question = "Nghe và chọn từ đúng",
                            correctAnswer = word.word,
                            options = (listOf(word.word) + otherWords.map { it.word }).shuffled(),
                            word = word
                        )
                    )
                }
                QuestionType.TRUE_FALSE -> {
                    // Đúng/Sai
                    word.meaning?.let { meaning ->
                        val isTrue = Math.random() > 0.5
                        val displayMeaning = if (isTrue) {
                            meaning
                        } else {
                            words.filter { it != word && it.meaning != null }.randomOrNull()?.meaning ?: meaning
                        }
                        if (displayMeaning != null) {
                            questions.add(
                                QuizQuestion(
                                    type = QuestionType.TRUE_FALSE,
                                    question = "\"${word.word}\" có nghĩa là \"$displayMeaning\"",
                                    correctAnswer = if (isTrue) "Đúng" else "Sai",
                                    options = listOf("Đúng", "Sai"),
                                    word = word
                                )
                            )
                        }
                    }
                }
                QuestionType.SEE_WORD_CHOOSE_MEANING -> {
                    // Nhìn từ chọn nghĩa
                    word.meaning?.let { meaning ->
                        val otherWords = words.filter { it != word && it.meaning != null }.shuffled().take(3)
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.SEE_WORD_CHOOSE_MEANING,
                                question = "Nghĩa của từ \"${word.word}\" là gì?",
                                correctAnswer = meaning,
                                options = (listOf(meaning) + otherWords.mapNotNull { it.meaning }).shuffled(),
                                word = word
                            )
                        )
                    }
                }
                QuestionType.SEE_MEANING_CHOOSE_WORD -> {
                    // Nhìn nghĩa chọn từ
                    word.meaning?.let { meaning ->
                        val otherWords = words.filter { it != word }.shuffled().take(3)
                        questions.add(
                            QuizQuestion(
                                type = QuestionType.SEE_MEANING_CHOOSE_WORD,
                                question = "Từ tiếng Anh của \"$meaning\" là gì?",
                                correctAnswer = word.word,
                                options = (listOf(word.word) + otherWords.map { it.word }).shuffled(),
                                word = word
                            )
                        )
                    }
                }
                QuestionType.PRONUNCIATION -> {
                    // Luyện phát âm
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

    val targetSize = (words.size * 2).coerceAtLeast(1)
    val finalQuestions = questions.shuffled().take(targetSize)
    return finalQuestions
}
