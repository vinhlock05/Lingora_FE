package com.example.lingora_fe.user.practice.presentation.screen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.MainText
import com.example.lingora_fe.core.ui.theme.NavBarText
import com.example.lingora_fe.util.AudioRecorderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationPracticeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }
    var customWord by remember { mutableStateOf("") }
    var showWordCard by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    
    // Initialize AudioRecorderManager
    val audioRecorder = remember { AudioRecorderManager(context) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            Log.d("PronunciationPractice", "✅ Audio permission granted")
        } else {
            Log.e("PronunciationPractice", "❌ Audio permission denied")
        }
    }
    
    // Request permission on first launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    // Clean up recorder when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
            Log.d("PronunciationPractice", "AudioRecorderManager released")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện phát âm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MainText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                .padding(paddingValues)
        ) {
            // Custom Word Input Content
            CustomWordInputContent(
                customWord = customWord,
                onWordChange = { customWord = it },
                isRecording = isRecording,
                hasRecorded = hasRecorded,
                showWordCard = showWordCard,
                hasAudioPermission = hasAudioPermission,
                audioRecorder = audioRecorder,
                onAddWord = { 
                    if (customWord.isNotEmpty()) {
                        showWordCard = true
                        Log.d("PronunciationPractice", "Word added: $customWord")
                    }
                },
                onStartRecording = {
                    if (hasAudioPermission) {
                        // Use 0 as questionId for custom word practice
                        val success = audioRecorder.startRecording(0)
                        if (success) {
                            isRecording = true
                            Log.d("PronunciationPractice", "🎤 Started recording for word: $customWord")
                        } else {
                            Log.e("PronunciationPractice", "Failed to start recording")
                        }
                    } else {
                        Log.e("PronunciationPractice", "No audio permission")
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecording = {
                    val recordingInfo = audioRecorder.stopRecording()
                    isRecording = false
                    if (recordingInfo != null) {
                        hasRecorded = true
                        Log.d("PronunciationPractice", "⏹️ Recording stopped")
                        Log.d("PronunciationPractice", "📁 Audio saved at: ${recordingInfo.filePath}")
                        Log.d("PronunciationPractice", "⏱️ Duration: ${recordingInfo.durationFormatted}")
                    } else {
                        Log.e("PronunciationPractice", "Failed to stop recording")
                    }
                },
                onRetry = {
                    hasRecorded = false
                    Log.d("PronunciationPractice", "🔄 Retry recording")
                },
                onReset = {
                    audioRecorder.reset()
                    customWord = ""
                    showWordCard = false
                    hasRecorded = false
                    isRecording = false
                    Log.d("PronunciationPractice", "♻️ Reset practice")
                }
            )
        }
    }
}


@Composable
fun CustomWordInputContent(
    customWord: String,
    onWordChange: (String) -> Unit,
    isRecording: Boolean,
    hasRecorded: Boolean,
    showWordCard: Boolean,
    hasAudioPermission: Boolean,
    audioRecorder: AudioRecorderManager,
    onAddWord: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Nhập từ bạn muốn luyện phát âm",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NavBarText
        )

        // Custom word input
        OutlinedTextField(
            value = customWord,
            onValueChange = onWordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Ví dụ: hello",
                    color = NavBarText.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            trailingIcon = {
                IconButton(
                    onClick = { onAddWord() },
                    enabled = customWord.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (customWord.isNotEmpty()) GradientStart else NavBarText.copy(alpha = 0.3f)
                    )
                }
            },
            singleLine = true
        )

        if (showWordCard && customWord.isNotEmpty()) {
            // Word Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GradientStart
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = customWord,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { /* Play pronunciation */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Nghe mẫu",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Tips Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEFF6FF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Hãy phát âm từ này một cách rõ ràng và chính xác để nhận được phản hồi.",
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recording Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recording Button
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = if (isRecording) Color(0xFFEF4444) else GradientStart,
                            shape = CircleShape
                        )
                        .clickable(enabled = hasAudioPermission) {
                            if (!isRecording) {
                                onStartRecording()
                            } else {
                                onStopRecording()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop" else "Record",
                        modifier = Modifier.size(56.dp),
                        tint = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (!hasAudioPermission) "Cần quyền ghi âm" 
                               else if (isRecording) "Đang ghi âm..." 
                               else "Nhấn để bắt đầu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainText
                    )
                    Text(
                        text = if (!hasAudioPermission) "Vui lòng cấp quyền microphone"
                               else if (isRecording) "Nhấn lại để dừng" 
                               else "Hãy phát âm rõ ràng",
                        fontSize = 13.sp,
                        color = NavBarText
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onRetry() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavBarText
                    )
                ) {
                    Text(
                        text = "Thử lại",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { onReset() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart
                    ),
                    enabled = hasRecorded
                ) {
                    Text(
                        text = "Luyện từ khác",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

