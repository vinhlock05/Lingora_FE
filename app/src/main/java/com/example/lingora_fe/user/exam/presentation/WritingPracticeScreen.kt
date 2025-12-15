package com.example.lingora_fe.user.exam.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import com.example.lingora_fe.core.ui.components.FocusComponent
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.exam.domain.model.QuestionOptionsParser
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingPracticeScreen(
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
    
    // NEW STRUCTURE: Section -> SectionGroup (Task) -> QuestionGroup -> Questions
    val tasks = sectionState.section?.groups ?: emptyList() // Writing Tasks
    var currentTaskIndex by remember(sectionState.section) { mutableStateOf(0) }
    val currentTask = tasks.getOrNull(currentTaskIndex)
    
    // Get question groups from current task
    val questionGroups = currentTask?.questionGroups ?: emptyList()
    var currentQuestionGroupIndex by remember(currentTaskIndex) { mutableStateOf(0) }
    val currentQuestionGroup = questionGroups.getOrNull(currentQuestionGroupIndex)
    
    // Get questions from current question group 
    val questions = currentQuestionGroup?.questions ?: emptyList()
    val currentQuestion = questions.firstOrNull()
    
    // Essay state - store by task
    val essayByTask = remember { mutableStateMapOf<Int, String>() }
    var essayText by remember { mutableStateOf("") }
    
    // Sync essay text when task changes
    LaunchedEffect(currentTaskIndex) {
        val taskId = currentTask?.id
        if (taskId != null) {
            essayText = essayByTask[taskId] ?: ""
        }
    }
    
    // Get word requirements
    val minWords = currentQuestion?.metadata?.let { 
        QuestionOptionsParser.getMinWordCount(it) 
    } ?: 150
    val wordCount = if (essayText.trim().isEmpty()) 0 else essayText.trim().split("\\s+".toRegex()).size
    
    // UI State
    var showExitWarningDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showSubmitSuccessDialog by remember { mutableStateOf(false) }
    
    // Timer
    val sectionDuration = sectionState.section?.durationSeconds ?: 60 * 60
    var timeRemaining by remember(sectionDuration) { mutableStateOf(sectionDuration) }
    var autoSubmitted by remember { mutableStateOf(false) }
    
    // Navigation flags
    val isLastTask = currentTaskIndex >= tasks.size - 1
    
    // Timer countdown
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        } else {
            if (!autoSubmitted) {
                autoSubmitted = true
                currentTask?.id?.let { essayByTask[it] = essayText }
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
                    text = "Bài viết của bạn sẽ không được lưu. Bạn có chắc chắn muốn thoát?",
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
                    Text("Tiếp tục viết", color = GradientStart, fontWeight = FontWeight.Medium)
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
                        currentTask?.id?.let { essayByTask[it] = essayText }
                        viewModel.submitCurrentSection(examId)
                    },
                    enabled = !sectionState.isSubmitting
                ) {
                    Text("Nộp bài", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Tiếp tục viết", color = NavBarText)
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
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    FocusComponent {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Task Navigation: <- Task Title ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentTaskIndex > 0) {
                            currentTask?.id?.let { essayByTask[it] = essayText }
                            currentTaskIndex--
                            currentQuestionGroupIndex = 0
                        }
                    },
                    enabled = currentTaskIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous task",
                        tint = if (currentTaskIndex > 0) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentTask?.title ?: "Task ${currentTaskIndex + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    val hasContent = (essayByTask[currentTask?.id] ?: "").isNotBlank() || essayText.isNotBlank()
                    Text(
                        text = if (hasContent) "Đã viết • $wordCount từ" else "Chưa viết",
                        fontSize = 12.sp,
                        color = if (hasContent) GradientStart else NavBarText
                    )
                }
                
                IconButton(
                    onClick = {
                        if (currentTaskIndex < tasks.size - 1) {
                            currentTask?.id?.let { essayByTask[it] = essayText }
                            currentTaskIndex++
                            currentQuestionGroupIndex = 0
                        }
                    },
                    enabled = currentTaskIndex < tasks.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next task",
                        tint = if (currentTaskIndex < tasks.size - 1) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Task progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                tasks.forEachIndexed { idx, task ->
                    val taskHasContent = (essayByTask[task.id] ?: "").isNotBlank()
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    idx == currentTaskIndex -> GradientStart
                                    taskHasContent -> GradientStart.copy(alpha = 0.5f)
                                    else -> Color(0xFFE5E7EB)
                                }
                            )
                    )
                }
            }

                // Task Description / Instructions
                currentTask?.description?.let { desc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GradientStart,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Instructions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GradientStart
                                )
                            }
                            Text(
                                text = desc,
                                fontSize = 14.sp,
                                color = NavBarText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Question / Prompt
                currentQuestion?.let { question ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = GradientStart,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Question",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GradientStart
                                )
                            }
                            
                            Text(
                                text = question.prompt,
                                fontSize = 15.sp,
                                color = MainText,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                
                // Task Image (for Part 1 charts/graphs if available) - Below Question
                currentTask?.resourceUrl?.let { imageUrl ->
                    if (imageUrl.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = GradientStart,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Chart / Graph",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GradientStart
                                    )
                                }
                                coil.compose.AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Task image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 150.dp, max = 300.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // Word Count Indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (wordCount >= minWords) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Số từ đã viết",
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                            Text(
                                text = "$wordCount từ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wordCount >= minWords) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Yêu cầu tối thiểu",
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                            Text(
                                text = "$minWords từ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavBarText
                            )
                        }
                    }
                }

                // Essay Input
                OutlinedTextField(
                    value = essayText,
                    onValueChange = { newText ->
                        essayText = newText
                        // Update answer in viewModel
                        currentQuestion?.let { q ->
                            viewModel.updateAnswer(q.id, newText)
                        }
                        // Auto-save to map
                        currentTask?.id?.let { essayByTask[it] = newText }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    placeholder = {
                        Text(
                            text = "Viết bài của bạn ở đây...",
                            color = NavBarText.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // optional: đóng bàn phím
                            defaultKeyboardAction(ImeAction.Done)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
