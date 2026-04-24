package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors
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
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            if (state.quiz != null) {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        containerColor = ClassroomColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết bài kiểm tra",
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MainText
                        )
                    }
                },
                actions = {
                    if (state.isTeacher) {
                        IconButton(onClick = { viewModel.showImportStudySetDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import từ StudySet",
                                tint = ClassroomColors.BrandPrimaryStrong
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = { viewModel.showAddQuestionDialog() },
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm câu hỏi")
                }
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
                    CircularProgressIndicator(color = ClassroomColors.BrandPrimary)
                }
            }

            state.error != null && state.quiz == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Đã xảy ra lỗi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClassroomColors.Danger
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClassroomColors.BrandPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Thử lại")
                        }
                    }
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
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = quiz.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )

                            if (!quiz.description.isNullOrEmpty()) {
                                Text(
                                    text = quiz.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassroomColors.TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val brandAssistChipColors = AssistChipDefaults.assistChipColors(
                                    containerColor = ClassroomColors.BrandSoftSurface,
                                    labelColor = ClassroomColors.BrandPrimaryStrong
                                )
                                val brandAssistChipBorder = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = ClassroomColors.BrandPrimary
                                )

                                if (quiz.timeLimitSeconds != null) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${quiz.timeLimitSeconds} giây") },
                                        colors = brandAssistChipColors,
                                        border = brandAssistChipBorder
                                    )
                                }

                                AssistChip(
                                    onClick = {},
                                    label = { Text("${quiz.passingScore * 100}% để đạt") },
                                    colors = brandAssistChipColors,
                                    border = brandAssistChipBorder
                                )

                                if (quiz.isPublished) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Công khai") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = ClassroomColors.PublicChipBackground,
                                            labelColor = ClassroomColors.PublicChipText
                                        ),
                                        border = brandAssistChipBorder
                                    )
                                }
                            }
                        }
                    }

                    // Questions section (Teacher only)
                    if (state.isTeacher) {
                        Text(
                            text = "Câu hỏi (${quiz.questions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MainText,
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
                                    color = ClassroomColors.TextMuted
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
                    } else {
                        // Student Preview Mode
                        QuizOverviewContent(
                            quiz = quiz,
                            isTeacher = state.isTeacher,
                            onStart = {
                                navController.navigate(
                                    com.example.lingora_fe.navigation.Route.quizSession(
                                        classroomId = state.classroomId,
                                        quizId = quiz.id.toString()
                                    )
                                )
                            }
                        )
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface)
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
                        fontWeight = FontWeight.SemiBold,
                        color = MainText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Loại: ${question.type.value}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted
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
                            modifier = Modifier.size(18.dp),
                            tint = ClassroomColors.BrandPrimaryStrong
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
                            tint = ClassroomColors.Danger
                        )
                    }
                }
            }

            if (question.options.isNotEmpty()) {
                Text(
                    text = "Tùy chọn: ${question.options.take(2).joinToString(", ")}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (question.correctAnswer != null) {
                Text(
                    text = "Câu trả lời đúng: ${question.correctAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.BrandPrimaryStrong,
                    fontWeight = FontWeight.Medium,
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
    val brandFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClassroomColors.BrandPrimary,
        focusedLabelColor = ClassroomColors.BrandPrimaryStrong,
        cursorColor = ClassroomColors.BrandPrimary,
        disabledBorderColor = ClassroomColors.NeutralBorder,
        disabledLabelColor = ClassroomColors.TextMuted,
        disabledTextColor = MainText
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.editingQuestion != null) "Chỉnh sửa Câu Hỏi"
                else "Thêm Câu Hỏi Mới",
                fontWeight = FontWeight.SemiBold
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
                        enabled = false,
                        colors = brandFieldColors
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
                    maxLines = 4,
                    colors = brandFieldColors
                )

                // Correct answer
                OutlinedTextField(
                    value = state.correctAnswer,
                    onValueChange = onCorrectAnswerChange,
                    label = { Text("Câu trả lời đúng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = brandFieldColors
                )

                // Explanation
                OutlinedTextField(
                    value = state.explanation,
                    onValueChange = onExplanationChange,
                    label = { Text("Giải thích (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = brandFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.questionText.isNotBlank() && !state.isSavingQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                if (state.isSavingQuestion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSavingQuestion,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassroomColors.TextSecondary
                )
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
        title = { Text("Import từ StudySet", fontWeight = FontWeight.SemiBold) },
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
                            CircularProgressIndicator(color = ClassroomColors.BrandPrimary)
                        }
                    }

                    state.studySetOptions.isEmpty() -> {
                        Text(
                            "Không có StudySet nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClassroomColors.TextMuted,
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
                                        onClick = { onStudySetSelected(option.id) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = ClassroomColors.BrandPrimary,
                                            unselectedColor = ClassroomColors.TextMuted
                                        )
                                    )
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MainText,
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
                         !state.isLoadingStudySets,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                if (state.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Import")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isImporting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ClassroomColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun QuizOverviewContent(
    quiz: com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizDetail,
    isTeacher: Boolean,
    onStart: () -> Unit
) {
    val userAttempts = quiz.userAttempts ?: 0
    val maxAttempts = quiz.maxAttempts
    val isLimitReached = !isTeacher && userAttempts >= maxAttempts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = ClassroomColors.BrandSoftSurface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = ClassroomColors.BrandPrimaryStrong
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLimitReached) "Hết lượt làm bài" else "Sẵn sàng bắt đầu?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLimitReached) ClassroomColors.Danger else MainText
            )
            Text(
                text = "Bài kiểm tra này có ${quiz.questions.size} câu hỏi",
                style = MaterialTheme.typography.bodyLarge,
                color = ClassroomColors.TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Thời gian",
                value = quiz.timeLimitSeconds?.let { "${it / 60} phút" } ?: "Không giới hạn",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Lượt làm bài",
                value = if (isTeacher) "∞" else "$userAttempts / $maxAttempts",
                modifier = Modifier.weight(1f)
            )
        }

        if (!quiz.description.isNullOrEmpty()) {
            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodyMedium,
                color = ClassroomColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (isLimitReached) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ClassroomColors.DangerSoft
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bạn đã đạt giới hạn số lần làm bài cho bài kiểm tra này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassroomColors.Danger,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            enabled = !isLimitReached,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClassroomColors.BrandPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isTeacher) "Xem trước" else "Bắt đầu làm bài",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ClassroomColors.BrandSoftSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ClassroomColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ClassroomColors.BrandPrimaryStrong
            )
        }
    }
}

