package com.example.lingora_fe.user.classroom.presentation

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.util.DateFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDetailScreen(
    classroomId: String,
    navController: NavController,
    viewModel: ClassroomDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Bài học", "Bài kiểm tra", "Thảo luận", "Thành viên")
    var menuExpanded by remember { mutableStateOf(false) }
    val isTeacher = state.currentUserId != null && state.classroom?.teacher?.id == state.currentUserId
    val fabVisible = state.selectedTab in 0..1 && isTeacher
    val fabLabel = if (state.selectedTab == 0) "Bài học mới" else "Bài kiểm tra mới"

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            navController.popBackStack()
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        // Refresh detail whenever we return to this screen (e.g. from creation/edit)
        viewModel.loadDetail()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.classroom?.name ?: "Lớp học", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (isTeacher) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    onClick = { 
                                        menuExpanded = false
                                        navController.navigate(com.example.lingora_fe.navigation.Route.createClassroomWithId(classroomId))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Lưu trữ") },
                                    onClick = { 
                                        menuExpanded = false
                                        viewModel.archiveClassroom()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa", color = Color.Red) },
                                    onClick = { 
                                        menuExpanded = false
                                        viewModel.deleteClassroom()
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (fabVisible) {
                FloatingActionButton(
                    onClick = {
                        if (state.selectedTab == 0) {
                            navController.navigate(com.example.lingora_fe.navigation.Route.createLesson(classroomId))
                        } else {
                            navController.navigate(com.example.lingora_fe.navigation.Route.createQuiz(classroomId))
                        }
                    },
                    containerColor = Color(0xFF5CB85C),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = fabLabel)
                }
            }
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
                if (state.classroom?.coverImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(state.classroom?.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh bìa lớp học",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Ảnh bìa", color = Color.White)
                }
            }

            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF5CB85C),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = Color(0xFF5CB85C)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { 
                            Text(
                                title,
                                color = if (state.selectedTab == index) Color(0xFF5CB85C) else Color.Gray,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (state.selectedTab) {
                0 -> LessonsTabContent(
                    classroomId = classroomId,
                    lessons = state.lessons,
                    isTeacher = isTeacher,
                    navController = navController,
                    onDeleteLesson = { viewModel.deleteLesson(it) }
                )

                1 -> QuizzesTabContent(
                    classroomId = classroomId,
                    quizzes = state.quizzes,
                    isTeacher = isTeacher,
                    navController = navController,
                    onDeleteQuiz = { viewModel.deleteQuiz(it) }
                )

                2 -> {
                    val context = LocalContext.current
                    ChatTabContent(
                        state = state,
                        onInputChange = { viewModel.onChatInputChange(it) },
                        onSendMessage = { viewModel.sendMessage() },
                        onReplyTo = { viewModel.setReplyingTo(it) },
                        onClearReply = { viewModel.setReplyingTo(null) },
                        onPickImage = { uri -> viewModel.uploadAttachment(context, uri) },
                        onClearAttachment = { viewModel.clearAttachment() },
                        onLoadMore = { viewModel.loadMoreChatHistory() }
                    )
                }

                3 -> MembersTabContent(
                    members = state.members,
                    teacher = state.classroom?.teacher,
                    isTeacher = isTeacher,
                    onApprove = { viewModel.approveMember(it) },
                    onKick = { viewModel.showRemoveMemberDialog(it) }
                )
            }
        }

        if (state.memberToRemove != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRemoveMemberDialog() },
                title = { Text("Loại bỏ thành viên") },
                text = { Text("Bạn có chắc chắn muốn loại bỏ ${state.memberToRemove?.user?.username ?: "người này"} khỏi lớp học không?") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmRemoveMember() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        if (state.isRemovingMember) {
                            run {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Text("Loại bỏ")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRemoveMemberDialog() }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun LessonsTabContent(
    classroomId: String,
    lessons: List<ClassroomLesson>,
    isTeacher: Boolean,
    navController: NavController,
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
            LessonCard(
                classroomId = classroomId,
                lesson = lesson,
                isTeacher = isTeacher,
                navController = navController,
                onDelete = { onDeleteLesson(lesson.id) }
            )
        }
    }
}

@Composable
private fun LessonCard(
    classroomId: String,
    lesson: ClassroomLesson,
    isTeacher: Boolean,
    navController: NavController,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(com.example.lingora_fe.navigation.Route.lessonDetail(classroomId, lesson.id.toString(), isTeacher))
            },
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
                if (isTeacher) {
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
                                text = { Text("Chỉnh sửa") },
                                onClick = { 
                                    expanded = false
                                    navController.navigate(com.example.lingora_fe.navigation.Route.createLesson(classroomId) + "?lessonId=${lesson.id}")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = { expanded = false; onDelete() }
                            )
                        }
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
                    color = Color(0xFF5CB85C)
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
    classroomId: String,
    quizzes: List<ClassroomQuiz>,
    isTeacher: Boolean,
    navController: NavController,
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
            QuizCard(
                classroomId = classroomId,
                quiz = quiz,
                isTeacher = isTeacher,
                navController = navController,
                onDelete = { onDeleteQuiz(quiz.id) }
            )
        }
    }
}

