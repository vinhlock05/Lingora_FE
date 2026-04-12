package com.example.lingora_fe.user.classroom.presentation

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    navController: NavController,
    viewModel: LessonDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            if (state.lesson != null) {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bài học") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (state.isTeacher) {
                        IconButton(onClick = { viewModel.showImportStudySetDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import từ StudySet"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isTeacher) {
                FloatingActionButton(
                    onClick = { viewModel.showAddFlashcardDialog() }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm flashcard")
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
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.lesson == null -> {
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
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }

            state.lesson != null -> {
                val lesson = state.lesson
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Lesson info section
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
                                text = lesson.title,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            if (!lesson.description.isNullOrEmpty()) {
                                Text(
                                    text = lesson.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(lesson.lessonType.displayName) }
                                )

                                if (lesson.isPublished) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Công khai") }
                                    )
                                }
                            }

                            if (!lesson.content.isNullOrEmpty()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(
                                    text = "Nội dung:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = lesson.content ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Flashcards section
                    if (state.isTeacher) {
                        Text(
                            text = "Flashcards (${lesson.flashcards.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (lesson.flashcards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có flashcard nào. Nhấn + để thêm.",
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
                                items(lesson.flashcards) { flashcard ->
                                    FlashcardItem(
                                        flashcard = flashcard,
                                        onEdit = { viewModel.editFlashcard(flashcard) },
                                        onDelete = { viewModel.deleteFlashcard(flashcard.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Student Mode: Pager View
                        if (lesson.flashcards.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Bài học này chưa có nội dung học tập.")
                            }
                        } else {
                            LessonStudyPager(flashcards = lesson.flashcards)
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
                    Text("Không tìm thấy bài học")
                }
            }
        }
    }

    // Flashcard dialog
    if (state.showAddFlashcardDialog) {
        AddFlashcardDialog(
            state = state,
            onFrontChange = { viewModel.onFlashcardFrontChange(it) },
            onBackChange = { viewModel.onFlashcardBackChange(it) },
            onExampleChange = { viewModel.onFlashcardExampleChange(it) },
            onSave = { viewModel.saveFlashcard() },
            onDismiss = { viewModel.hideAddFlashcardDialog() }
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
fun FlashcardItem(
    flashcard: ClassroomFlashcard,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flashcard.frontText,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa flashcard",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa flashcard",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = flashcard.backText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (flashcard.example != null && flashcard.example.isNotEmpty()) {
                Text(
                    text = "Ví dụ: ${flashcard.example}",
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
fun AddFlashcardDialog(
    state: LessonDetailState,
    onFrontChange: (String) -> Unit,
    onBackChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.editingFlashcard != null) "Chỉnh sửa Flashcard"
                else "Thêm Flashcard Mới"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.flashcardFront,
                    onValueChange = onFrontChange,
                    label = { Text("Mặt trước *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.flashcardBack,
                    onValueChange = onBackChange,
                    label = { Text("Mặt sau *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.flashcardExample,
                    onValueChange = onExampleChange,
                    label = { Text("Ví dụ (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.flashcardFront.isNotBlank() &&
                         state.flashcardBack.isNotBlank() &&
                         !state.isSavingFlashcard
            ) {
                if (state.isSavingFlashcard) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Lưu")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSavingFlashcard
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ImportStudySetDialog(
    state: LessonDetailState,
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

@Composable
fun LessonStudyPager(flashcards: List<ClassroomFlashcard>) {
    val pagerState = rememberPagerState(pageCount = { flashcards.size })
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp
        ) { page ->
            FlippableFlashcard(flashcard = flashcards[page])
        }
        
        Text(
            text = "${pagerState.currentPage + 1} / ${flashcards.size}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FlippableFlashcard(flashcard: ClassroomFlashcard) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            }
            .clickable { rotated = !rotated },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Front Side
            Card(
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = flashcard.frontText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // Back Side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = flashcard.backText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (!flashcard.example.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ví dụ:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = flashcard.example,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
