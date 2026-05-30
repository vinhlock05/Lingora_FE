package com.example.lingora_fe.user.classroom.presentation.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InteractiveScriptView(
    subtitlesJson: String?,
    currentPosition: Long,
    onSeekTo: (Long) -> Unit,
    exoPlayer: ExoPlayer,
    wordRepository: WordRepository,
    modifier: Modifier = Modifier
) {
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()

    // Parse cues
    val subtitles = remember(subtitlesJson) {
        if (!subtitlesJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SubtitleCue>>() {}.type
                gson.fromJson<List<SubtitleCue>>(subtitlesJson, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Scroll state & automatic scrolling
    val listState = rememberLazyListState()
    val activeIndex = remember(subtitles, currentPosition) {
        subtitles.indexOfFirst { currentPosition in it.startTime..it.endTime }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex != -1) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -150 // center item visually in the viewport
            )
        }
    }

    // Bottom Sheet lookup state
    var selectedWordText by remember { mutableStateOf<String?>(null) }
    var lookupWordResult by remember { mutableStateOf<Word?>(null) }
    var isLookupLoading by remember { mutableStateOf(false) }
    var lookupErrorMsg by remember { mutableStateOf<String?>(null) }
    var audioPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    // Auto-fetch details when word selected
    LaunchedEffect(selectedWordText) {
        selectedWordText?.let { wordText ->
            isLookupLoading = true
            lookupErrorMsg = null
            lookupWordResult = null
            wordRepository.lookupWord(wordText).fold(
                ifLeft = { failure ->
                    isLookupLoading = false
                    lookupErrorMsg = failure.message ?: "Không tìm thấy từ"
                },
                ifRight = { word ->
                    isLookupLoading = false
                    lookupWordResult = word
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer?.release()
            audioPlayer = null
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Lời thoại & Phụ đề bài học",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (subtitles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bài giảng này không có phụ đề chạy chữ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(subtitles) { index, cue ->
                val isActive = index == activeIndex

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else Color.Transparent
                        )
                        .border(
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSeekTo(cue.startTime) }
                        .padding(12.dp)
                ) {
                    // Play arrow cue seek button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Tua tới đây",
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatMillis(cue.startTime),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Word FlowRow for click-to-lookup
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val wordList = cue.text.split(" ")
                        wordList.forEach { word ->
                            val cleanWord = word.replace(Regex("[^a-zA-Z]"), "").lowercase()
                            Text(
                                text = "$word ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        if (cleanWord.isNotBlank()) {
                                            exoPlayer.pause()
                                            selectedWordText = cleanWord
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Tap-to-Lookup Bottom Sheet Dialog
        if (selectedWordText != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedWordText = null },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Tra cứu nhanh",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLookupLoading) {
                            CircularProgressIndicator()
                        } else if (lookupErrorMsg != null) {
                            Text(
                                text = "Không tìm thấy từ: \"$selectedWordText\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (lookupWordResult != null) {
                            val word = lookupWordResult!!
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = word.word,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            word.type?.let {
                                                Text(
                                                    text = "($it)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            word.phonetic?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    if (!word.audioUrl.isNullOrBlank()) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    audioPlayer?.release()
                                                    audioPlayer = MediaPlayer().apply {
                                                        setAudioAttributes(
                                                            AudioAttributes.Builder()
                                                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                                .build()
                                                        )
                                                        setDataSource(word.audioUrl)
                                                        prepareAsync()
                                                        setOnPreparedListener { start() }
                                                        setOnCompletionListener { release() }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Icon(Icons.Default.VolumeUp, "Speak")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                word.vnMeaning?.let {
                                    Text(
                                        text = "Nghĩa tiếng Việt:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                word.example?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ví dụ: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}
