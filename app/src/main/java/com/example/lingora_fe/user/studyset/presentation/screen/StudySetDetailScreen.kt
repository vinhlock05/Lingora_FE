package com.example.lingora_fe.user.studyset.presentation.screen

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetDetailViewModel
import com.example.lingora_fe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetDetailScreen(
    onBackClick: () -> Unit,
    onStartFlashcard: (Int) -> Unit,
    onStartQuiz: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteSuccess: () -> Unit = {},
    viewModel: StudySetDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Calculate isOwner based on current state - only check when studySet is loaded
    val currentUserId = remember { viewModel.getCurrentUserId() }
    val isOwner = remember(uiState.studySet, currentUserId) {
        val studySet = uiState.studySet
        currentUserId != null && studySet != null && studySet.owner.id == currentUserId
    }

    LaunchedEffect(Unit) {
        viewModel.loadStudySet()
    }
    
    Log.d("StudySetDetailScreen", "isOwner: $isOwner, studySet: ${uiState.studySet?.id}, currentUserId: $currentUserId")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết học liệu") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (isOwner && uiState.studySet != null) {
                        IconButton(onClick = { onEditClick(uiState.studySet!!.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.studySet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.studySet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            uiState.studySet != null -> {
                val studySet = uiState.studySet!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Study Set Info Card
                    StudySetInfoCard(studySet = studySet)

                    // Learning Mode Selection
                    Text(
                        text = "Chọn chế độ học",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )

                    // Flashcard Option
                    LearningModeCard(
                        icon = R.drawable.ic_flashcard,
                        title = "Flashcard",
                        description = "Học từ vựng qua thẻ ghi nhớ",
                        buttonText = "Bắt đầu Flashcard",
                        onClick = { onStartFlashcard(studySet.id) },
                        enabled = studySet.flashcards.isNotEmpty()
                    )

                    // Quiz Option
                    LearningModeCard(
                        icon = R.drawable.ic_quiz,
                        title = "Quiz",
                        description = "Kiểm tra kiến thức với câu hỏi trắc nghiệm",
                        buttonText = "Bắt đầu Quiz",
                        onClick = { onStartQuiz(studySet.id) },
                        enabled = studySet.quizzes.isNotEmpty()
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa học liệu") },
            text = { Text("Bạn có chắc chắn muốn xóa học liệu này? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteStudySet {
                            onDeleteSuccess()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun StudySetInfoCard(
    studySet: com.example.lingora_fe.user.studyset.domain.model.StudySet
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = studySet.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            if (!studySet.description.isNullOrBlank()) {
                Text(
                    text = studySet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavBarText
                )
            }

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChip(text = "${studySet.flashcards.size} thẻ")
                TagChip(text = "${studySet.quizzes.size} câu hỏi")
            }

            // Owner and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tác giả: ${studySet.owner.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
                Text(
                    text = "${studySet.likeCount} lượt thích",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
            }
        }
    }
}

@Composable
private fun LearningModeCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = NavBarText
                )
            }
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Text(buttonText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF0369A1),
            fontSize = 12.sp
        )
    }
}


