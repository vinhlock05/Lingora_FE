package com.example.lingora_fe.user.classroom.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PodcastPlayerView(
    audioUrl: String,
    title: String,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPos by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Listen to player state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                duration = exoPlayer.duration.coerceAtLeast(0)
            }
        }
        exoPlayer.addListener(listener)
        isPlaying = exoPlayer.isPlaying
        duration = exoPlayer.duration.coerceAtLeast(0)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Prepare audio source
    LaunchedEffect(audioUrl) {
        val mediaItem = MediaItem.fromUri(audioUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = false
        currentPos = 0
    }

    // Polling player position when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                currentPos = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(0)
                delay(250)
            }
        } else {
            currentPos = exoPlayer.currentPosition
        }
    }

    val formatTime = { ms: Long ->
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        String.format("%02d:%02d", mins, secs)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClassroomColors.BrandSoftSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ClassroomColors.BrandPrimary.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ClassroomColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trình phát âm thanh bài giảng",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClassroomColors.TextSecondary
                    )
                }

                // Wave visualizer animation
                WaveformAnimation(isPlaying = isPlaying)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slider progress bar
            Slider(
                value = currentPos.toFloat(),
                valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                onValueChange = { newValue ->
                    currentPos = newValue.toLong()
                    exoPlayer.seekTo(currentPos)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = ClassroomColors.BrandPrimary,
                    inactiveTrackColor = ClassroomColors.BrandPrimary.copy(alpha = 0.15f),
                    thumbColor = ClassroomColors.BrandPrimary
                )
            )

            // Duration Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPos),
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextSecondary
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassroomColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Play / Pause button
            IconButton(
                onClick = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = ClassroomColors.BrandPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun WaveformAnimation(isPlaying: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(32.dp)
    ) {
        val barsCount = 6
        for (i in 0 until barsCount) {
            val infiniteTransition = rememberInfiniteTransition(label = "waveform_bar_$i")
            val targetHeight by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 8f,
                    targetValue = Random.nextInt(12, 32).toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = Random.nextInt(400, 700), easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "height_$i"
                )
            } else {
                remember { mutableStateOf(8f) }
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(targetHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ClassroomColors.BrandPrimary)
            )
        }
    }
}
