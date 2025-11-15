package com.example.lingora_fe.user.studyset.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.studyset.domain.model.QuizType
import com.example.lingora_fe.user.studyset.domain.model.StudySetVisibility
import com.example.lingora_fe.user.studyset.presentation.ContentTab
import com.example.lingora_fe.user.studyset.presentation.FlashcardFormItem
import com.example.lingora_fe.user.studyset.presentation.QuizFormItem
import com.example.lingora_fe.user.studyset.presentation.viewmodel.StudySetFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditStudySetScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: StudySetFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasSaved by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isLoading, uiState.error, hasSaved) {
        if (!uiState.isLoading && hasSaved) {
            if (uiState.error == null) {
                // Save was successful
                hasSaved = false
                val message = if (uiState.isEditMode) "Đã cập nhật học liệu thành công" else "Đã tạo học liệu thành công"
                snackbarHostState.showSnackbar(message)
                // Navigate back after showing snackbar
                kotlinx.coroutines.delay(1500) // Wait for snackbar to show
                onSaveSuccess()
            } else {
                // Save failed
                hasSaved = false
                snackbarHostState.showSnackbar(uiState.error ?: "Có lỗi xảy ra")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (!uiState.isEditMode) "Tạo học liệu mới" else "Chỉnh sửa học liệu")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            hasSaved = true
                            viewModel.saveStudySet(onSuccess = {})
                        },
                        enabled = !uiState.isLoading && uiState.title.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Lưu",
                                color = GradientStart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Basic Information Section
            Text(
                text = "Thông tin cơ bản",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tiêu đề học liệu") },
                placeholder = { Text("Nhập tiêu đề...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Mô tả") },
                placeholder = { Text("Mô tả nội dung học liệu...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // Paid toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Học liệu có phí",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    Text(
                        text = "Bật để bán học liệu này",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText
                    )
                }
                Switch(
                    checked = uiState.isPaid,
                    onCheckedChange = { viewModel.togglePaid() }
                )
            }

            if (uiState.isPaid) {
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.updatePrice(it) },
                    label = { Text("Giá bán (VNĐ)") },
                    placeholder = { Text("0") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Visibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VisibilityChip(
                    text = "Công khai",
                    isSelected = uiState.visibility == StudySetVisibility.PUBLIC,
                    onClick = { viewModel.updateVisibility(StudySetVisibility.PUBLIC) },
                    modifier = Modifier.weight(1f)
                )
                VisibilityChip(
                    text = "Riêng tư",
                    isSelected = uiState.visibility == StudySetVisibility.PRIVATE,
                    onClick = { viewModel.updateVisibility(StudySetVisibility.PRIVATE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            // Main Content Section
            Text(
                text = "Nội dung chính",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MainText
            )

            Text(
                text = "Tạo flashcard và quiz cho học liệu",
                style = MaterialTheme.typography.bodyMedium,
                color = NavBarText
            )

            // Content Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContentTabButton(
                    text = "Flashcard (${uiState.flashcards.size})",
                    isSelected = uiState.selectedContentTab == ContentTab.FLASHCARD,
                    onClick = { viewModel.switchContentTab(ContentTab.FLASHCARD) },
                    modifier = Modifier.weight(1f)
                )
                ContentTabButton(
                    text = "Quiz (${uiState.quizzes.size})",
                    isSelected = uiState.selectedContentTab == ContentTab.QUIZ,
                    onClick = { viewModel.switchContentTab(ContentTab.QUIZ) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Content List
            when (uiState.selectedContentTab) {
                ContentTab.FLASHCARD -> {
                    FlashcardListSection(
                        flashcards = uiState.flashcards,
                        onAdd = { viewModel.addFlashcard() },
                        onUpdate = { index, flashcard -> viewModel.updateFlashcard(index, flashcard) },
                        onRemove = { index -> viewModel.removeFlashcard(index) }
                    )
                }
                ContentTab.QUIZ -> {
                    QuizListSection(
                        quizzes = uiState.quizzes,
                        onAdd = { viewModel.addQuiz() },
                        onUpdate = { index, quiz -> viewModel.updateQuiz(index, quiz) },
                        onRemove = { index -> viewModel.removeQuiz(index) }
                    )
                }
            }

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Bottom buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hủy")
                }
                Button(
                    onClick = {
                        hasSaved = true
                        viewModel.saveStudySet(onSuccess = {})
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isLoading && uiState.title.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Lưu học liệu")
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibilityChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) GradientStart else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MainText,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ContentTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) GradientStart else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MainText,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FlashcardListSection(
    flashcards: List<FlashcardFormItem>,
    onAdd: () -> Unit,
    onUpdate: (Int, FlashcardFormItem) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Thẻ flashcard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MainText
        )
        Text(
            text = "Tạo thẻ học với mặt trước và mặt sau",
            style = MaterialTheme.typography.bodySmall,
            color = NavBarText
        )

        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+ Thêm thẻ")
        }

        flashcards.forEachIndexed { index, flashcard ->
            FlashcardFormItem(
                index = index,
                flashcard = flashcard,
                onUpdate = { updated -> onUpdate(index, updated) },
                onRemove = { onRemove(index) }
            )
        }
    }
}

@Composable
private fun FlashcardFormItem(
    index: Int,
    flashcard: FlashcardFormItem,
    onUpdate: (FlashcardFormItem) -> Unit,
    onRemove: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thẻ ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedTextField(
                value = flashcard.frontText,
                onValueChange = { onUpdate(flashcard.copy(frontText = it)) },
                label = { Text("Mặt trước") },
                placeholder = { Text("Từ vựng, câu hỏi...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = flashcard.backText,
                onValueChange = { onUpdate(flashcard.copy(backText = it)) },
                label = { Text("Mặt sau") },
                placeholder = { Text("Nghĩa, đáp án...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = flashcard.example,
                onValueChange = { onUpdate(flashcard.copy(example = it)) },
                label = { Text("Ví dụ (tùy chọn)") },
                placeholder = { Text("Câu ví dụ minh họa...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun QuizListSection(
    quizzes: List<QuizFormItem>,
    onAdd: () -> Unit,
    onUpdate: (Int, QuizFormItem) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Câu hỏi quiz",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MainText
        )
        Text(
            text = "Tạo nhiều dạng câu hỏi khác nhau",
            style = MaterialTheme.typography.bodySmall,
            color = NavBarText
        )

        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = GradientStart
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("+ Thêm câu hỏi")
        }

        quizzes.forEachIndexed { index, quiz ->
            QuizFormItem(
                index = index,
                quiz = quiz,
                onUpdate = { updated -> onUpdate(index, updated) },
                onRemove = { onRemove(index) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizFormItem(
    index: Int,
    quiz: QuizFormItem,
    onUpdate: (QuizFormItem) -> Unit,
    onRemove: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu hỏi ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Quiz Type Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when (quiz.type) {
                        QuizType.MULTIPLE_CHOICE -> "Trắc nghiệm nhiều"
                        QuizType.TRUE_FALSE -> "Đúng/Sai"
                        QuizType.SHORT_ANSWER -> "Điền vào chỗ trống"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại câu hỏi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Trắc nghiệm nhiều") },
                        onClick = {
                            onUpdate(quiz.copy(type = QuizType.MULTIPLE_CHOICE))
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Đúng/Sai") },
                        onClick = {
                            onUpdate(quiz.copy(type = QuizType.TRUE_FALSE))
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Điền vào chỗ trống") },
                        onClick = {
                            onUpdate(quiz.copy(type = QuizType.SHORT_ANSWER))
                            expanded = false
                        }
                    )
                }
            }

            OutlinedTextField(
                value = quiz.question,
                onValueChange = { onUpdate(quiz.copy(question = it)) },
                label = { Text("Câu hỏi") },
                placeholder = { Text("Nhập câu hỏi...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            when (quiz.type) {
                QuizType.MULTIPLE_CHOICE -> {
                    Text(
                        text = "Các đáp án (có thể chọn nhiều đáp án đúng)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    quiz.options.forEachIndexed { optIndex, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = option == quiz.correctAnswer,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        onUpdate(quiz.copy(correctAnswer = option))
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newValue ->
                                    val updatedOptions = quiz.options.toMutableList()
                                    updatedOptions[optIndex] = newValue
                                    onUpdate(quiz.copy(options = updatedOptions))
                                },
                                label = { Text("Đáp án ${('A' + optIndex)}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    Text(
                        text = "Tích checkbox để đánh dấu các đáp án đúng",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText,
                        fontSize = 12.sp
                    )
                }
                QuizType.TRUE_FALSE -> {
                    Text(
                        text = "Đáp án đúng",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = quiz.correctAnswer == "Đúng",
                            onClick = { onUpdate(quiz.copy(correctAnswer = "Đúng")) },
                            label = { Text("Đúng (Yes)") }
                        )
                        FilterChip(
                            selected = quiz.correctAnswer == "Sai",
                            onClick = { onUpdate(quiz.copy(correctAnswer = "Sai")) },
                            label = { Text("Sai (No)") }
                        )
                    }
                }
                QuizType.SHORT_ANSWER -> {
                    Text(
                        text = "Câu có chỗ trống",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MainText
                    )
                    OutlinedTextField(
                        value = quiz.question,
                        onValueChange = { onUpdate(quiz.copy(question = it)) },
                        label = { Text("Nhập câu và đánh dấu chỗ trống bằng [") },
                        placeholder = { Text("Sử dụng [blank] để đánh dấu chỗ trống") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Sử dụng [blank] để đánh dấu chỗ trống",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavBarText,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = quiz.correctAnswer,
                        onValueChange = { onUpdate(quiz.copy(correctAnswer = it)) },
                        label = { Text("Đáp án cho các chỗ trống") },
                        placeholder = { Text("Nhập đáp án...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

