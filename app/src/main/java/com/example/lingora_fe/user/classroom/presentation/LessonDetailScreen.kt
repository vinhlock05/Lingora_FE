package com.example.lingora_fe.user.classroom.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.presentation.components.ClassroomColors
import com.example.lingora_fe.util.FileUploadHelper
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUploadingImage by remember { mutableStateOf(false) }
    var flashcardToDelete by remember { mutableStateOf<ClassroomFlashcard?>(null) }

    // SRT file picker — used in both "add new" and "edit existing" subtitle flows
    val srtPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.parseSrtFromUri(it) } }

    LaunchedEffect(state.isSavingSubtitle) {
        if (!state.isSavingSubtitle && state.successMessage != null) {
            snackbarHostState.showSnackbar(state.successMessage)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            if (state.lesson != null) {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { success ->
            snackbarHostState.showSnackbar(success)
            viewModel.clearSuccess()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
                val inlineAttachments = lesson.attachments.filter {
                    it.role == com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.INLINE &&
                    (it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO ||
                     it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO ||
                     it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.IMAGE)
                }
                val inlineDocAttachments = lesson.attachments.filter {
                    it.role == com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.INLINE &&
                    it.fileType != com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO &&
                    it.fileType != com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO &&
                    it.fileType != com.example.lingora_fe.user.classroom.util.LessonAttachmentType.IMAGE
                }
                val downloadAttachments = lesson.attachments.filter {
                    it.role == com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.DOWNLOAD
                }
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val hasMedia = inlineAttachments.isNotEmpty()
                val tabs = remember(hasMedia, lesson.flashcards.size) {
                    mutableListOf<String>().apply {
                        if (hasMedia) add("Bài giảng")
                        add("Tài liệu")
                        add("Từ vựng")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Top-level TabRow
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = ClassroomColors.BrandPrimaryStrong,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = ClassroomColors.BrandPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        color = if (selectedTabIndex == index) ClassroomColors.BrandPrimaryStrong else ClassroomColors.TextMuted,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Render content based on selected tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val activeTabTitle = tabs.getOrNull(selectedTabIndex) ?: ""
                        when (activeTabTitle) {
                            "Bài giảng" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    val activeIndex = if (inlineAttachments.isNotEmpty()) {
                                        state.currentInlineIndex.coerceIn(0, inlineAttachments.lastIndex)
                                    } else 0
                                    val activeAttachment = inlineAttachments[activeIndex]

                                    InlineCarouselContainer(
                                        inlineAttachments = inlineAttachments,
                                        currentIndex = activeIndex,
                                        onIndexChange = { index -> viewModel.onInlineIndexChange(index) }
                                    ) { attachment ->
                                        when (attachment.fileType) {
                                            com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO -> {
                                                VideoPlayerView(
                                                    videoUrl = attachment.fileUrl,
                                                    exoPlayer = viewModel.exoPlayer,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(16f / 9f)
                                                )
                                            }
                                            com.example.lingora_fe.user.classroom.util.LessonAttachmentType.IMAGE -> {
                                                AsyncImage(
                                                    model = attachment.fileUrl,
                                                    contentDescription = attachment.title ?: attachment.fileName,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 320.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            else -> {
                                                PodcastPlayerView(
                                                    audioUrl = attachment.fileUrl,
                                                    exoPlayer = viewModel.exoPlayer,
                                                    title = attachment.title ?: attachment.fileName,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Teacher: action row
                                    if (state.isTeacher) {
                                        val isMediaType = activeAttachment.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO ||
                                            activeAttachment.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO
                                        val hasSubtitle = !activeAttachment.subtitlesJson.isNullOrBlank()
                                        var showSubtitleMenu by remember { mutableStateOf(false) }
                                        var showSubtitleOptionsMenu by remember { mutableStateOf(false) }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8F8F8))
                                                .padding(horizontal = 12.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Delete button (left)
                                            IconButton(
                                                onClick = { viewModel.confirmDeleteAttachment(activeAttachment) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Xóa",
                                                    tint = ClassroomColors.Danger,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Subtitle controls (right) — only for video/audio
                                            if (isMediaType) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    if (state.isTranscribing) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(14.dp),
                                                            strokeWidth = 2.dp,
                                                            color = ClassroomColors.BrandPrimary
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            "Đang tạo...",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = ClassroomColors.BrandPrimary
                                                        )
                                                    } else if (!hasSubtitle) {
                                                        // ── No subtitle: one button → 2-option dropdown ──
                                                        Box {
                                                            TextButton(
                                                                onClick = { showSubtitleMenu = true },
                                                                enabled = !state.isSavingSubtitle
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Subtitles,
                                                                    null,
                                                                    Modifier.size(16.dp),
                                                                    tint = ClassroomColors.BrandPrimaryStrong
                                                                )
                                                                Spacer(Modifier.width(4.dp))
                                                                Text(
                                                                    "Thêm phụ đề",
                                                                    style = MaterialTheme.typography.labelMedium,
                                                                    color = ClassroomColors.BrandPrimaryStrong
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = showSubtitleMenu,
                                                                onDismissRequest = { showSubtitleMenu = false }
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Upload file SRT") },
                                                                    leadingIcon = { Icon(Icons.Default.FileUpload, null, Modifier.size(18.dp)) },
                                                                    onClick = {
                                                                        showSubtitleMenu = false
                                                                        viewModel.prepareSubtitleEdit(activeAttachment)
                                                                        srtPicker.launch("*/*")
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("Tạo tự động bằng AI") },
                                                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)) },
                                                                    onClick = {
                                                                        showSubtitleMenu = false
                                                                        viewModel.transcribeExistingAttachment(activeAttachment)
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        // ── Has subtitle: edit button + small options ──
                                                        TextButton(
                                                            onClick = { viewModel.startEditSubtitle(activeAttachment) },
                                                            enabled = !state.isSavingSubtitle
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Subtitles,
                                                                null,
                                                                Modifier.size(16.dp),
                                                                tint = ClassroomColors.BrandPrimaryStrong
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                "Chỉnh sửa phụ đề",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = ClassroomColors.BrandPrimaryStrong
                                                            )
                                                        }
                                                        Box {
                                                            IconButton(
                                                                onClick = { showSubtitleOptionsMenu = true },
                                                                modifier = Modifier.size(32.dp),
                                                                enabled = !state.isSavingSubtitle
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.MoreVert,
                                                                    null,
                                                                    Modifier.size(18.dp),
                                                                    tint = ClassroomColors.TextSecondary
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = showSubtitleOptionsMenu,
                                                                onDismissRequest = { showSubtitleOptionsMenu = false }
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Upload file SRT mới") },
                                                                    leadingIcon = { Icon(Icons.Default.FileUpload, null, Modifier.size(18.dp)) },
                                                                    onClick = {
                                                                        showSubtitleOptionsMenu = false
                                                                        viewModel.prepareSubtitleEdit(activeAttachment)
                                                                        srtPicker.launch("*/*")
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("Tạo lại bằng AI") },
                                                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)) },
                                                                    onClick = {
                                                                        showSubtitleOptionsMenu = false
                                                                        viewModel.transcribeExistingAttachment(activeAttachment)
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Interactive Subtitles — only for video/audio
                                    val isMediaAttachment = activeAttachment.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO ||
                                        activeAttachment.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO
                                    if (isMediaAttachment) {
                                        var currentPosition by remember { mutableStateOf(0L) }
                                        LaunchedEffect(viewModel.exoPlayer) {
                                            while (true) {
                                                if (viewModel.exoPlayer.isPlaying) {
                                                    currentPosition = viewModel.exoPlayer.currentPosition
                                                }
                                                kotlinx.coroutines.delay(250)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(Color.White)
                                        ) {
                                            InteractiveScriptView(
                                                subtitlesJson = activeAttachment.subtitlesJson,
                                                currentPosition = currentPosition,
                                                onSeekTo = { seekMs ->
                                                    viewModel.exoPlayer.seekTo(seekMs)
                                                    currentPosition = seekMs
                                                },
                                                exoPlayer = viewModel.exoPlayer,
                                                wordRepository = viewModel.wordRepository
                                            )
                                        }
                                    }
                                }
                            }

                            "Tài liệu" -> {
                                if (state.isTeacher) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 80.dp), // Extra padding for FAB
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            LessonInfoCard(lesson = lesson)
                                        }
                                        if (inlineDocAttachments.isNotEmpty()) {
                                            item {
                                                AttachmentSectionHeader(
                                                    icon = Icons.Default.Description,
                                                    label = "Tài liệu học tập (Xem trong app)",
                                                    count = inlineDocAttachments.size
                                                )
                                            }
                                            items(inlineDocAttachments) { attachment ->
                                                AttachmentItem(
                                                    attachment = attachment,
                                                    isTeacher = true,
                                                    onDelete = { viewModel.confirmDeleteAttachment(attachment) }
                                                )
                                            }
                                        }
                                        if (downloadAttachments.isNotEmpty()) {
                                            item {
                                                AttachmentSectionHeader(
                                                    icon = Icons.Default.Download,
                                                    label = "Tài liệu tải về",
                                                    count = downloadAttachments.size
                                                )
                                            }
                                            items(downloadAttachments) { attachment ->
                                                AttachmentItem(
                                                    attachment = attachment,
                                                    isTeacher = true,
                                                    onDelete = { viewModel.confirmDeleteAttachment(attachment) }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val scrollState = rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(scrollState)
                                    ) {
                                        LessonInfoCard(lesson = lesson)
                                        if (inlineDocAttachments.isNotEmpty()) {
                                            AttachmentSectionHeader(
                                                icon = Icons.Default.Description,
                                                label = "Tài liệu học tập (Xem trong app)",
                                                count = inlineDocAttachments.size
                                            )
                                            inlineDocAttachments.forEach { attachment ->
                                                AttachmentItem(
                                                    attachment = attachment,
                                                    isTeacher = false,
                                                    onDelete = {}
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
                                                    isTeacher = false,
                                                    onDelete = {}
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            "Từ vựng" -> {
                                if (state.isTeacher) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 80.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Flashcards (${lesson.flashcards.size})",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MainText
                                                )
                                            }
                                        }

                                        if (lesson.flashcards.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Chưa có flashcard nào. Nhấn nút + để thêm.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = ClassroomColors.TextMuted
                                                    )
                                                }
                                            }
                                        } else {
                                            items(lesson.flashcards) { flashcard ->
                                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                                    FlashcardItem(
                                                        flashcard = flashcard,
                                                        onEdit = { viewModel.editFlashcard(flashcard) },
                                                        onDelete = { flashcardToDelete = flashcard }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
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

    if (state.showAddFlashcardDialog) {
        AddFlashcardDialog(
            state = state,
            isUploadingImage = isUploadingImage,
            onFrontChange = { viewModel.onFlashcardFrontChange(it) },
            onBackChange = { viewModel.onFlashcardBackChange(it) },
            onExampleChange = { viewModel.onFlashcardExampleChange(it) },
            onSave = { localImageUri, removeExistingImage ->
                scope.launch {
                    if (localImageUri != null) {
                        isUploadingImage = true
                        FileUploadHelper.uploadImage(
                            context = context,
                            imageUri = localImageUri,
                            folder = "lingora/classroom/flashcards"
                        ).fold(
                            ifLeft = {
                                isUploadingImage = false
                                snackbarHostState.showSnackbar("Tải ảnh lên thất bại")
                            },
                            ifRight = { uploadedUrl ->
                                isUploadingImage = false
                                viewModel.saveFlashcard(uploadedImageUrl = uploadedUrl)
                            }
                        )
                    } else {
                        viewModel.saveFlashcard(removeImage = removeExistingImage)
                    }
                }
            },
            onDismiss = { viewModel.hideAddFlashcardDialog() }
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
            onTranscribe        = { viewModel.transcribeAttachment() },
            onUploadSrt         = { srtPicker.launch("*/*") },
            onSave              = { viewModel.saveAttachment() },
            onDismiss           = { viewModel.hideAddAttachmentDialog() }
        )
    }

    if (state.showSubtitleEditor && state.editorSubtitlesJson != null && state.lesson != null) {
        val inlineAttachments = state.lesson.attachments.filter {
            it.role == com.example.lingora_fe.user.classroom.util.LessonAttachmentRole.INLINE &&
            (it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO ||
             it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.AUDIO ||
             it.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.IMAGE)
        }
        val activeIndex = if (inlineAttachments.isNotEmpty()) {
            state.currentInlineIndex.coerceIn(0, inlineAttachments.lastIndex)
        } else 0
        val activeAttachment = inlineAttachments.getOrNull(activeIndex)

        if (activeAttachment != null) {
            SubtitleEditorDialog(
                initialSubtitlesJson = state.editorSubtitlesJson,
                exoPlayer = viewModel.exoPlayer,
                attachment = activeAttachment,
                onSave = { finalizedJson ->
                    viewModel.saveEditorSubtitles(finalizedJson)
                },
                onDismiss = {
                    viewModel.hideSubtitleEditor()
                }
            )
        }
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

    flashcardToDelete?.let { flashcard ->
        DeleteConfirmDialog(
            title = "Xóa flashcard?",
            message = "Bạn có chắc muốn xóa flashcard \"${flashcard.frontText}\"?\nHành động này không thể hoàn tác.",
            onConfirm = {
                viewModel.deleteFlashcard(flashcard.id)
                flashcardToDelete = null
            },
            onDismiss = { flashcardToDelete = null }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flashcard.frontText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MainText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa flashcard",
                            modifier = Modifier.size(18.dp),
                            tint = ClassroomColors.BrandPrimaryStrong
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa flashcard",
                            modifier = Modifier.size(18.dp),
                            tint = ClassroomColors.Danger
                        )
                    }
                }
            }

            Text(
                text = flashcard.backText,
                style = MaterialTheme.typography.bodySmall,
                color = ClassroomColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (flashcard.example != null && flashcard.example.isNotEmpty()) {
                Text(
                    text = "Ví dụ: ${flashcard.example}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.BrandPrimaryStrong,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!flashcard.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = flashcard.imageUrl,
                    contentDescription = "Ảnh flashcard",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AddFlashcardDialog(
    state: LessonDetailState,
    isUploadingImage: Boolean,
    onFrontChange: (String) -> Unit,
    onBackChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onSave: (Uri?, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val brandFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClassroomColors.BrandPrimary,
        focusedLabelColor = ClassroomColors.BrandPrimaryStrong,
        cursorColor = ClassroomColors.BrandPrimary
    )
    var selectedImageUri by remember(state.showAddFlashcardDialog, state.editingFlashcard?.id) {
        mutableStateOf<Uri?>(null)
    }
    var removeExistingImage by remember(state.showAddFlashcardDialog, state.editingFlashcard?.id) {
        mutableStateOf(false)
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            removeExistingImage = false
        }
    }
    val previewImageModel: Any? = selectedImageUri
        ?: if (!removeExistingImage && state.flashcardImageUrl.isNotBlank()) state.flashcardImageUrl else null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClassroomColors.ScreenBackground,
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ClassroomColors.BrandSoftSurface
            ) {
                Icon(
                    imageVector = if (state.editingFlashcard != null) Icons.Default.Edit else Icons.Default.Add,
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
                text = if (state.editingFlashcard != null) "Chỉnh sửa Flashcard" else "Thêm Flashcard Mới",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Content section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "NỘI DUNG",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    OutlinedTextField(
                        value = state.flashcardFront,
                        onValueChange = onFrontChange,
                        label = { Text("Mặt trước *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = brandFieldColors,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = state.flashcardBack,
                        onValueChange = onBackChange,
                        label = { Text("Mặt sau *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = brandFieldColors,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = state.flashcardExample,
                        onValueChange = onExampleChange,
                        label = { Text("Ví dụ (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = brandFieldColors,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Image section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ẢNH MINH HỌA",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClassroomColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                    if (previewImageModel != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = previewImageModel,
                                contentDescription = "Ảnh flashcard",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.9f),
                                        contentColor = ClassroomColors.BrandPrimaryStrong
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Đổi ảnh",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        selectedImageUri = null
                                        removeExistingImage = true
                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.9f),
                                        contentColor = ClassroomColors.Danger
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Xóa ảnh",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, ClassroomColors.NeutralBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ClassroomColors.BrandPrimaryStrong
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    "Chọn ảnh",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedImageUri, removeExistingImage) },
                enabled = state.flashcardFront.isNotBlank() &&
                        state.flashcardBack.isNotBlank() &&
                        !state.isSavingFlashcard &&
                        !isUploadingImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.isSavingFlashcard || isUploadingImage) {
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
                enabled = !state.isSavingFlashcard && !isUploadingImage,
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
    state: LessonDetailState,
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
                    Text("Nhập flashcard", fontWeight = FontWeight.SemiBold)
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

private fun lessonTypeIcon(typeValue: String) = when (typeValue) {
    "VIDEO" -> Icons.Default.SmartDisplay
    "STUDYSET" -> Icons.Default.MenuBook
    "TEXT" -> Icons.Default.Description
    else -> Icons.Default.AutoAwesome
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
            color = ClassroomColors.TextSecondary,
            fontWeight = FontWeight.SemiBold
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
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
            }
            .clickable { rotated = !rotated },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Card(
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = ClassroomColors.CardSurface)
            ) {
                val hasImage = !flashcard.imageUrl.isNullOrBlank()
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (hasImage) {
                        AsyncImage(
                            model = flashcard.imageUrl,
                            contentDescription = "Ảnh flashcard",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .then(if (hasImage) Modifier.align(Alignment.BottomCenter) else Modifier)
                            .fillMaxWidth()
                            .then(
                                if (hasImage) Modifier.background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                ) else Modifier
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = flashcard.frontText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasImage) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = ClassroomColors.BrandSoftSurface)
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
                        fontWeight = FontWeight.SemiBold,
                        color = ClassroomColors.BrandPrimaryStrong
                    )
                    if (!flashcard.example.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ví dụ:",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClassroomColors.BrandPrimaryStrong,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = flashcard.example,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClassroomColors.TextSecondary
                        )
                    }
                }
            }
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

@Composable
fun LessonInfoCard(lesson: com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonDetail) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = lessonTypeIcon(lesson.lessonType.value),
                    contentDescription = null,
                    tint = ClassroomColors.BrandPrimaryStrong,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MainText
                )
            }

            if (!lesson.description.isNullOrEmpty()) {
                Text(
                    text = lesson.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassroomColors.TextSecondary
                )
            }

            if (!lesson.isPublished) {
                Text(
                    text = "Bản nháp",
                    color = ClassroomColors.Danger,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            } else {
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
}
