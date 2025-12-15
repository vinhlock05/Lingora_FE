package com.example.lingora_fe.user.exam.presentation

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.exam.presentation.components.UniversalQuestionRenderer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPracticeScreen(
    navController: NavController,
    testId: String,
    sectionId: Int,
    attemptId: Int? = null
) {
    val examId = testId.toIntOrNull() ?: 0
    val viewModel: ExamViewModel = hiltViewModel()
    val sectionState by viewModel.sectionState.collectAsState()
    
    LaunchedEffect(sectionId, attemptId) { 
        viewModel.loadSection(examId, sectionId)
        attemptId?.let { viewModel.setExistingAttemptId(it) }
    }
    
    // NEW STRUCTURE: Section -> SectionGroup (Passage) -> QuestionGroup -> Questions
    val sectionGroups = sectionState.section?.groups ?: emptyList() // Passages
    var currentPassageIndex by remember(sectionState.section) { mutableStateOf(0) }
    val currentPassage = sectionGroups.getOrNull(currentPassageIndex)
    val passageContent = currentPassage?.content ?: ""
    
    // Get all question groups from current passage
    val questionGroups = currentPassage?.questionGroups ?: emptyList()
    var currentQuestionGroupIndex by remember(currentPassageIndex) { mutableStateOf(0) }
    val currentQuestionGroup = questionGroups.getOrNull(currentQuestionGroupIndex)
    
    // Get questions from current question group
    val questions = currentQuestionGroup?.questions ?: emptyList()
    var currentQuestionIndex by remember(currentQuestionGroupIndex) { mutableStateOf(0) }
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    
    // Store answers for all questions
    val answersMap = remember { mutableStateMapOf<Int, Any?>() }
    val selectedAnswer = currentQuestion?.let { answersMap[it.id] }
    
    // UI State
    var showPassage by remember { mutableStateOf(true) }
    var showExitWarningDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showSubmitSuccessDialog by remember { mutableStateOf(false) }
    
    // Timer
    val sectionDuration = sectionState.section?.durationSeconds ?: 60 * 60
    var timeRemaining by remember(sectionDuration) { mutableStateOf(sectionDuration) }
    var autoSubmitted by remember { mutableStateOf(false) }
    
    // Stats
    val answeredInCurrentQuestionGroup = questions.count { q -> answersMap[q.id] != null }
    val totalQuestionsInPassage = questionGroups.sumOf { it.questions.size }
    val answeredInPassage = questionGroups.flatMap { it.questions }.count { q -> answersMap[q.id] != null }
    
    // Navigation flags
    val isLastPassage = currentPassageIndex >= sectionGroups.size - 1
    val isLastQuestionGroup = currentQuestionGroupIndex >= questionGroups.size - 1
    val isLastQuestion = currentQuestionIndex >= questions.size - 1
    val isAtVeryEnd = isLastPassage && isLastQuestionGroup && isLastQuestion
    
    // Timer countdown
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        } else {
            if (!autoSubmitted) {
                autoSubmitted = true
                viewModel.submitCurrentSection(examId)
            }
        }
    }
    
    // Handle submit success
    LaunchedEffect(sectionState.message) {
        if (sectionState.message != null && !sectionState.isSubmitting) {
            showSubmitSuccessDialog = true
        }
    }
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    
    BackHandler { showExitWarningDialog = true }
    
    // Exit Warning Dialog (Back button)
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
    
    // Submit Success Dialog
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
            }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Passage Navigation: <- Passage Title ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPassageIndex > 0) {
                            currentPassageIndex--
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentPassageIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous passage",
                        tint = if (currentPassageIndex > 0) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentPassage?.title ?: "Passage ${currentPassageIndex + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$answeredInPassage/$totalQuestionsInPassage câu đã làm",
                        fontSize = 12.sp,
                        color = NavBarText
                    )
                }
                
                IconButton(
                    onClick = {
                        if (currentPassageIndex < sectionGroups.size - 1) {
                            currentPassageIndex++
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentPassageIndex < sectionGroups.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next passage",
                        tint = if (currentPassageIndex < sectionGroups.size - 1) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Passage progress dots
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
                                    idx == currentPassageIndex -> Color(0xFF10B981)
                                    idx < currentPassageIndex -> Color(0xFF10B981).copy(alpha = 0.5f)
                                    else -> Color(0xFFE5E7EB)
                                }
                            )
                    )
                }
            }

            // Passage Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showPassage = !showPassage },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF10B981))
                ) {
                    Icon(
                        imageVector = if (showPassage) Icons.Default.VisibilityOff else Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showPassage) "Ẩn bài đọc" else "Hiện bài đọc",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Passage Content - Fixed height with internal scroll
            if (showPassage && passageContent.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp), // Fixed height
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = "Reading Passage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Scrollable passage text
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = passageContent,
                                fontSize = 15.sp,
                                color = MainText,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            // Question Group Header
            currentQuestionGroup?.let { qGroup ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
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
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "$answeredInCurrentQuestionGroup/${questions.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF10B981)
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
                        
                        // Display word list or options
                        qGroup.content?.let { content ->
                            parseAndDisplayReadingContent(content, qGroup.metadata)
                        }
                        
                        // Question group tabs
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
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFFE5E7EB),
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
                    text = "Passage ${currentPassageIndex + 1}/${sectionGroups.size}",
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
                            accentColor = Color(0xFF10B981)
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
                            !isLastPassage -> {
                                currentPassageIndex++
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
                            isLastQuestion && isLastQuestionGroup -> "Passage tiếp"
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
private fun parseAndDisplayReadingContent(content: String, metadata: Map<String, Any>?) {
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
    }
}

