package com.example.lingora_fe.user.exam.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.user.exam.presentation.viewmodel.ExamViewModel
import com.example.lingora_fe.util.AudioRecorderManager
import com.example.lingora_fe.util.RecordingInfo
import com.example.lingora_fe.util.CloudinaryUploader
import com.example.lingora_fe.util.UploadResult
import com.example.lingora_fe.util.formatDuration
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingPracticeScreen(
    navController: NavController,
    testId: String,
    sectionId: Int,
    attemptId: Int? = null
) {
    val examId = testId.toIntOrNull() ?: 0
    val viewModel: ExamViewModel = hiltViewModel()
    val sectionState by viewModel.sectionState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(sectionId, attemptId) { 
        viewModel.loadSection(examId, sectionId)
        attemptId?.let { viewModel.setExistingAttemptId(it) }
    }

    // Audio recorder
    val audioRecorder = remember { AudioRecorderManager(context) }
    val cloudinaryUploader = remember { CloudinaryUploader() }
    
    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose { audioRecorder.release() }
    }

    // NEW STRUCTURE: Section -> SectionGroup (Part) -> QuestionGroup -> Questions
    val parts = sectionState.section?.groups ?: emptyList()
    var currentPartIndex by remember(sectionState.section) { mutableStateOf(0) }
    val currentPart = parts.getOrNull(currentPartIndex)
    
    val questionGroups = currentPart?.questionGroups ?: emptyList()
    var currentQuestionGroupIndex by remember(currentPartIndex) { mutableStateOf(0) }
    val currentQuestionGroup = questionGroups.getOrNull(currentQuestionGroupIndex)
    
    val questions = currentQuestionGroup?.questions ?: emptyList()
    var currentQuestionIndex by remember(currentQuestionGroupIndex) { mutableStateOf(0) }
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    
    // Recording states per question
    val recordingsByQuestion = remember { mutableStateMapOf<Int, RecordingInfo>() }
    val uploadedUrlsByQuestion = remember { mutableStateMapOf<Int, String>() }
    
    // Current recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var currentRecordingInfo by remember { mutableStateOf<RecordingInfo?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf<String?>(null) }
    
    // Playback progress state - collect from audioRecorder
    val playbackProgress by audioRecorder.playbackProgress.collectAsState()
    val playbackPosition by audioRecorder.playbackPosition.collectAsState()
    val playbackDuration by audioRecorder.playbackDuration.collectAsState()
    val playbackState by audioRecorder.playbackState.collectAsState()
    
    // Sync isPlaying with playbackState
    LaunchedEffect(playbackState) {
        isPlaying = playbackState == AudioRecorderManager.PlaybackState.Playing
    }
    
    // Sync recording info when question changes
    LaunchedEffect(currentQuestion?.id) {
        currentQuestion?.id?.let { qId ->
            currentRecordingInfo = recordingsByQuestion[qId]
            isRecording = false
            isPlaying = false
            audioRecorder.reset()
        }
    }
    
    // Recording timer - now uses audioRecorder's internal timer, just collect it
    val recorderDuration by audioRecorder.recordingDuration.collectAsState()
    LaunchedEffect(isRecording, recorderDuration) {
        if (isRecording) {
            recordingDuration = recorderDuration / 1000 // Convert ms to seconds
        }
    }
    
    // Permission handling
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }
    
    // UI State
    var showExitWarningDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showSubmitSuccessDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Timer
    val sectionDuration = sectionState.section?.durationSeconds ?: 15 * 60
    var timeRemaining by remember(sectionDuration) { mutableStateOf(sectionDuration) }
    var autoSubmitted by remember { mutableStateOf(false) }

    // Navigation flags
    val isLastPart = currentPartIndex >= parts.size - 1
    val isLastQuestionGroup = currentQuestionGroupIndex >= questionGroups.size - 1
    val isLastQuestion = currentQuestionIndex >= questions.size - 1
    val isAtVeryEnd = isLastPart && isLastQuestionGroup && isLastQuestion
    
    // Count recorded questions
    val totalQuestions = parts.sumOf { part ->
        part.questionGroups?.sumOf { it.questions?.size ?: 0 } ?: 0
    }
    val recordedCount = recordingsByQuestion.size

    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        } else {
            if (!autoSubmitted) {
                autoSubmitted = true
                // Submit all recordings
                scope.launch {
                    submitAllRecordings(
                        recordingsByQuestion,
                        uploadedUrlsByQuestion,
                        cloudinaryUploader,
                        viewModel,
                        examId
                    )
                }
            }
        }
    }
    
    // Handle submit success
    LaunchedEffect(sectionState.message) {
        if (sectionState.message != null && !sectionState.isSubmitting) {
            showSubmitSuccessDialog = true
        }
    }

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)

    BackHandler { showExitWarningDialog = true }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Cần quyền ghi âm", fontWeight = FontWeight.Bold) },
            text = { Text("Để ghi âm bài Speaking, ứng dụng cần quyền truy cập microphone.", color = NavBarText) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Cấp quyền", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Để sau", color = NavBarText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Exit Warning Dialog
    if (showExitWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExitWarningDialog = false },
            title = { Text("Thoát khỏi bài thi?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Bài làm của bạn sẽ không được lưu. Bạn có chắc chắn muốn thoát?", color = NavBarText) },
            confirmButton = {
                TextButton(onClick = {
                    showExitWarningDialog = false
                    audioRecorder.release()
                    navController.popBackStack()
                }) {
                    Text("Thoát", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarningDialog = false }) {
                    Text("Tiếp tục làm bài", color = GradientStart, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Nộp bài thi?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bạn đã ghi $recordedCount/$totalQuestions câu trả lời.", color = MainText)
                    Text("Sau khi nộp bạn sẽ không thể chỉnh sửa.", color = NavBarText)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitDialog = false
                        scope.launch {
                            isUploading = true
                            uploadProgress = "Đang upload audio..."
                            submitAllRecordings(
                                recordingsByQuestion,
                                uploadedUrlsByQuestion,
                                cloudinaryUploader,
                                viewModel,
                                examId
                            )
                            isUploading = false
                            uploadProgress = null
                        }
                    },
                    enabled = !sectionState.isSubmitting && !isUploading
                ) {
                    Text("Nộp bài", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Tiếp tục làm bài", color = NavBarText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Submit Success Dialog
    if (showSubmitSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Đã nộp bài thành công!", fontWeight = FontWeight.Bold) },
            text = { Text("Bài làm của bạn đã được ghi nhận.", color = NavBarText) },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitSuccessDialog = false
                    navController.previousBackStackEntry?.savedStateHandle?.set("completedSectionId", sectionId)
                    navController.popBackStack()
                }) {
                    Text("OK", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timerText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining < 300) Color(0xFFEF4444) else Color(0xFFEA580C)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showSubmitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !sectionState.isSubmitting && !isUploading
                    ) {
                        if (sectionState.isSubmitting || isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Nộp bài", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Part Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPartIndex > 0) {
                            currentPartIndex--
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentPartIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous part",
                        tint = if (currentPartIndex > 0) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentPart?.title ?: "Part ${currentPartIndex + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$recordedCount/$totalQuestions câu đã ghi",
                        fontSize = 12.sp,
                        color = if (recordedCount > 0) GradientStart else NavBarText
                    )
                }
                
                IconButton(
                    onClick = {
                        if (currentPartIndex < parts.size - 1) {
                            currentPartIndex++
                            currentQuestionGroupIndex = 0
                            currentQuestionIndex = 0
                        }
                    },
                    enabled = currentPartIndex < parts.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next part",
                        tint = if (currentPartIndex < parts.size - 1) MainText else Color(0xFFD1D5DB),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Part progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                parts.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    idx == currentPartIndex -> GradientStart
                                    idx < currentPartIndex -> GradientStart.copy(alpha = 0.5f)
                                    else -> Color(0xFFE5E7EB)
                                }
                            )
                    )
                }
            }

            // Question Group Header
            currentQuestionGroup?.let { qGroup ->
                qGroup.description?.let { desc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = qGroup.title ?: "Instructions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C)
                            )
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = NavBarText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Question Card
            currentQuestion?.let { question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = GradientStart,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Câu ${currentQuestionIndex + 1}/${questions.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainText
                            )
                            
                            // Show if recorded
                            if (recordingsByQuestion.containsKey(question.id)) {
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Đã ghi",
                                            fontSize = 12.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = question.prompt,
                            fontSize = 15.sp,
                            color = MainText,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Recording Section
            currentQuestion?.let { question ->
                RecordingSection(
                    isRecording = isRecording,
                    recordingDuration = recordingDuration,
                    currentRecordingInfo = currentRecordingInfo ?: recordingsByQuestion[question.id],
                    isPlaying = isPlaying,
                    hasPermission = hasAudioPermission,
                    playbackProgress = playbackProgress,
                    playbackPosition = playbackPosition,
                    playbackDuration = if (playbackDuration > 0) playbackDuration else currentRecordingInfo?.durationMs ?: 0L,
                    onStartRecording = {
                        if (!hasAudioPermission) {
                            showPermissionDialog = true
                        } else {
                            if (audioRecorder.startRecording(question.id)) {
                                isRecording = true
                                currentRecordingInfo = null
                            }
                        }
                    },
                    onStopRecording = {
                        isRecording = false
                        val info = audioRecorder.stopRecording()
                        if (info != null) {
                            currentRecordingInfo = info
                            recordingsByQuestion[question.id] = info
                        }
                    },
                    onPlayPause = {
                        currentRecordingInfo?.let { info ->
                            audioRecorder.togglePlayback(info.filePath)
                        }
                    },
                    onSeek = { progress ->
                        audioRecorder.seekToProgress(progress)
                    },
                    onReRecord = {
                        currentRecordingInfo?.let { info ->
                            audioRecorder.deleteRecording(info.filePath)
                            currentRecordingInfo = null
                            recordingsByQuestion.remove(question.id)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        when {
                            currentQuestionIndex > 0 -> currentQuestionIndex--
                            currentQuestionGroupIndex > 0 -> {
                                currentQuestionGroupIndex--
                                currentQuestionIndex = (questionGroups.getOrNull(currentQuestionGroupIndex - 1)?.questions?.size ?: 1) - 1
                            }
                            currentPartIndex > 0 -> {
                                currentPartIndex--
                                currentQuestionGroupIndex = 0
                                currentQuestionIndex = 0
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavBarText),
                    enabled = (currentQuestionIndex > 0 || currentQuestionGroupIndex > 0 || currentPartIndex > 0) 
                        && !sectionState.isSubmitting && !isRecording && timeRemaining > 0
                ) {
                    Text("Câu trước", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        when {
                            !isLastQuestion -> currentQuestionIndex++
                            !isLastQuestionGroup -> {
                                currentQuestionGroupIndex++
                                currentQuestionIndex = 0
                            }
                            !isLastPart -> {
                                currentPartIndex++
                                currentQuestionGroupIndex = 0
                                currentQuestionIndex = 0
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart,
                        disabledContainerColor = Color(0xFF9CA3AF)
                    ),
                    enabled = !isAtVeryEnd && !sectionState.isSubmitting && !isRecording && timeRemaining > 0
                ) {
                    Text(
                        text = when {
                            isAtVeryEnd -> "Hoàn thành"
                            isLastQuestion && isLastQuestionGroup -> "Part tiếp"
                            isLastQuestion -> "Nhóm tiếp"
                            else -> "Câu tiếp"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingSection(
    isRecording: Boolean,
    recordingDuration: Long,
    currentRecordingInfo: RecordingInfo?,
    isPlaying: Boolean,
    hasPermission: Boolean,
    playbackProgress: Float,
    playbackPosition: Long,
    playbackDuration: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onReRecord: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isRecording -> Color(0xFFFEE2E2)
                currentRecordingInfo != null -> Color(0xFFDCFCE7)
                else -> Color(0xFFF3F4F6)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isRecording) {
                // Recording in progress
                RecordingInProgress(
                    duration = recordingDuration,
                    onStop = onStopRecording
                )
            } else if (currentRecordingInfo != null) {
                // Recorded - show playback controls
                RecordedAudioPlayer(
                    recordingInfo = currentRecordingInfo,
                    isPlaying = isPlaying,
                    playbackProgress = playbackProgress,
                    playbackPosition = playbackPosition,
                    playbackDuration = playbackDuration,
                    onPlayPause = onPlayPause,
                    onSeek = onSeek,
                    onReRecord = onReRecord
                )
            } else {
                // Ready to record
                ReadyToRecord(
                    hasPermission = hasPermission,
                    onStart = onStartRecording
                )
            }
        }
    }
}

@Composable
private fun ReadyToRecord(
    hasPermission: Boolean,
    onStart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(48.dp)
        )
        
        Text(
            text = "Nhấn để bắt đầu ghi âm",
            fontSize = 14.sp,
            color = NavBarText
        )
        
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = GradientStart),
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bắt đầu ghi",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (!hasPermission) {
            Text(
                text = "Cần cấp quyền microphone",
                fontSize = 12.sp,
                color = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
private fun RecordingInProgress(
    duration: Long,
    onStop: () -> Unit
) {
    // Animated recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animated recording indicator
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = alpha * 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((60 * scale).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )
        }
        
        // Recording timer
        Text(
            text = formatDuration(duration),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444)
        )
        
        Text(
            text = "Đang ghi âm...",
            fontSize = 14.sp,
            color = Color(0xFFEF4444),
            fontWeight = FontWeight.Medium
        )
        
        // Stop button
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dừng ghi",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RecordedAudioPlayer(
    recordingInfo: RecordingInfo,
    isPlaying: Boolean,
    playbackProgress: Float,
    playbackPosition: Long,
    playbackDuration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onReRecord: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = "Đã ghi âm thành công!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
        )
        
        // Recording info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recordingInfo.durationFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    Text(
                        text = "Thời lượng",
                        fontSize = 12.sp,
                        color = NavBarText
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recordingInfo.fileSizeFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainText
                    )
                    Text(
                        text = "Kích thước",
                        fontSize = 12.sp,
                        color = NavBarText
                    )
                }
            }
        }
        
        // Playback progress with seek bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play/Pause button with current time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play/Pause icon button
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(48.dp)
                            .background(GradientStart, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Seek slider
                    Column(modifier = Modifier.weight(1f)) {
                        Slider(
                            value = playbackProgress.coerceIn(0f, 1f),
                            onValueChange = { onSeek(it) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = GradientStart,
                                activeTrackColor = GradientStart,
                                inactiveTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                        
                        // Time labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = playbackPosition.formatDuration(),
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                            Text(
                                text = if (playbackDuration > 0) playbackDuration.formatDuration() else recordingInfo.durationFormatted,
                                fontSize = 12.sp,
                                color = NavBarText
                            )
                        }
                    }
                }
            }
        }
        
        // Re-record button
        OutlinedButton(
            onClick = onReRecord,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444))
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ghi lại", fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private suspend fun submitAllRecordings(
    recordingsByQuestion: Map<Int, RecordingInfo>,
    uploadedUrlsByQuestion: MutableMap<Int, String>,
    cloudinaryUploader: CloudinaryUploader,
    viewModel: ExamViewModel,
    examId: Int
) {
    // Upload each recording to Cloudinary
    recordingsByQuestion.forEach { (questionId, recordingInfo) ->
        if (!uploadedUrlsByQuestion.containsKey(questionId)) {
            val result = cloudinaryUploader.uploadAudioSigned(
                filePath = recordingInfo.filePath,
                publicId = "speaking_q${questionId}_${System.currentTimeMillis()}"
            )
            
            when (result) {
                is UploadResult.Success -> {
                    uploadedUrlsByQuestion[questionId] = result.url
                    viewModel.updateAnswer(questionId, result.url)
                }
                is UploadResult.Error -> {
                    // Use local path as fallback
                    viewModel.updateAnswer(questionId, recordingInfo.filePath)
                }
                else -> {}
            }
        }
    }
    
    // Submit section
    viewModel.submitCurrentSection(examId)
}
