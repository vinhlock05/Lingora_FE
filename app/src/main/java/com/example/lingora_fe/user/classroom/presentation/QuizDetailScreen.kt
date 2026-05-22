package com.example.lingora_fe.user.classroom.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val snackbarHostState = remember { SnackbarHostState() }
    var questionToDelete by remember { mutableStateOf<ClassroomQuizQuestion?>(null) }

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

                    if (state.isTeacher) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Câu hỏi (${quiz.questions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MainText
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        navController.navigate(
                                            com.example.lingora_fe.navigation.Route.quizAttempts(
                                                classroomId = state.classroomId,
                                                quizId = quiz.id.toString()
                                            )
                                        )
                                    }
                                ) {
                                    Text("Kết quả HS")
                                }
                                TextButton(onClick = { viewModel.showImportStudySetDialog() }) {
                                    Text("Nhập từ học liệu")
                                }
                            }
                        }

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
                                        onDelete = { questionToDelete = question }
                                    )
                                }
                            }
                        }
                    } else {
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

    if (state.showAddQuestionDialog) {
        AddQuestionDialog(
            state = state,
            onQuestionTypeChange = { viewModel.onQuestionTypeChange(it) },
            onQuestionTextChange = { viewModel.onQuestionTextChange(it) },
            onQuestionOptionChange = { idx, value -> viewModel.onQuestionOptionItemChange(idx, value) },
            onAddOption = { viewModel.addQuestionOption() },
            onRemoveOption = { viewModel.removeQuestionOption(it) },
            onCorrectAnswerChange = { viewModel.onCorrectAnswerChange(it) },
            onExplanationChange = { viewModel.onExplanationChange(it) },
            onSave = { viewModel.saveQuestion() },
            onDismiss = { viewModel.hideAddQuestionDialog() }
        )
    }

    if (state.showImportStudySetDialog) {
        ImportStudySetDialog(
            state = state,
            onStudySetSelected = { viewModel.onStudySetSelected(it) },
            onImport = { viewModel.importFromStudySet() },
            onDismiss = { viewModel.hideImportStudySetDialog() }
        )
    }

    questionToDelete?.let { question ->
        DeleteConfirmDialog(
            title = "Xóa câu hỏi?",
            message = "Bạn có chắc muốn xóa câu hỏi này?\nHành động này không thể hoàn tác.",
            onConfirm = {
                viewModel.deleteQuestion(question.id)
                questionToDelete = null
            },
            onDismiss = { questionToDelete = null }
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
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ClassroomColors.BrandSoftSurface
                    ) {
                        Text(
                            text = question.type.value,
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.BrandPrimaryStrong,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa câu hỏi",
                            modifier = Modifier.size(18.dp),
                            tint = ClassroomColors.BrandPrimaryStrong
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
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
                    text = "Lựa chọn: ${question.options.take(2).joinToString(", ")}${if (question.options.size > 2) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Đáp án đúng:",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextMuted
                )
                Text(
                    text = question.correctAnswer ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.BrandPrimaryStrong,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionDialog(
    state: QuizDetailState,
    onQuestionTypeChange: (QuizType) -> Unit,
    onQuestionTextChange: (String) -> Unit,
    onQuestionOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
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
        containerColor = ClassroomColors.ScreenBackground,
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ClassroomColors.BrandSoftSurface
            ) {
                Icon(
                    imageVector = if (state.editingQuestion != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = ClassroomColors.BrandPrimaryStrong,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (state.editingQuestion != null) "Chỉnh sửa Câu Hỏi" else "Thêm Câu Hỏi Mới",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Question type section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LOẠI CÂU HỎI",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuizType.entries.forEach { type ->
                            val isSelected = state.questionType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { onQuestionTypeChange(type) },
                                label = {
                                    Text(
                                        text = type.value,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ClassroomColors.BrandSoftSurface,
                                    selectedLabelColor = ClassroomColors.BrandPrimaryStrong
                                ),
                                border = if (isSelected)
                                    BorderStroke(1.5.dp, ClassroomColors.BrandPrimary)
                                else
                                    BorderStroke(1.dp, ClassroomColors.NeutralBorder)
                            )
                        }
                    }
                }

                // Question text
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NỘI DUNG CÂU HỎI",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    OutlinedTextField(
                        value = state.questionText,
                        onValueChange = onQuestionTextChange,
                        label = { Text("Câu hỏi *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = brandFieldColors,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Options section for multiple choice
                if (state.questionType == QuizType.MULTIPLE_CHOICE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CÁC LỰA CHỌN",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                        state.questionOptions.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            color = ClassroomColors.BrandSoftSurface,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + index).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ClassroomColors.BrandPrimaryStrong
                                    )
                                }
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { onQuestionOptionChange(index, it) },
                                    label = { Text("Lựa chọn ${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = brandFieldColors,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                IconButton(
                                    onClick = { onRemoveOption(index) },
                                    enabled = state.questionOptions.size > 2,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Xóa lựa chọn",
                                        tint = if (state.questionOptions.size > 2)
                                            ClassroomColors.Danger
                                        else
                                            ClassroomColors.TextPlaceholder,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        TextButton(
                            onClick = onAddOption,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = ClassroomColors.BrandPrimaryStrong
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm lựa chọn", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // True/False selector
                if (state.questionType == QuizType.TRUE_FALSE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ĐÁP ÁN ĐÚNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("True", "False").forEach { value ->
                                val isSelected = state.correctAnswer.equals(value, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onCorrectAnswerChange(value) },
                                    label = { Text(value, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ClassroomColors.BrandSoftSurface,
                                        selectedLabelColor = ClassroomColors.BrandPrimaryStrong
                                    ),
                                    border = if (isSelected)
                                        BorderStroke(1.5.dp, ClassroomColors.BrandPrimary)
                                    else
                                        BorderStroke(1.dp, ClassroomColors.NeutralBorder)
                                )
                            }
                        }
                    }
                }

                // Correct answer field (for multiple choice and short answer)
                if (state.questionType != QuizType.TRUE_FALSE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (state.questionType == QuizType.SHORT_ANSWER) "ĐÁP ÁN MẪU" else "ĐÁP ÁN ĐÚNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClassroomColors.TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                        OutlinedTextField(
                            value = state.correctAnswer,
                            onValueChange = onCorrectAnswerChange,
                            label = {
                                Text(
                                    if (state.questionType == QuizType.SHORT_ANSWER)
                                        "Đáp án mẫu"
                                    else
                                        "Nhập chính xác một lựa chọn"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = brandFieldColors,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Explanation
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GIẢI THÍCH (TÙY CHỌN)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    OutlinedTextField(
                        value = state.explanation,
                        onValueChange = onExplanationChange,
                        label = { Text("Giải thích đáp án") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        colors = brandFieldColors,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.questionText.isNotBlank() && !state.isSavingQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.isSavingQuestion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu", fontWeight = FontWeight.SemiBold)
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
    var searchQuery by remember(state.showImportStudySetDialog, state.studySetOptions) {
        mutableStateOf("")
    }
    val filteredStudySets = remember(searchQuery, state.studySetOptions) {
        state.studySetOptions.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClassroomColors.ScreenBackground,
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ClassroomColors.BrandSoftSurface
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = ClassroomColors.BrandPrimaryStrong,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nhập từ Học Liệu", fontWeight = FontWeight.Bold)
                if (!state.isLoadingStudySets && state.studySetOptions.isNotEmpty()) {
                    Text(
                        text = "${state.studySetOptions.size} học liệu",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Tìm học liệu...", color = ClassroomColors.TextPlaceholder) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ClassroomColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Xóa",
                                    tint = ClassroomColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ClassroomColors.BrandPrimary,
                        unfocusedBorderColor = ClassroomColors.NeutralBorder,
                        cursorColor = ClassroomColors.BrandPrimary
                    )
                )

                when {
                    state.isLoadingStudySets -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = ClassroomColors.BrandPrimary)
                                Text(
                                    "Đang tải học liệu...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassroomColors.TextMuted
                                )
                            }
                        }
                    }

                    filteredStudySets.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = ClassroomColors.TextPlaceholder,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = if (state.studySetOptions.isEmpty())
                                        "Chưa có học liệu nào"
                                    else
                                        "Không tìm thấy kết quả",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassroomColors.TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredStudySets.size) { index ->
                                val option = filteredStudySets[index]
                                val isSelected = state.selectedStudySetId == option.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = isSelected,
                                            onClick = { onStudySetSelected(option.id) },
                                            role = Role.RadioButton
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            ClassroomColors.BrandSoftSurface
                                        else
                                            ClassroomColors.CardSurface
                                    ),
                                    border = if (isSelected)
                                        BorderStroke(1.5.dp, ClassroomColors.BrandPrimary)
                                    else
                                        BorderStroke(1.dp, ClassroomColors.NeutralBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onStudySetSelected(option.id) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = ClassroomColors.BrandPrimary,
                                                unselectedColor = ClassroomColors.TextMuted
                                            )
                                        )
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) ClassroomColors.BrandPrimaryStrong else MainText,
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
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Nhập câu hỏi", fontWeight = FontWeight.SemiBold)
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
    val hasNoQuestions = quiz.questions.isEmpty()
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
                text = when {
                    hasNoQuestions -> "Chưa có câu hỏi"
                    isLimitReached -> "Hết lượt làm bài"
                    else -> "Sẵn sàng bắt đầu?"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    hasNoQuestions || isLimitReached -> ClassroomColors.Danger
                    else -> MainText
                }
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

        if (hasNoQuestions) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ClassroomColors.DangerSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bài kiểm tra này chưa có câu hỏi nào. Vui lòng chờ giáo viên thêm câu hỏi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassroomColors.Danger,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (isLimitReached) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ClassroomColors.DangerSoft),
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
            enabled = !isLimitReached && !hasNoQuestions,
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

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = ClassroomColors.DangerSoft
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ClassroomColors.Danger,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ClassroomColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.Danger,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Xóa", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ClassroomColors.NeutralBorder)
            ) {
                Text("Hủy", color = ClassroomColors.TextSecondary)
            }
        }
    )
}
