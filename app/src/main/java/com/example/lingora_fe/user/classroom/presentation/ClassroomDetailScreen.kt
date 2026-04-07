package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDetailScreen(
    classroomId: String,
    navController: NavController,
    viewModel: ClassroomDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Bài học", "Bài kiểm tra", "Thảo luận")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.classroom?.name ?: "Lớp học", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.classroom == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.error != null && state.classroom == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error ?: "Đã xảy ra lỗi", color = Color.Red)
                    TextButton(onClick = { viewModel.loadDetail() }) {
                        Text("Thử lại")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF81C784)),
                contentAlignment = Alignment.Center
            ) {
                Text("Ảnh bìa", color = Color.White)
            }

            TabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (state.selectedTab) {
                0 -> LessonsTabContent(
                    lessons = state.lessons,
                    onDeleteLesson = { viewModel.deleteLesson(it) }
                )
                1 -> QuizzesTabContent(
                    quizzes = state.quizzes,
                    onDeleteQuiz = { viewModel.deleteQuiz(it) }
                )
                2 -> ChatTabContent(
                    messages = state.chatMessages,
                    chatInput = state.chatInput,
                    isChatLoading = state.isChatLoading,
                    isSendingMessage = state.isSendingMessage,
                    onInputChange = { viewModel.onChatInputChange(it) },
                    onSend = { viewModel.sendMessage() }
                )
            }
        }
    }
}

@Composable
private fun LessonsTabContent(
    lessons: List<ClassroomLesson>,
    onDeleteLesson: (Int) -> Unit
) {
    if (lessons.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có bài học nào", color = Color.Gray)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(lessons, key = { it.id }) { lesson ->
            LessonCard(lesson = lesson, onDelete = { onDeleteLesson(lesson.id) })
        }
    }
}

@Composable
private fun LessonCard(lesson: ClassroomLesson, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!lesson.isPublished) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        fontWeight = FontWeight.Bold,
                        color = if (!lesson.isPublished) Color.Gray else Color.Black
                    )
                    lesson.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tùy chọn",
                            tint = Color.Gray
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = { expanded = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (lesson.lessonType) {
                        ClassroomLessonType.VIDEO -> "Video"
                        ClassroomLessonType.STUDYSET -> "Bộ học"
                        ClassroomLessonType.TEXT -> "Văn bản"
                        ClassroomLessonType.MIXED -> "Tổng hợp"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!lesson.isPublished) {
                    Text("Bản nháp", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun QuizzesTabContent(
    quizzes: List<ClassroomQuiz>,
    onDeleteQuiz: (Int) -> Unit
) {
    if (quizzes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có bài kiểm tra nào", color = Color.Gray)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quizzes, key = { it.id }) { quiz ->
            QuizCard(quiz = quiz, onDelete = { onDeleteQuiz(quiz.id) })
        }
    }
}

@Composable
private fun QuizCard(quiz: ClassroomQuiz, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = quiz.title, fontWeight = FontWeight.Bold)
                    quiz.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tùy chọn",
                            tint = Color.Gray
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Xóa", color = Color.Red) },
                            onClick = { expanded = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                quiz.timeLimitSeconds?.let {
                    Text(
                        text = "⏱ ${it / 60} phút",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Điểm qua: ${quiz.passingScore.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                if (!quiz.isPublished) {
                    Text("Bản nháp", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun ChatTabContent(
    messages: List<ClassroomMessage>,
    chatInput: String,
    isChatLoading: Boolean,
    isSendingMessage: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                isChatLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                messages.isEmpty() -> Text(
                    text = "Chưa có tin nhắn nào",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Nhập tin nhắn...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(
                    onClick = onSend,
                    enabled = chatInput.isNotBlank() && !isSendingMessage
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (chatInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ClassroomMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            val initial = message.sender.username?.firstOrNull()?.toString() ?: "?"
            Text(initial, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender.username ?: "Ẩn danh",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            when (message.type) {
                ClassroomMessageType.TEXT -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                ClassroomMessageType.IMAGE, ClassroomMessageType.FILE -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    message.attachmentUrl?.let { url ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "📎 ${url.substringAfterLast("/")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }
    }
}
