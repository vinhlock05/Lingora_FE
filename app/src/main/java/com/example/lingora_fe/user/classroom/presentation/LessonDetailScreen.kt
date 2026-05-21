package com.example.lingora_fe.user.classroom.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.presentation.components.*
import com.example.lingora_fe.user.classroom.util.LessonAttachmentRole

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
        containerColor = ClassroomColors.ScreenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết bài học",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.isTeacher) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.showAddAttachmentDialog() },
                        containerColor = ClassroomColors.CardSurface,
                        contentColor = ClassroomColors.BrandPrimaryStrong
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Thêm attachment")
                    }
                    FloatingActionButton(
                        onClick = { viewModel.showAddFlashcardDialog() },
                        containerColor = ClassroomColors.BrandPrimary,
                        contentColor = Color.White
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm flashcard")
                    }
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
                            color = ClassroomColors.Danger
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClassroomColors.BrandPrimary,
                                contentColor = Color.White
                            )
                        ) { Text("Thử lại") }
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
                    // ── Lesson info card ──────────────────────────────────────
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
                                text = lesson.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )

                            if (!lesson.description.isNullOrEmpty()) {
                                Text(
                                    text = lesson.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassroomColors.TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(lesson.lessonType.displayName) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = ClassroomColors.BrandSoftSurface,
                                        labelColor = ClassroomColors.BrandPrimaryStrong
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(
                                        enabled = true,
                                        borderColor = ClassroomColors.BrandPrimary
                                    )
                                )
                                if (lesson.isPublished) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Công khai") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = ClassroomColors.PublicChipBackground,
                                            labelColor = ClassroomColors.PublicChipText
                                        ),
                                        border = AssistChipDefaults.assistChipBorder(
                                            enabled = true,
                                            borderColor = ClassroomColors.BrandPrimary
                                        )
                                    )
                                }
                            }

                            if (!lesson.content.isNullOrEmpty()) {
                                Divider(
                                    color = ClassroomColors.NeutralBorder,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "Nội dung:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ClassroomColors.TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = lesson.content ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClassroomColors.TextSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // ── Attachments section ───────────────────────────────────
                    val inlineAttachments   = lesson.attachments.filter { it.role == LessonAttachmentRole.INLINE }
                    val downloadAttachments = lesson.attachments.filter { it.role == LessonAttachmentRole.DOWNLOAD }

                    if (lesson.attachments.isNotEmpty() || state.isTeacher) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tài liệu đính kèm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MainText
                            )
                            if (lesson.attachments.isNotEmpty()) {
                                Surface(shape = CircleShape, color = ClassroomColors.BrandSoftSurface) {
                                    Text(
                                        text = "${lesson.attachments.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ClassroomColors.BrandPrimaryStrong,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (lesson.attachments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.isTeacher) "Chưa có tài liệu. Nhấn 📎 để thêm."
                                           else "Bài học này chưa có tài liệu đính kèm.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClassroomColors.TextMuted
                                )
                            }
                        } else {
                            if (inlineAttachments.isNotEmpty()) {
                                AttachmentSectionHeader(
                                    icon = Icons.Default.PlayArrow,
                                    label = "Xem trong app",
                                    count = inlineAttachments.size
                                )
                                inlineAttachments.forEach { attachment ->
                                    AttachmentItem(
                                        attachment = attachment,
                                        isTeacher = state.isTeacher,
                                        onDelete = { viewModel.confirmDeleteAttachment(attachment) }
                                    )
                                }
                            }
                            if (downloadAttachments.isNotEmpty()) {
                                AttachmentSectionHeader(
                                    icon = Icons.Default.Download,
                                    label = "Tài liệu tải về",
                                    count = downloadAttachments.size
                                )
                                downloadAttachments.forEach { attachment ->
                                    AttachmentItem(
                                        attachment = attachment,
                                        isTeacher = state.isTeacher,
                                        onDelete = { viewModel.confirmDeleteAttachment(attachment) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ── Flashcards section ────────────────────────────────────
                    if (state.isTeacher) {
                        Text(
                            text = "Flashcards (${lesson.flashcards.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MainText,
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
                                items(lesson.flashcards) { flashcard ->
                                    FlashcardItem(
                                        flashcard = flashcard,
                                        onEdit    = { viewModel.editFlashcard(flashcard) },
                                        onDelete  = { viewModel.deleteFlashcard(flashcard.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Student mode: pager
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

    // ── Dialogs / Bottom sheets ───────────────────────────────────────────────

    if (state.showAddFlashcardDialog) {
        AddFlashcardDialog(
            state          = state,
            onFrontChange   = { viewModel.onFlashcardFrontChange(it) },
            onBackChange    = { viewModel.onFlashcardBackChange(it) },
            onExampleChange = { viewModel.onFlashcardExampleChange(it) },
            onSave          = { viewModel.saveFlashcard() },
            onDismiss       = { viewModel.hideAddFlashcardDialog() }
        )
    }

    if (state.showImportStudySetDialog) {
        ImportStudySetDialog(
            state              = state,
            onStudySetSelected = { viewModel.onStudySetSelected(it) },
            onImport           = { viewModel.importFromStudySet() },
            onDismiss          = { viewModel.hideImportStudySetDialog() }
        )
    }

    if (state.showAddAttachmentDialog) {
        val filePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri -> uri?.let { viewModel.onFilePicked(it) } }

        val currentInlineCount = state.lesson?.attachments
            ?.count { it.role == LessonAttachmentRole.INLINE } ?: 0
        val currentTotalCount  = state.lesson?.attachments?.size ?: 0

        AddAttachmentBottomSheet(
            state               = state,
            currentInlineCount  = currentInlineCount,
            currentTotalCount   = currentTotalCount,
            onPickFile          = { filePicker.launch("*/*") },
            onTitleChange       = { viewModel.onAttachmentTitleChange(it) },
            onRoleChange        = { viewModel.onAttachmentRoleChange(it) },
            onSave              = { viewModel.saveAttachment() },
            onDismiss           = { viewModel.hideAddAttachmentDialog() }
        )
    }

    state.attachmentToDelete?.let { attachment ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteAttachment() },
            title = { Text("Xóa tài liệu", fontWeight = FontWeight.SemiBold) },
            text  = { Text("Bạn có chắc muốn xóa \"${attachment.title ?: attachment.fileName}\"?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAttachment() },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = ClassroomColors.Danger,
                        contentColor   = Color.White
                    )
                ) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteAttachment() }) { Text("Hủy") }
            }
        )
    }
}
