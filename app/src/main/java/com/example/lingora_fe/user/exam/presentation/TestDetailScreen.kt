package com.example.lingora_fe.user.exam.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(
    navController: NavController,
    testId: String
) {
    val viewModel: ExamViewModel = hiltViewModel()
    val state by viewModel.detailState.collectAsState()
    val fullTestSubmitted by viewModel.fullTestSubmitted.collectAsState()
    val examId = testId.toIntOrNull()
    LaunchedEffect(examId) { examId?.let { viewModel.loadExamDetail(it) } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.exam?.title ?: "Exam",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Test Overview Card
            Card(
                modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = Color(0xFF3B82F6), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Duration chip - horizontal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF3E8FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.sections.sumOf { it.durationSeconds ?: 0 }.let { seconds ->
                                        val h = seconds / 3600
                                        val m = (seconds % 3600) / 60
                                        if (h > 0) "${h}h ${m}m" else "${m} phút"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF9333EA)
                                )
                            }
                        }
                    }
                    val canStartFull = state.sections.size >= 2 && state.attemptId == null
                    if (canStartFull) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val id = state.exam?.id ?: return@Button
                                    viewModel.startFullAttempt(id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent // QUAN TRỌNG
                                ),
                                contentPadding = PaddingValues() // bỏ padding để gradient ôm sát
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    if (state.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Bắt đầu Full test")
                                    }

                                }
                            }

                        }
                    }
                    if (state.message != null) {
                        Text(text = state.message!!)
                    }
                }
            }

            var fullFlowStarted by remember { mutableStateOf(false) }
            var exitFullFlowDialog by remember { mutableStateOf(false) }
            
            // Use ViewModel state for completed sections - persists across navigation
            val completedSections by viewModel.completedSections.collectAsState()

            LaunchedEffect(state.attemptId) {
                if (state.attemptId != null) {
                    fullFlowStarted = true
                }
            }
            
            // Check for completed section every time the screen is shown
            val currentBackStackEntry = navController.currentBackStackEntry
            LaunchedEffect(currentBackStackEntry) {
                val id = currentBackStackEntry?.savedStateHandle?.get<Int>("completedSectionId")
                if (id != null) {
                    viewModel.markSectionCompleted(id)
                    currentBackStackEntry.savedStateHandle.remove<Int>("completedSectionId")
                }
            }
            
            // Submit Success Dialog
            if (fullTestSubmitted) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { 
                        Text(
                            text = "🎉 Hoàn thành bài thi!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ) 
                    },
                    text = { 
                        Text(
                            text = "Bạn đã hoàn thành tất cả các phần của bài thi. Kết quả sẽ được tính toán và hiển thị trong lịch sử làm bài.",
                            color = NavBarText
                        ) 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                fullFlowStarted = false
                                viewModel.clearCompletedSections()
                                viewModel.clearDetailMessage()
                            }
                        ) {
                            Text(
                                text = "Xem kết quả",
                                color = GradientStart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (fullFlowStarted && state.sections.size >= 2) {
                Text(text = "Full Test", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MainText)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.sections.forEachIndexed { idx, section ->
                            // Check ViewModel state
                            val isDone = section.id in completedSections
                            val isNext = state.sections.firstOrNull { 
                                it.id !in completedSections
                            }?.id == section.id
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isDone) Color(0xFFF0FDF4) else if (isNext) Color(0xFFFEF3C7) else Color(0xFFF9FAFB),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Number badge
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (isDone) Color(0xFF10B981) else if (isNext) Color(0xFFF59E0B) else Color(0xFFE5E7EB),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${idx + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNext) Color.White else NavBarText
                                            )
                                        }
                                    }
                                    Text(
                                        text = section.sectionType.value,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MainText
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isDone) "Hoàn thành" else "Chưa làm",
                                        fontSize = 12.sp,
                                        color = if (isDone) Color(0xFF10B981) else NavBarText
                                    )
                                    
                                    // Gradient Button "Làm"
                                    Button(
                                        onClick = {
                                            if (isNext) {
                                                val examIdStr = (state.exam?.id ?: 0).toString()
                                                val fullAttemptId = state.attemptId // Pass attemptId for FULL mode
                                                when (section.sectionType.value) {
                                                    "LISTENING" -> navController.navigate(
                                                        Route.listeningPractice(examIdStr, section.id, attemptId = fullAttemptId, isPractice = false)
                                                    )
                                                    "READING" -> navController.navigate(
                                                        Route.readingPractice(examIdStr, section.id, attemptId = fullAttemptId)
                                                    )
                                                    "WRITING" -> navController.navigate(
                                                        Route.writingPractice(examIdStr, section.id, attemptId = fullAttemptId)
                                                    )
                                                    "SPEAKING" -> navController.navigate(
                                                        Route.speakingPractice(examIdStr, section.id, attemptId = fullAttemptId)
                                                    )
                                                }
                                            }
                                        },
                                        enabled = isNext,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            disabledContainerColor = Color(0xFFE5E7EB)
                                        ),
                                        contentPadding = PaddingValues(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    brush = if (isNext) Brush.linearGradient(
                                                        colors = listOf(GradientStart, GradientEnd)
                                                    ) else Brush.linearGradient(
                                                        colors = listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Làm",
                                                color = if (isNext) Color.White else NavBarText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val allDone = state.sections.all { it.id in completedSections }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Outlined Thoát button
                            OutlinedButton(
                                onClick = { exitFullFlowDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text("Thoát", fontWeight = FontWeight.Medium)
                            }
                            
                            // Gradient Hoàn thành button
                            Button(
                                onClick = {
                                    viewModel.submitAttemptFinal()
                                    
                                          },
                                enabled = allDone,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color(0xFFE5E7EB)
                                ),
                                contentPadding = PaddingValues(),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = if (allDone) Brush.linearGradient(
                                                colors = listOf(GradientStart, GradientEnd)
                                            ) else Brush.linearGradient(
                                                colors = listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Hoàn thành",
                                        color = if (allDone) Color.White else NavBarText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        
                        if (state.isSubmitting) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                        if (state.message != null) {
                            Text(
                                text = state.message!!,
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (exitFullFlowDialog) {
                    AlertDialog(
                        onDismissRequest = { exitFullFlowDialog = false },
                        title = {
                            Text(
                                text = "Thoát chế độ Full test?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = "Thoát sẽ mất tiến độ đang làm. Bạn có chắc chắn muốn thoát không?",
                                color = NavBarText
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    exitFullFlowDialog = false
                                    fullFlowStarted = false
                                    viewModel.abortFullAttempt()
                                    viewModel.clearCompletedSections()
                                }
                            ) {
                                Text(
                                    text = "Thoát",
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { exitFullFlowDialog = false }) {
                                Text(
                                    text = "Tiếp tục",
                                    color = GradientStart,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            } else {
                Text(
                    text = "Chọn kỹ năng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )

                state.sections.forEach { section ->
                    val icon = when (section.sectionType.value) {
                        "LISTENING" -> Icons.Default.Headset
                        "READING" -> Icons.Default.MenuBook
                        "WRITING" -> Icons.Default.Edit
                        else -> Icons.Default.Description
                    }
                    val iconColor = when (section.sectionType.value) {
                        "LISTENING" -> Color(0xFF3B82F6)
                        "READING" -> Color(0xFF10B981)
                        "WRITING" -> Color(0xFF9333EA)
                        else -> Color(0xFF6366F1)
                    }
                    val iconBg = when (section.sectionType.value) {
                        "LISTENING" -> Color(0xFFDCECFE)
                        "READING" -> Color(0xFFD1FAE5)
                        "WRITING" -> Color(0xFFF3E8FF)
                        else -> Color(0xFFE0E7FF)
                    }
                    val duration = section.durationSeconds?.let { s ->
                        val m = s / 60
                        "${m} phút"
                    } ?: ""
                    val isCompleted = section.status == "COMPLETED"
                    SkillCard(
                        icon = icon,
                        iconColor = iconColor,
                        iconBackgroundColor = iconBg,
                        title = section.sectionType.value,
                        subtitle = section.title ?: "",
                        duration = duration,
                        isCompleted = isCompleted,
                        onClick = {
                            val examIdStr = (state.exam?.id ?: 0).toString()
                            when (section.sectionType.value) {
                                "LISTENING" -> navController.navigate(
                                    Route.listeningPractice(examIdStr, section.id)
                                )
                                "READING" -> navController.navigate(
                                    Route.readingPractice(examIdStr, section.id)
                                )
                                "WRITING" -> navController.navigate(
                                    Route.writingPractice(examIdStr, section.id)
                                )
                                "SPEAKING" -> navController.navigate(
                                    Route.speakingPractice(examIdStr, section.id)
                                )
                            }
                        }
                    )
                }
            }
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            if (state.error != null) {
                Text(text = state.error!!, color = Color.Red)
            }
        }
    }
}

@Composable
fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = NavBarText
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MainText
        )
    }
}

@Composable
fun SkillCard(
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    duration: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⏱ $duration",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

