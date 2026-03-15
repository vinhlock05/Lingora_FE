package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lingora_fe.user.classroom.domain.model.*

val mockLessons = listOf(
    ClassroomLesson("1", "Lesson 1: Introduction to IELTS Speaking", "Overview of Part 1, 2, 3", isLocked = false, isCompleted = true, isPublished = true, type = "VIDEO"),
    ClassroomLesson("2", "Lesson 2: Speaking Part 1 Practice", "Common topics and vocabulary", isLocked = false, isCompleted = false, isPublished = true, type = "PRACTICE"),
    ClassroomLesson("3", "Lesson 3: Advanced Vocabulary for Speaking", "Idioms and collocations", isLocked = true, isCompleted = false, isPublished = false, type = "READING")
)

val mockMembers = listOf(
    ClassroomMember("1", "Nguyễn Văn A", "OWNER", joinedAt = "01/01/2026"),
    ClassroomMember("2", "Student A", "STUDENT", joinedAt = "05/01/2026"),
    ClassroomMember("3", "Student B", "STUDENT", joinedAt = "10/01/2026")
)

val mockDiscussions = listOf(
    ClassroomDiscussionPost("1", "Student A", "Có ai có tài liệu Speaking band 7.0 không ạ?", messageType = "TEXT", attachmentUrl = null, timeAgo = "2 giờ trước", replyCount = 3),
    ClassroomDiscussionPost("2", "Ms. Hằng", "Nhắc nhở nộp bài tập về nhà Tuần 1", messageType = "FILE", attachmentUrl = "https://example.com/homework.pdf", timeAgo = "5 giờ trước", replyCount = 10)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomDetailScreen(
    classroomId: String,
    navController: NavController
) {
    val classroom = mockClassrooms.find { it.id == classroomId } ?: mockClassrooms.first()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Bài học", "Thành viên", "Thảo luận")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = classroom.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF81C784)),
                contentAlignment = Alignment.Center
            ) {
                Text("Cover Image", color = Color.White)
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> LessonsTab(mockLessons)
                    1 -> MembersTab(mockMembers)
                    2 -> DiscussionTab(mockDiscussions)
                }
            }
        }
    }
}

@Composable
fun LessonsTab(lessons: List<ClassroomLesson>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(lessons) { lesson ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (lesson.isLocked) Color(0xFFF5F5F5) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = lesson.title, fontWeight = FontWeight.Bold, color = if (lesson.isLocked) Color.Gray else Color.Black, modifier = Modifier.weight(1f))
                        if (!lesson.isPublished) {
                            Text("Bản nháp", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = lesson.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = lesson.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        if (lesson.isCompleted) {
                            Text("Hoàn thành", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium)
                        } else if (lesson.isLocked) {
                            Text("Đã khóa", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MembersTab(members: List<ClassroomMember>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(members) { member ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.first().toString())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = member.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (member.role == "OWNER") "Người tạo" else "Học viên",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (member.role == "OWNER") MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                member.joinedAt?.let {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DiscussionTab(posts: List<ClassroomDiscussionPost>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(posts) { post ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(post.authorName.first().toString())
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = post.authorName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = post.timeAgo, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
                    
                    if (post.messageType == "FILE" && post.attachmentUrl != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "📎 Đính kèm: ${post.attachmentUrl.substringAfterLast("/")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${post.replyCount} phản hồi",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