@Composable
private fun QuizCard(
    classroomId: String,
    quiz: ClassroomQuiz,
    isTeacher: Boolean,
    navController: NavController,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(com.example.lingora_fe.navigation.Route.quizDetail(classroomId, quiz.id.toString(), isTeacher))
            },
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
                if (isTeacher) {
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
                                text = { Text("Chỉnh sửa") },
                                onClick = { 
                                    expanded = false
                                    navController.navigate(com.example.lingora_fe.navigation.Route.createQuiz(classroomId) + "?quizId=${quiz.id}")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = { expanded = false; onDelete() }
                            )
                        }
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
                    text = "Điểm qua: ${(quiz.passingScore * 100).toInt()}%",
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatTabContent(
    state: ClassroomDetailState,
    onSendMessage: () -> Unit,
    onInputChange: (String) -> Unit,
    onReplyTo: (ClassroomMessage) -> Unit,
    onClearReply: () -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    onClearAttachment: () -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onPickImage(it) } }

    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    val canSend = (state.chatInput.isNotBlank() || state.pendingAttachmentUrl != null) && !state.isSendingMessage

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.chatMessages.isEmpty() && !state.isChatLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Chưa có thảo luận nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Text(
                        text = "Hãy bắt đầu cuộc hội thoại!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            } else {
                LaunchedEffect(listState) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { index ->
                            if (index <= 1 && state.chatMessages.isNotEmpty() && state.hasMoreChatMessages && !state.isChatLoadingMore) {
                                onLoadMore()
                            }
                        }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isChatLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF5CB85C))
                            }
                        }
                    }
                    
                    items(state.chatMessages, key = { it.id ?: it.hashCode() }) { message ->
                        val isMe = message.sender.id == state.currentUserId
                        MessageBubble(
                            message = message,
                            isMe = isMe,
                            onLongPress = { onReplyTo(message) }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().imePadding(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Column {
                // Reply preview bar
                if (state.replyingTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null,
                            tint = Color(0xFF5CB85C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.replyingTo.sender.username ?: "Unknown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5CB85C)
                            )
                            Text(
                                text = when (state.replyingTo.type) {
                                    ClassroomMessageType.IMAGE -> "[Hình ảnh]"
                                    ClassroomMessageType.FILE -> "[Tệp đính kèm]"
                                    else -> state.replyingTo.content
                                },
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onClearReply, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Attachment preview
                if (state.isUploadingAttachment) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF5CB85C))
                        Text("Đang tải ảnh...", fontSize = 13.sp, color = Color.Gray)
                    }
                } else if (state.pendingAttachmentUrl != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.pendingAttachmentUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Ảnh đính kèm",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ảnh đính kèm", modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color.Gray)
                        IconButton(onClick = onClearAttachment, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa ảnh", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Input row
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { attachmentPickerLauncher.launch(arrayOf("*/*")) },
                        enabled = !state.isUploadingAttachment,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Đính kèm ảnh",
                            tint = Color(0xFF5CB85C)
                        )
                    }
                    OutlinedTextField(
                        value = state.chatInput,
                        onValueChange = onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập tin nhắn...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5CB85C),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSendMessage,
                        enabled = canSend,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (canSend) Color(0xFF5CB85C) else Color.LightGray,
                                CircleShape
                            )
                    ) {
                        if (state.isSendingMessage) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Gửi",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarBox(
    size: Int = 40,
    avatarUrl: String?,
    username: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val initials = username?.firstOrNull()?.uppercase() ?: "U"
        val innerSize = (size * 0.9).toInt().dp
        
        if (!avatarUrl.isNullOrBlank() && avatarUrl != "N/A") {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = debugPlaceholder(initials, innerSize), // Fallback if image fails
                fallback = debugPlaceholder(initials, innerSize)
            )
        } else {
            InitialsCircle(initials, innerSize)
        }
    }
}

