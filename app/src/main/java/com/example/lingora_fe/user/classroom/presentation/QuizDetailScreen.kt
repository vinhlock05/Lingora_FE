package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.util.QuizType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    navController: NavController,
    viewModel: QuizDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value
    var expandedQuestionType by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bài kiểm tra") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showImportStudySetDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import từ StudySet"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddQuestionDialog() }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm câu hỏi")
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "Đã xảy ra lỗi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            state.quiz != null -> {
                val quiz = state.quiz
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Quiz info section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = quiz.title,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            if (!quiz.description.isNullOrEmpty()) {
                                Text(
                                    text = quiz.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (quiz.timeLimitSeconds != null) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${quiz.timeLimitSeconds} giây") }
                                    )
                                }

                                AssistChip(
                                    onClick = {},
                                    label = { Text("${quiz.passingScore}% để đạt") }
                                )

                                if (quiz.isPublished) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Công khai") }
                                    )
                                }
                            }
                        }
                    }

                    // Questions section
                    Text(
                        text = "Câu hỏi (${quiz.questions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (quiz.questions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có câu hỏi nào. Nhấn + để thêm.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quiz.questions) { question ->
                                QuestionItem(
                                    question = question,
                                    onEdit = { viewModel.editQuestion(question) },
                                    onDelete = { viewModel.deleteQuestion(question.id) }
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không tìm thấy bài kiểm tra")
                }
            }
        }
    }

    // Question dialog
    if (state.showAddQuestionDialog) {
        AddQuestionDialog(
            state = state,
            expandedQuestionType = expandedQuestionType,
            onExpandedQuestionTypeChange = { expandedQuestionType = it },
            onQuestionTypeChange = { viewModel.onQuestionTypeChange(it) },
            onQuestionTextChange = { viewModel.onQuestionTextChange(it) },
            onCorrectAnswerChange = { viewModel.onCorrectAnswerChange(it) },
            onExplanationChange = { viewModel.onExplanationChange(it) },
            onSave = { viewModel.saveQuestion() },
            onDismiss = { viewModel.hideAddQuestionDialog() }
        )
    }

    // Import from StudySet dialog
    if (state.showImportStudySetDialog) {
        ImportStudySetDialog(
            state = state,
            onStudySetSelected = { viewModel.onStudySetSelected(it) },
            onImport = { viewModel.importFromStudySet() },
            onDismiss = { viewModel.hideImportStudySetDialog() }
        )
    }
}

@Composable
fun QuestionItem(
    question: ClassroomQuizQuestion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Loại: ${question.type.value}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa câu hỏi",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa câu hỏi",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (question.options.isNotEmpty()) {
                Text(
                    text = "Tùy chọn: ${question.options.take(2).joinToString(", ")}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (question.correctAnswer != null) {
                Text(
                    text = "Câu trả lời đúng: ${question.correctAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    state: QuizDetailState,
    expandedQuestionType: Boolean,
    onExpandedQuestionTypeChange: (Boolean) -> Unit,
    onQuestionTypeChange: (QuizType) -> Unit,
    onQuestionTextChange: (String) -> Unit,
    onCorrectAnswerChange: (String) -> Unit,
    onExplanationChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.editingQuestion != null) "Chỉnh sửa Câu Hỏi"
                else "Thêm Câu Hỏi Mới"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question type dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.questionType.value,
                        onValueChange = {},
                        label = { Text("Loại câu hỏi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable { onExpandedQuestionTypeChange(true) },
                        enabled = false
                    )

                    DropdownMenu(
                        expanded = expandedQuestionType,
                        onDismissRequest = { onExpandedQuestionTypeChange(false) },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        QuizType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.value) },
                                onClick = {
                                    onQuestionTypeChange(type)
                                    onExpandedQuestionTypeChange(false)
                                }
                            )
                        }
                    }
                }

                // Question text
                OutlinedTextField(
                    value = state.questionText,
                    onValueChange = onQuestionTextChange,
                    label = { Text("Câu hỏi *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Correct answer
                OutlinedTextField(
                    value = state.correctAnswer,
                    onValueChange = onCorrectAnswerChange,
                    label = { Text("Câu trả lời đúng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Explanation
                OutlinedTextField(
                    value = state.explanation,
                    onValueChange = onExplanationChange,
                    label = { Text("Giải thích (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.questionText.isNotBlank() && !state.isSavingQuestion
            ) {
                if (state.isSavingQuestion) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Lưu")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSavingQuestion
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ImportStudySetDialog(
    state: QuizDetailState,
    onStudySetSelected: (Int) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import từ StudySet") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    state.isLoadingStudySets -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.studySetOptions.isEmpty() -> {
                        Text(
                            "Không có StudySet nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.studySetOptions.size) { index ->
                                val option = state.studySetOptions[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = state.selectedStudySetId == option.id,
                                            onClick = { onStudySetSelected(option.id) },
                                            role = Role.RadioButton
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = state.selectedStudySetId == option.id,
                                        onClick = { onStudySetSelected(option.id) }
                                    )
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onImport,
                enabled = state.selectedStudySetId != null &&
                         !state.isImporting &&
                         !state.isLoadingStudySets
            ) {
                if (state.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Import")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isImporting
            ) {
                Text("Hủy")
            }
        }
    )
}

