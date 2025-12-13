package com.example.lingora_fe.user.exam.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.rememberAudioPlayer
import com.example.lingora_fe.user.exam.presentation.components.UniversalQuestionRenderer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeScreen(
    navController: NavController,
    testId: String,
    sectionId: Int,
    isPracticeMode: Boolean = true, // true = practice, false = actual exam
    attemptId: Int? = null
) {
    val examId = testId.toIntOrNull() ?: 0
    val viewModel: ExamViewModel = hiltViewModel()
    val sectionState by viewModel.sectionState.collectAsState()
    
    LaunchedEffect(sectionId, attemptId) { 
        viewModel.loadSection(examId, sectionId)
        attemptId?.let { viewModel.setExistingAttemptId(it) }
    }
    
    // NEW STRUCTURE: Section -> SectionGroup -> QuestionGroup -> Questions
    val sectionGroups = sectionState.section?.groups ?: emptyList()
    var currentSectionGroupIndex by remember(sectionState.section) { mutableStateOf(0) }
    val currentSectionGroup = sectionGroups.getOrNull(currentSectionGroupIndex)
    
    // Get all question groups from current section group
    val questionGroups = currentSectionGroup?.questionGroups ?: emptyList()
    var currentQuestionGroupIndex by remember(currentSectionGroupIndex) { mutableStateOf(0) }
    val currentQuestionGroup = questionGroups.getOrNull(currentQuestionGroupIndex)
    
    // Get questions from current question group
    val questions = currentQuestionGroup?.questions ?: emptyList()
    var currentQuestionIndex by remember(currentQuestionGroupIndex) { mutableStateOf(0) }
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    
    // Store answers for all questions: Map<questionId, answer>
    val answersMap = remember { mutableStateMapOf<Int, Any?>() }
    
    // Get saved answer for current question
    val selectedAnswer = currentQuestion?.let { answersMap[it.id] }
    
    // Get audio URL from section group resourceUrl or section audioUrl
    val audioUrl = currentSectionGroup?.resourceUrl ?: sectionState.section?.audioUrl
    
    // Track if audio has been played for this section group
    var hasPlayedOnce by remember(currentSectionGroupIndex) { mutableStateOf(false) }
    
    // Use audio player utility with 2 second buffer delay
    val audioPlayer = rememberAudioPlayer(
        audioUrl = audioUrl,
        autoPlay = !hasPlayedOnce,
        bufferDelayMs = 2000L,
        onPlaybackEnded = {
            hasPlayedOnce = true
        }
    )
    
    // Dialogs
    var showSubmitSuccessDialog by remember { mutableStateOf(false) }
    var showExitWarningDialog by remember { mutableStateOf(false) }  // For back button - warn about losing progress
    var showSubmitDialog by remember { mutableStateOf(false) }        // For submit button - confirm submission
    
    // Timer
    val sectionDuration = sectionState.section?.durationSeconds ?: 30 * 60
    var timeRemaining by remember(sectionDuration) { mutableStateOf(sectionDuration) }
    var autoSubmitted by remember { mutableStateOf(false) }
    
    // Calculate answered questions count
    val answeredInCurrentQuestionGroup = questions.count { q -> answersMap[q.id] != null }
    val totalQuestionsInSectionGroup = questionGroups.sumOf { it.questions.size }
    val answeredInSectionGroup = questionGroups.flatMap { it.questions }.count { q -> answersMap[q.id] != null }
    
    // Navigation flags
    val isLastSectionGroup = currentSectionGroupIndex >= sectionGroups.size - 1
    val isLastQuestionGroup = currentQuestionGroupIndex >= questionGroups.size - 1
    val isLastQuestion = currentQuestionIndex >= questions.size - 1
    val isAtVeryEnd = isLastSectionGroup && isLastQuestionGroup && isLastQuestion
    
    // Timer countdown
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        } else {
            if (!autoSubmitted) {
                autoSubmitted = true
                audioPlayer.stop()
                viewModel.submitCurrentSection(examId)
            }
        }
    }
    Log.d("ListeningPracticeScreen", "sectionState message: ${sectionState.message}, isSubmitting: ${sectionState.isSubmitting}")
    // Handle submit success
    LaunchedEffect(sectionState.message) {
        if (sectionState.message != null && !sectionState.isSubmitting) {
            showSubmitSuccessDialog = true
        }
    }
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    
    // Back handler - show exit warning
    BackHandler {
        showExitWarningDialog = true
    }
    
    // Exit Warning Dialog (Back button) - cảnh báo mất tiến độ
    if (showExitWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExitWarningDialog = false },
            title = { 
                Text(
                    text = "Thoát khỏi bài thi?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ) 
            },
            text = { 
                Text(
                    text = "Bài làm của bạn sẽ không được lưu. Bạn có chắc chắn muốn thoát?",
                    color = NavBarText
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitWarningDialog = false
                        audioPlayer.stop()
                        navController.popBackStack()
                    }
                ) {
                    Text("Thoát", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarningDialog = false }) {
                    Text("Tiếp tục làm bài", color = GradientStart, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Submit Confirmation Dialog (Nộp bài button)
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { 
                Text(
                    text = "Nộp bài thi?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ) 
            },
            text = { 
                Text(
                    text = "Bạn có chắc chắn muốn nộp bài? Sau khi nộp bạn sẽ không thể chỉnh sửa.",
                    color = NavBarText
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitDialog = false
                        audioPlayer.stop()
                        viewModel.submitCurrentSection(examId)
                    },
                    enabled = !sectionState.isSubmitting
                ) {
                    Text("Nộp bài", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Tiếp tục làm bài", color = NavBarText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Submit success dialog
    if (showSubmitSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Đã nộp bài thành công!", fontWeight = FontWeight.Bold) },
            text = { Text("Bài làm của bạn đã được ghi nhận.", color = NavBarText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitSuccessDialog = false
                        navController.previousBackStackEntry?.savedStateHandle?.set("completedSectionId", sectionId)
                        navController.popBackStack()
                    }
                ) {
                    Text("OK", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timerText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !sectionState.isSubmitting
                    ) {
                        if (sectionState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Nộp bài", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
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
                .imePadding() // Thêm padding khi keyboard mở
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Group Navigation: <- Section Group Title ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentSectionGroupIndex > 0) {
                            audioPlayer.stop()
                            hasPlayedOnce = false
                            currentSectionGroupIndex--
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentSectionGroupIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous section",
                        tint = if (currentSectionGroupIndex > 0) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSectionGroup?.title ?: "Section ${currentSectionGroupIndex + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$answeredInSectionGroup/$totalQuestionsInSectionGroup câu đã làm",
                        fontSize = 12.sp,
                        color = NavBarText
                    )
                }
                
                IconButton(
                    onClick = {
                        if (currentSectionGroupIndex < sectionGroups.size - 1) {
                            audioPlayer.stop()
                            hasPlayedOnce = false
                            currentSectionGroupIndex++
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentSectionGroupIndex < sectionGroups.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next section",
                        tint = if (currentSectionGroupIndex < sectionGroups.size - 1) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Section Group progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                sectionGroups.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    idx == currentSectionGroupIndex -> GradientStart
                                    idx < currentSectionGroupIndex -> Color(0xFF10B981)
                                    else -> Color(0xFFE5E7EB)
                                }
                            )
                    )
                }
            }

            // Audio Player Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPracticeMode && hasPlayedOnce) {
                            IconButton(
                                onClick = { audioPlayer.replay() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF6B7280), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = {
                                if (isPracticeMode) {
                                    audioPlayer.togglePlayPause()
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    when {
                                        audioPlayer.state.isBuffering -> Color(0xFFFBBF24)
                                        audioPlayer.state.isPlaying -> Color(0xFF3B82F6)
                                        else -> Color(0xFF9CA3AF)
                                    },
                                    CircleShape
                                ),
                            enabled = isPracticeMode || (!hasPlayedOnce && audioPlayer.state.isReady)
                        ) {
                            if (audioPlayer.state.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (audioPlayer.state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (audioPlayer.state.isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = when {
                            audioPlayer.state.isBuffering -> "Đang tải..."
                            audioPlayer.state.error != null -> "Lỗi: ${audioPlayer.state.error}"
                            !isPracticeMode && hasPlayedOnce -> "Audio đã phát xong"
                            audioPlayer.state.isPlaying -> "Đang phát..."
                            audioPlayer.state.isReady -> "Sẵn sàng"
                            else -> "Audio"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (audioPlayer.state.error != null) Color(0xFFEF4444) else MainText
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isPracticeMode) {
                            Slider(
                                value = audioPlayer.state.progress,
                                onValueChange = { audioPlayer.seekToProgress(it) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = audioPlayer.state.isReady,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF3B82F6),
                                    activeTrackColor = Color(0xFF3B82F6),
                                    inactiveTrackColor = Color.White
                                )
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { audioPlayer.state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF3B82F6),
                                trackColor = Color.White
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(audioPlayer.state.currentTimeFormatted, fontSize = 12.sp, color = NavBarText)
                            Text(audioPlayer.state.durationFormatted, fontSize = 12.sp, color = NavBarText)
                        }
                    }
                    
                    Surface(
                        color = if (isPracticeMode) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isPracticeMode) "Practice Mode" else "Exam Mode",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = if (isPracticeMode) Color(0xFF065F46) else Color(0xFFDC2626)
                        )
                    }
                }
            }

            // Question Group Header Card
            currentQuestionGroup?.let { qGroup ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = qGroup.title ?: "Questions",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "$answeredInCurrentQuestionGroup/${questions.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3B82F6)
                            )
                        }
                        
                        qGroup.description?.let { desc ->
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = NavBarText,
                                lineHeight = 18.sp
                            )
                        }
                        
                        // Display word list or options if content exists
                        qGroup.content?.let { content ->
                            parseAndDisplayContent(content, qGroup.metadata)
                        }
                        
                        // Question group navigation
                        if (questionGroups.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                questionGroups.forEachIndexed { idx, qg ->
                                    val isSelected = idx == currentQuestionGroupIndex
                                    Surface(
                                        onClick = {
                                            currentQuestionGroupIndex = idx
                                            currentQuestionIndex = 0
                                        },
                                        color = if (isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = qg.title?.replace("Questions ", "Q") ?: "Q${idx + 1}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) Color.White else NavBarText
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                    fontWeight = FontWeight.Medium,
                    color = MainText
                )
                Text(
                    text = "Section ${currentSectionGroupIndex + 1}/${sectionGroups.size}",
                    fontSize = 12.sp,
                    color = NavBarText
                )
            }

            // Question Renderer
            currentQuestion?.let { question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UniversalQuestionRenderer(
                            question = question,
                            answer = selectedAnswer,
                            onAnswerChange = { newAnswer ->
                                answersMap[question.id] = newAnswer
                                viewModel.updateAnswer(question.id, newAnswer)
                            },
                            accentColor = Color(0xFF3B82F6)
                        )
                    }
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
                        when {
                            currentQuestionIndex > 0 -> currentQuestionIndex--
                            currentQuestionGroupIndex > 0 -> {
                                currentQuestionGroupIndex--
                                currentQuestionIndex = (questionGroups.getOrNull(currentQuestionGroupIndex - 1)?.questions?.size ?: 1) - 1
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavBarText),
                    enabled = (currentQuestionIndex > 0 || currentQuestionGroupIndex > 0) && !sectionState.isSubmitting && timeRemaining > 0
                ) {
                    Text("Câu trước", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        when {
                            !isLastQuestion -> currentQuestionIndex++
                            !isLastQuestionGroup -> {
                                currentQuestionGroupIndex++
                                currentQuestionIndex = 0
                            }
                            !isLastSectionGroup -> {
                                audioPlayer.stop()
                                hasPlayedOnce = false
                                currentSectionGroupIndex++
                                currentQuestionGroupIndex = 0
                                currentQuestionIndex = 0
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart,
                        disabledContainerColor = Color(0xFF9CA3AF)
                    ),
                    enabled = !isAtVeryEnd && !sectionState.isSubmitting && timeRemaining > 0
                ) {
                    Text(
                        text = when {
                            isAtVeryEnd -> "Hoàn thành"
                            isLastQuestion && isLastQuestionGroup -> "Section tiếp"
                            isLastQuestion -> "Nhóm tiếp"
                            else -> "Câu tiếp"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun parseAndDisplayContent(content: String, metadata: Map<String, Any>?) {
    val contentType = metadata?.get("contentType") as? String
    
    // Parse data outside of composable calls
    val words = runCatching {
        if (contentType == "word_list") {
            content.trim()
                .removePrefix("[").removeSuffix("]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } else emptyList()
    }.getOrElse { emptyList() }
    
    val options = runCatching {
        if (contentType == "options_list") {
            val optionPattern = """"key"\s*:\s*"([^"]+)"\s*,\s*"value"\s*:\s*"([^"]+)"""".toRegex()
            optionPattern.findAll(content).map { match ->
                val (key, value) = match.destructured
                "$key - $value"
            }.toList()
        } else emptyList()
    }.getOrElse { emptyList() }
    
    // Now display based on parsed data
    when {
        words.isNotEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Word Box",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF854D0E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = words.joinToString("   •   "),
                        fontSize = 13.sp,
                        color = Color(0xFF713F12),
                        lineHeight = 20.sp
                    )
                }
            }
        }
        options.isNotEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Options",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    options.forEach { option ->
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
        contentType == null && content.isNotBlank() && !content.startsWith("[") -> {
            Text(
                text = content,
                fontSize = 13.sp,
                color = NavBarText
            )
        }
    }
}