@Composable
private fun InitialsCircle(initials: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun debugPlaceholder(initials: String, size: androidx.compose.ui.unit.Dp) = 
    androidx.compose.ui.graphics.painter.ColorPainter(Color.Transparent) // This is just to satisfy AsyncImage types, InitialsCircle handles it

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ClassroomMessage,
    isMe: Boolean,
    onLongPress: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            AvatarBox(
                avatarUrl = message.sender.avatar,
                username = message.sender.username
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isMe) Color(0xFF10B981) else Color(0xFFF5F7FA),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 18.dp
                ),
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isMe) {
                        Text(
                            text = message.sender.username ?: "Unknown",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MainText
                        )
                    }

                    // Replied-to quote block
                    if (message.repliedTo != null) {
                        Surface(
                            color = if (isMe) Color(0xFF0E9A6D) else Color(0xFFE8E8E8),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(36.dp)
                                        .background(
                                            if (isMe) Color.White.copy(alpha = 0.7f)
                                            else Color(0xFF5CB85C),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = message.repliedTo.sender.username ?: "Unknown",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isMe) Color.White.copy(alpha = 0.9f) else Color(0xFF5CB85C)
                                    )
                                    Text(
                                        text = when (message.repliedTo.type) {
                                            ClassroomMessageType.IMAGE -> "[Hình ảnh]"
                                            ClassroomMessageType.FILE -> "[Tệp đính kèm]"
                                            else -> message.repliedTo.content
                                        },
                                        fontSize = 12.sp,
                                        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Message content
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 1. Render Attachment (if any)
                        if (!message.attachmentUrl.isNullOrBlank()) {
                            when (message.type) {
                                ClassroomMessageType.IMAGE -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(message.attachmentUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Hình ảnh",
                                        modifier = Modifier
                                            .widthIn(max = 220.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                                ClassroomMessageType.FILE -> {
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                if (isMe) Color(0xFF0E9A6D) else Color(0xFFE0E0E0),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = if (isMe) Color.White else Color(0xFF5CB85C),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = message.content.takeIf { it.contains(".") } ?: "Tệp đính kèm",
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else MainText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }

                        // 2. Render Text Content (if not empty or different from attachment URL)
                        if (message.content.isNotBlank() && (message.attachmentUrl == null || message.content != message.attachmentUrl)) {
                            // If it's a file but content is not the filename, or it's an image/text message
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                color = if (isMe) Color.White else MainText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            Text(
                text = DateFormatHelper.formatDateAsChatTime(message.createdAt),
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = if (isMe) 0.dp else 8.dp,
                    end = if (isMe) 8.dp else 0.dp
                )
            )
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(12.dp))
            AvatarBox(
                avatarUrl = message.sender.avatar,
                username = message.sender.username
            )
        }
    }
}

@Composable
private fun MembersTabContent(
    members: List<com.example.lingora_fe.user.classroom.domain.model.ClassroomMember>,
    teacher: com.example.lingora_fe.user.classroom.domain.model.ClassroomUser?,
    isTeacher: Boolean,
    onApprove: (Int) -> Unit,
    onKick: (com.example.lingora_fe.user.classroom.domain.model.ClassroomMember) -> Unit
) {
    val pendingMembers = members.filter { it.status.value == "PENDING" }
    val activeMembers = members.filter { 
        (it.status.value == "ACTIVE" || it.role.value == "TEACHER" || it.role.value == "ASSISTANT") && 
        (teacher == null || it.user.id != teacher.id) 
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (teacher != null) {
            item {
                Text("Giáo viên", fontWeight = FontWeight.Bold, color = Color(0xFF5CB85C))
                Spacer(modifier = Modifier.height(8.dp))
                TeacherCard(user = teacher)
            }
        }

        if (isTeacher && pendingMembers.isNotEmpty()) {
            item {
                Text("Chờ duyệt (${pendingMembers.size})", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(pendingMembers, key = { "pending_${it.id}" }) { member ->
                MemberCard(
                    member = member,
                    isTeacher = true,
                    isPending = true,
                    onApprove = { onApprove(member.id) },
                    onKick = { onKick(member) }
                )
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        }

        item {
            Text("Học sinh (${activeMembers.size})", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(activeMembers, key = { "active_${it.id}" }) { member ->
            MemberCard(
                member = member,
                isTeacher = isTeacher && member.role.value == "STUDENT",
                isPending = false,
                onApprove = {},
                onKick = { onKick(member) }
            )
        }
    }
}

@Composable
private fun TeacherCard(user: com.example.lingora_fe.user.classroom.domain.model.ClassroomUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarBox(avatarUrl = user.avatar, username = user.username)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(user.username ?: "Unknown Teacher", fontWeight = FontWeight.Bold)
                Text("Giáo viên chính", style = MaterialTheme.typography.labelMedium, color = Color(0xFF5CB85C))
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: com.example.lingora_fe.user.classroom.domain.model.ClassroomMember,
    isTeacher: Boolean,
    isPending: Boolean,
    onApprove: () -> Unit,
    onKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isPending) Color(0xFFFFF9C4) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val username = member.user.username ?: "Unknown User"
            Column(modifier = Modifier.weight(1f)) {
                Text(username, fontWeight = FontWeight.Bold)
                if (member.role.value == "ASSISTANT" || member.role.value == "TEACHER") {
                    Text("Giáo viên", style = MaterialTheme.typography.labelMedium, color = Color(0xFF5CB85C))
                } else {
                    Text(if (isPending) "Học sinh (Chờ duyệt)" else "Học sinh", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }

            if (isTeacher) {
                if (isPending) {
                    IconButton(onClick = onApprove) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Duyệt", tint = Color(0xFF5CB85C))
                    }
                }
                IconButton(onClick = onKick) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đuổi/Xóa", tint = Color.Red)
                }
            }
        }
    }
}
