package com.example.lingora_fe.user.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.util.DateFormatHelper
import androidx.compose.foundation.layout.FlowRow
import com.example.lingora_fe.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPracticeScreen(
    navController: NavController
) {
    val viewModel: ExamViewModel = hiltViewModel()
    val state by viewModel.listState.collectAsState()
    val attemptsState by viewModel.attemptsState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadExams()
        } else {
            viewModel.loadAttempts()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện đề thi",
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
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.06f),
                            GradientEnd.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Luyện đề thi",
                    isSelected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Lịch sử làm bài",
                    isSelected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab == 0) {
                val listState = rememberLazyListState()
                LaunchedEffect(listState, state.exams.size, state.currentPage, state.totalPages, state.isLoading) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { lastVisibleIndex ->
                            if (lastVisibleIndex != null && lastVisibleIndex >= state.exams.size - 3) {
                                if (state.currentPage < state.totalPages && !state.isLoading) {
                                    viewModel.loadExams(state.currentPage + 1)
                                }
                            }
                        }
                }

                when {
                    state.isLoading && state.exams.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    state.error != null && state.exams.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.refreshExams() }) { Text("Thử lại") }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.exams) { exam ->
                                TestCard(
                                    title = exam.title,
                                    subtitle = exam.code,
                                    duration = exam.sections.sumOf { it.durationSeconds ?: 0 }.let { seconds ->
                                        val h = seconds / 3600
                                        val m = (seconds % 3600) / 60
                                        if (h > 0) "${h}h ${m}m" else "${m}m"
                                    },
                                    badge = exam.examType.value,
                                    skills = exam.sections.map { it.sectionType.value },
                                    onClick = { navController.navigate("practice/test/${exam.id}") }
                                )
                            }

                            if (state.error != null) {
                                item { Text(text = state.error!!, color = Color.Red) }
                            }

                            if (state.isLoading && state.exams.isNotEmpty()) {
                                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                            }
                        }
                    }
                }
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(listState, attemptsState.attempts.size, attemptsState.currentPage, attemptsState.totalPages, attemptsState.isLoading) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { lastVisibleIndex ->
                            if (lastVisibleIndex != null && lastVisibleIndex >= attemptsState.attempts.size - 3) {
                                if (attemptsState.currentPage < attemptsState.totalPages && !attemptsState.isLoading) {
                                    viewModel.loadAttempts(attemptsState.currentPage + 1)
                                }
                            }
                        }
                }
                when {
                    attemptsState.isLoading && attemptsState.attempts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    attemptsState.error != null && attemptsState.attempts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(text = attemptsState.error!!, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.loadAttempts(page = 1) }) { Text("Thử lại") }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(attemptsState.attempts) { attempt ->
                                val sectionType = attempt.scoreSummary?.sections?.firstOrNull()?.sectionType
                                AttemptCard(
                                    attemptId = attempt.id,
                                    examTitle = state.exams.find { it.id == attempt.examId }?.title ?: "",
                                    mode = attempt.mode.value,
                                    sectionType = sectionType?.value,
                                    status = attempt.status,
                                    startedAt = attempt.startedAt?.let { DateFormatHelper.formatChatTimestamp(it, includeDate = true) } ?: "",
                                    onClick = { navController.navigate(Route.attemptDetail(attempt.id)) }
                                )
                            }

                            if (attemptsState.error != null) {
                                item { Text(text = attemptsState.error!!, color = Color.Red) }
                            }

                            if (attemptsState.isLoading && attemptsState.attempts.isNotEmpty()) {
                                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttemptCard(
    attemptId: Int,
    examTitle: String,
    mode: String,
    sectionType: String?,
    status: String,
    startedAt: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title line
            Text(
                text = examTitle.ifEmpty { "Attempt #$attemptId" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            // Row: Mode • (SectionType if any)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Chế độ: $mode", fontSize = 13.sp, color = NavBarText)

                if (mode != "FULL" && !sectionType.isNullOrBlank()) {
                    Text(text = "• $sectionType", fontSize = 13.sp, color = NavBarText)
                }
            }

            // Row dưới: Status
            Text(
                text = "Trạng thái: $status",
                fontSize = 13.sp,
                color = NavBarText
            )

            // Started time
            if (startedAt.isNotBlank()) {
                Text(
                    text = "Bắt đầu: $startedAt",
                    fontSize = 12.sp,
                    color = NavBarText
                )
            }
        }
    }
}


@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GradientStart else Color.White,
        border = if (isSelected) null else BorderStroke(1.5.dp, GradientStart.copy(alpha = 0.3f)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else GradientStart
            )
        }
    }
}

@Composable
fun TestCard(
    title: String,
    subtitle: String,
    duration: String,
    badge: String,
    skills: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainText,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                }
            }

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = NavBarText,
                lineHeight = 20.sp
            )

            // Duration chip - horizontal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = duration,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9333EA)
                        )
                    }
                }
            }

            // Skills
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0x1A000000))
                    ) {
                        Text(
                            text = skill,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = NavBarText
                        )
                    }
                }
            }

            // Start Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Bắt đầu làm bài",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
