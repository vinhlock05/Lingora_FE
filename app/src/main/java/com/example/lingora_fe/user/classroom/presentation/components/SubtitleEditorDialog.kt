package com.example.lingora_fe.user.classroom.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.lingora_fe.core.ui.components.FocusComponent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.exoplayer.ExoPlayer
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

data class SubtitleCue(
    val index: Int,
    var startTime: Long, // in ms
    var endTime: Long,   // in ms
    var text: String
)

data class SubtitleCueUiState(
    val index: Int,
    val startTimeStr: String,
    val endTimeStr: String,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleEditorDialog(
    initialSubtitlesJson: String?,
    exoPlayer: ExoPlayer,
    attachment: ClassroomLessonAttachment,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val gson = remember { Gson() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val surfaceInteractionSource = remember { MutableInteractionSource() }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // ExoPlayer position states for real-time time code indicator
    var playerPosition by remember { mutableStateOf(0L) }
    var playerDuration by remember { mutableStateOf(0L) }

    // Polling player position and duration when dialog is visible
    LaunchedEffect(exoPlayer) {
        while (true) {
            playerPosition = exoPlayer.currentPosition
            playerDuration = exoPlayer.duration.coerceAtLeast(0L)
            delay(200)
        }
    }

    // Parse the JSON string into mutable cues of SubtitleCueUiState
    val mutableCues = remember {
        val list = mutableStateListOf<SubtitleCueUiState>()
        if (!initialSubtitlesJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SubtitleCue>>() {}.type
                val parsed: List<SubtitleCue> = gson.fromJson(initialSubtitlesJson, type)
                list.addAll(parsed.map {
                    SubtitleCueUiState(
                        index = it.index,
                        startTimeStr = formatMillisToTime(it.startTime),
                        endTimeStr = formatMillisToTime(it.endTime),
                        text = it.text
                    )
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FocusComponent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Biên tập Phụ đề AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Đóng")
                    }
                }

                Text(
                    text = "Bạn có thể phát bài học, tua đến mốc thời gian và click nút ⏱️ để lấy thời gian hiện tại chính xác.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render Mini Player inside dialog with time indicator below
                val isVideo = attachment.fileType == com.example.lingora_fe.user.classroom.util.LessonAttachmentType.VIDEO
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isVideo) {
                        VideoPlayerView(
                            videoUrl = attachment.fileUrl,
                            exoPlayer = exoPlayer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        PodcastPlayerView(
                            audioUrl = attachment.fileUrl,
                            title = attachment.title ?: attachment.fileName,
                            exoPlayer = exoPlayer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real-time time indicator badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Thời gian hiện tại",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${formatMillisToTime(playerPosition)} / ${formatMillisToTime(playerDuration)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()

                // List of cues
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (mutableCues.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có phân đoạn phụ đề nào. Nhấn Thêm dòng hoặc Lưu để hoàn tất.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    itemsIndexed(mutableCues) { index, cue ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Phân đoạn #${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { 
                                            mutableCues.removeAt(index)
                                            validationError = null // Reset error
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Xoá câu này",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Subtitle Text TextField
                                OutlinedTextField(
                                    value = cue.text,
                                    onValueChange = { newText ->
                                        mutableCues[index] = cue.copy(text = newText)
                                        validationError = null // Reset error
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Nội dung lời thoại...") },
                                    singleLine = false,
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Default
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Character and Word counts
                                val charCount = cue.text.length
                                val wordCount = cue.text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "$charCount ký tự | $wordCount từ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (charCount > 200) MaterialTheme.colorScheme.error 
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Timestamps row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cue.startTimeStr,
                                        onValueChange = { newVal ->
                                            val filtered = newVal.filter { it.isDigit() || it == ':' || it == '.' }
                                            mutableCues[index] = cue.copy(startTimeStr = filtered)
                                            validationError = null
                                        },
                                        placeholder = { Text("0") },
                                        label = { Text("Bắt đầu") },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    val currentPos = exoPlayer.currentPosition
                                                    mutableCues[index] = cue.copy(startTimeStr = formatMillisToTime(currentPos))
                                                    validationError = null
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = "Lấy mốc thời gian hiện tại",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { 
                                                keyboardController?.hide()
                                                focusManager.clearFocus(force = true) 
                                            }
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = cue.endTimeStr,
                                        onValueChange = { newVal ->
                                            val filtered = newVal.filter { it.isDigit() || it == ':' || it == '.' }
                                            mutableCues[index] = cue.copy(endTimeStr = filtered)
                                            validationError = null
                                        },
                                        placeholder = { Text("0") },
                                        label = { Text("Kết thúc") },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    val currentPos = exoPlayer.currentPosition
                                                    mutableCues[index] = cue.copy(endTimeStr = formatMillisToTime(currentPos))
                                                    validationError = null
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccessTime,
                                                    contentDescription = "Lấy mốc thời gian hiện tại",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { 
                                                keyboardController?.hide()
                                                focusManager.clearFocus(force = true) 
                                            }
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                // Error message area
                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Add Line Button (Sticky outlined action above the Save button)
                OutlinedButton(
                    onClick = {
                        val lastCue = mutableCues.lastOrNull()
                        val nextStartMs = lastCue?.let { parseTimeToMillis(it.endTimeStr) } ?: 0L
                        val nextEndMs = nextStartMs + 3000L // default to 3 seconds
                        mutableCues.add(
                            SubtitleCueUiState(
                                index = mutableCues.size,
                                startTimeStr = formatMillisToTime(nextStartMs),
                                endTimeStr = formatMillisToTime(nextEndMs),
                                text = ""
                            )
                        )
                        validationError = null // Reset error
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm dòng")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm phân đoạn mới", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer save button
                Button(
                    onClick = {
                        // 1. Parse string times to milliseconds Long values
                        val parsedCues = ArrayList<SubtitleCue>()
                        var parseError: String? = null
                        
                        for (i in mutableCues.indices) {
                            val cue = mutableCues[i]
                            val startMs = parseTimeToMillis(cue.startTimeStr)
                            val endMs = parseTimeToMillis(cue.endTimeStr)
                            
                            if (startMs == null || endMs == null) {
                                parseError = "Lỗi ở Phân đoạn #${i + 1}: Định dạng thời gian không hợp lệ. Định dạng yêu cầu: MM:SS.mmm hoặc số giây (ví dụ: 01:23.450 hoặc 83.450)."
                                break
                            }
                            parsedCues.add(
                                SubtitleCue(
                                    index = cue.index,
                                    startTime = startMs,
                                    endTime = endMs,
                                    text = cue.text
                                )
                            )
                        }
                        
                        if (parseError != null) {
                            validationError = parseError
                            return@Button
                        }

                        // 2. Sort cues chronologically by startTime
                        val sortedCues = parsedCues.sortedWith(compareBy({ it.startTime }, { it.endTime }))
                        
                        // 3. Perform validations
                        var error: String? = null
                        for (i in sortedCues.indices) {
                            val current = sortedCues[i]
                            if (current.startTime >= current.endTime) {
                                error = "Lỗi ở Phân đoạn #${i + 1}: Thời gian bắt đầu (${current.startTime} ms) phải nhỏ hơn thời gian kết thúc (${current.endTime} ms)."
                                break
                            }
                            if (i > 0) {
                                val prev = sortedCues[i - 1]
                                if (current.startTime < prev.endTime) {
                                    error = "Thời gian của Phân đoạn #${i + 1} (${current.startTime} ms) bị đè lên Phân đoạn #${i} (${prev.endTime} ms)."
                                    break
                                }
                            }
                        }

                        if (error != null) {
                            validationError = error
                        } else {
                            // Re-index sequentially from 0
                            val finalizedList = sortedCues.mapIndexed { i, cue ->
                                cue.copy(index = i)
                            }
                            val finalizedJson = gson.toJson(finalizedList)
                            onSave(finalizedJson)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, "Save icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu phụ đề chính thức")
                }
            }
        }
    }
}
}

private fun formatMillisToTime(ms: Long): String {
    if (ms < 0) return ""
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val millis = ms % 1000
    return String.format("%02d:%02d.%03d", mins, secs, millis)
}

private fun parseTimeToMillis(timeStr: String): Long? {
    val trimmed = timeStr.trim()
    if (trimmed.isEmpty()) return null
    
    // 1. Format: MM:SS.mmm or H:MM:SS.mmm
    if (trimmed.contains(":")) {
        val parts = trimmed.split(":")
        try {
            var hours = 0L
            var minutes = 0L
            var secondsWithMillis = ""
            
            if (parts.size == 3) {
                hours = parts[0].toLongOrNull() ?: 0L
                minutes = parts[1].toLongOrNull() ?: 0L
                secondsWithMillis = parts[2]
            } else if (parts.size == 2) {
                minutes = parts[0].toLongOrNull() ?: 0L
                secondsWithMillis = parts[1]
            } else {
                return null
            }
            
            var seconds = 0L
            var millis = 0L
            if (secondsWithMillis.contains(".")) {
                val secParts = secondsWithMillis.split(".")
                seconds = secParts[0].toLongOrNull() ?: 0L
                var msStr = secParts[1]
                if (msStr.length > 3) msStr = msStr.substring(0, 3)
                while (msStr.length < 3) {
                    msStr += "0"
                }
                millis = msStr.toLongOrNull() ?: 0L
            } else {
                seconds = secondsWithMillis.toLongOrNull() ?: 0L
            }
            
            return hours * 3600000L + minutes * 60000L + seconds * 1000L + millis
        } catch (e: Exception) {
            return null
        }
    }
    
    // 2. Format: Seconds as float (e.g. 83.45)
    if (trimmed.contains(".")) {
        try {
            val parts = trimmed.split(".")
            val seconds = parts[0].toLongOrNull() ?: 0L
            var msStr = parts[1]
            if (msStr.length > 3) msStr = msStr.substring(0, 3)
            while (msStr.length < 3) {
                msStr += "0"
            }
            val millis = msStr.toLongOrNull() ?: 0L
            return seconds * 1000L + millis
        } catch (e: Exception) {
            return null
        }
    }
    
    // 3. Raw milliseconds fallback
    return trimmed.toLongOrNull()
}
