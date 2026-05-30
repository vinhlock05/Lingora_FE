package com.example.lingora_fe.user.classroom.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SubtitleCue(
    val index: Int,
    var startTime: Long, // in ms
    var endTime: Long,   // in ms
    var text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleEditorDialog(
    initialSubtitlesJson: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val gson = remember { Gson() }
    
    // Parse the JSON string into mutable cues
    val mutableCues = remember {
        val list = mutableStateListOf<SubtitleCue>()
        if (!initialSubtitlesJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SubtitleCue>>() {}.type
                val parsed: List<SubtitleCue> = gson.fromJson(initialSubtitlesJson, type)
                list.addAll(parsed)
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
                    text = "AI đã tự động sinh phụ đề bên dưới. Bạn có thể bấm phát thử bài học và sửa đổi lại các lỗi chính tả hoặc thời gian trước khi lưu chính thức.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider()

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
                                    text = "Không có phân đoạn phụ đề nào. Nhấn lưu để hoàn tất.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    itemsIndexed(mutableCues) { index, cue ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                        text = "Phân đoạn #${cue.index}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { mutableCues.removeAt(index) },
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
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Nội dung lời thoại...") },
                                    singleLine = false,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Timestamps row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cue.startTime.toString(),
                                        onValueChange = { newVal ->
                                            newVal.toLongOrNull()?.let {
                                                mutableCues[index] = cue.copy(startTime = it)
                                            }
                                        },
                                        label = { Text("Bắt đầu (ms)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = cue.endTime.toString(),
                                        onValueChange = { newVal ->
                                            newVal.toLongOrNull()?.let {
                                                mutableCues[index] = cue.copy(endTime = it)
                                            }
                                        },
                                        label = { Text("Kết thúc (ms)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer save button
                Button(
                    onClick = {
                        val finalizedJson = gson.toJson(mutableCues.toList())
                        onSave(finalizedJson)
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
