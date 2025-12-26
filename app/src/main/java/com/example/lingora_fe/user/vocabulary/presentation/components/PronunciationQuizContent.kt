package com.example.lingora_fe.user.vocabulary.presentation.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.lingora_fe.core.ui.theme.GradientEnd
import com.example.lingora_fe.core.ui.theme.GradientStart
import com.example.lingora_fe.core.ui.theme.TopBarBorder
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import java.util.Locale

private const val TAG = "PronunciationQuiz"

@Composable
fun PronunciationQuizContent(
    word: Word,
    attemptCount: Int,
    onResult: (isCorrect: Boolean, recognizedText: String) -> Unit,
    onListenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var hasResult by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    // Dùng rememberUpdatedState để luôn có reference mới nhất của word
    val currentWord by rememberUpdatedState(word)
    
    // Reset state when word changes
    LaunchedEffect(word.id) {
        recognizedText = null
        hasResult = false
        isCorrect = false
        errorMessage = null
        isListening = false
        isProcessing = false
    }
    
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }
    
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
                isListening = true
                errorMessage = null
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech")
                isListening = false
                isProcessing = true
            }

            override fun onError(error: Int) {
                Log.e(TAG, "Speech recognition error: $error")
                isListening = false
                isProcessing = false
                errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được. Hãy nói to và rõ hơn!"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Không nghe thấy gì. Hãy nhấn mic và nói ngay!"
                    SpeechRecognizer.ERROR_AUDIO -> "Lỗi microphone. Kiểm tra quyền truy cập."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Lỗi mạng. Cần kết nối internet ổn định."
                    SpeechRecognizer.ERROR_SERVER -> "Lỗi server Google. Thử lại sau."
                    SpeechRecognizer.ERROR_CLIENT -> "Lỗi ứng dụng. Thử lại."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa cấp quyền microphone."
                    else -> "Lỗi không xác định ($error). Thử lại!"
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                isProcessing = false
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, "Recognition results: $matches")
                
                if (!matches.isNullOrEmpty()) {
                    // Chỉ check 3 từ đầu tiên trong list alternatives
                    val expectedWord = currentWord.word.trim()
                    var matchedText: String? = null
                    var anyMatch = false
                    
                    val checkLimit = minOf(matches.size, 3) // Giới hạn 3 alternatives
                    for (i in 0 until checkLimit) {
                        val trimmed = matches[i].trim()
                        if (trimmed.equals(expectedWord, ignoreCase = true)) {
                            matchedText = trimmed
                            anyMatch = true
                            break
                        }
                    }
                    
                    // Nếu không có exact match, lấy item đầu tiên để hiển thị
                    val displayText = matchedText ?: matches[0]
                    recognizedText = displayText
                    hasResult = true
                    isCorrect = anyMatch
                    
                    Log.d(TAG, "All matches: $matches")
                    Log.d(TAG, "Expected: '$expectedWord', Matched: $matchedText, Correct: $anyMatch")
                    // Không gọi onResult ở đây nữa - chờ user nhấn nút "Tiếp tục"
                } else {
                    errorMessage = "Không nhận diện được. Hãy thử lại!"
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
    
    fun startListening() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        
        recognizedText = null
        hasResult = false
        errorMessage = null
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        
        speechRecognizer.setRecognitionListener(recognitionListener)
        speechRecognizer.startListening(intent)
    }
    
    fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
    }
    
    // UI
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question header
        Text(
            text = if (attemptCount > 0) "Lần thử ${attemptCount + 1}" else "LUYỆN PHÁT ÂM",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Word card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TopBarBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LUYỆN PHÁT ÂM",
                    style = MaterialTheme.typography.labelMedium,
                    color = GradientStart,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Word to pronounce
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Phonetic
                word.phonetic?.let { phonetic ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = phonetic,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
                
                // Meaning
                word.meaning?.let { meaning ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = meaning,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Listen button
                IconButton(
                    onClick = onListenClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(GradientEnd.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Nghe mẫu",
                        tint = GradientEnd,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Nghe mẫu",
                    style = MaterialTheme.typography.bodySmall,
                    color = GradientEnd
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Microphone button (ẩn khi đã có kết quả)
        if (!hasResult) {
            val micScale by animateFloatAsState(
                targetValue = if (isListening) 1.2f else 1f,
                label = "mic_scale"
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(micScale)
                    .shadow(8.dp, CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isListening) {
                                listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            } else {
                                listOf(GradientStart, GradientEnd)
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isListening) {
                            stopListening()
                        } else {
                            startListening()
                        }
                    },
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = if (isListening) "Dừng" else "Bắt đầu đọc",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when {
                    isListening -> "Đang nghe..."
                    isProcessing -> "Đang xử lý..."
                    else -> "Nhấn để đọc"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isListening) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Result display
        if (hasResult && recognizedText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = if (isCorrect) "Chính xác!" else "Chưa đúng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                        
                        Text(
                            text = "Bạn nói: \"$recognizedText\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nút "Thử lại" nếu sai
            if (!isCorrect) {
                Button(
                    onClick = {
                        recognizedText = null
                        hasResult = false
                        isCorrect = false
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Thử lại", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Nút "Tiếp tục"
            Button(
                onClick = {
                    onResult(isCorrect, recognizedText ?: "")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCorrect) GradientStart else Color(0xFFEF4444)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCorrect) "Tiếp tục" else "Bỏ qua",
                    color = Color.White
                )
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Permission request
        if (!hasPermission && !isListening) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
            ) {
                Text("Cấp quyền microphone")
            }
        }
    }
}
