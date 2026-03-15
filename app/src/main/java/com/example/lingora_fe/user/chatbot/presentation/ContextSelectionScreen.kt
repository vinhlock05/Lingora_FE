package com.example.lingora_fe.user.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.chatbot.domain.model.ConversationContext

val mockContexts = listOf(
    ConversationContext(
        id = 1,
        name = "Giao tiếp hàng ngày",
        slug = "daily",
        description = "Luyện các chủ đề cơ bản như chào hỏi, giới thiệu bản thân, sở thích.",
        iconUrl = null,
        systemPrompt = "You are a friendly language partner helping the user practice daily conversations.",
        difficultyLevel = "BEGINNER",
        isActive = true,
        sortOrder = 1,
        createdAt = "2026-03-01T10:00:00Z"
    ),
    ConversationContext(
        id = 2,
        name = "Du lịch",
        slug = "travel",
        description = "Giao tiếp tại sân bay, khách sạn, nhà hàng, hỏi đường.",
        iconUrl = null,
        systemPrompt = "You are a helpful travel assistant. Practice travel scenarios with the user.",
        difficultyLevel = "BEGINNER",
        isActive = true,
        sortOrder = 2,
        createdAt = "2026-03-02T10:00:00Z"
    ),
    ConversationContext(
        id = 3,
        name = "Phỏng vấn",
        slug = "interview",
        description = "Trả lời phỏng vấn xin việc, thuật lại kinh nghiệm, kỹ năng.",
        iconUrl = null,
        systemPrompt = "You are an HR manager conducting a job interview. Be professional and encouraging.",
        difficultyLevel = "INTERMEDIATE",
        isActive = true,
        sortOrder = 3,
        createdAt = "2026-03-03T10:00:00Z"
    ),
    ConversationContext(
        id = 4,
        name = "Học tập",
        slug = "study",
        description = "Thảo luận bài tập nhóm, thuyết trình, trao đổi với giáo viên.",
        iconUrl = null,
        systemPrompt = "You are a classmate or a teacher discussing academic topics.",
        difficultyLevel = "INTERMEDIATE",
        isActive = true,
        sortOrder = 4,
        createdAt = "2026-03-04T10:00:00Z"
    )
)

data class ConversationHistory(
    val id: String,
    val contextName: String,
    val date: String,
    val score: Int
)

val mockHistory = listOf(
    ConversationHistory(id = "1", contextName = "Giao tiếp hàng ngày", date = "10/03/2026", score = 85),
    ConversationHistory(id = "2", contextName = "Phỏng vấn", date = "12/03/2026", score = 92)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextSelectionScreen(
    onBack: () -> Unit,
    onContextSelected: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện hội thoại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Trở về"
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
                    text = "Luyện hội thoại",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Lịch sử hội thoại",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab == 0) {
                Text(
                    text = "Chọn ngữ cảnh luyện tập",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mockContexts) { context ->
                        ContextCard(
                            context = context,
                            onClick = { onContextSelected(context.id) }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(mockHistory) { history ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = history.contextName, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = history.date, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = "${history.score}/100",
                                    color = GradientStart,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContextCard(
    context: ConversationContext,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.name.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = context.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = context.difficultyLevel,
                style = MaterialTheme.typography.labelSmall,
                color = when (context.difficultyLevel) {
                    "BEGINNER" -> Color(0xFF4CAF50)
                    "INTERMEDIATE" -> Color(0xFFFFA000)
                    else -> Color(0xFFE53935)
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Bắt đầu", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun TabButton(
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
