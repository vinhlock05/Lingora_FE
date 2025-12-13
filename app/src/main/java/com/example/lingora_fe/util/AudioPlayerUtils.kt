package com.example.lingora_fe.util

import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

/**
 * Audio playback state holder
 */
data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val hasEnded: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0f,
    val error: String? = null
) {
    val currentTimeFormatted: String
        get() {
            val minutes = (currentPosition / 1000 / 60).toInt()
            val seconds = (currentPosition / 1000 % 60).toInt()
            return String.format("%02d:%02d", minutes, seconds)
        }
    
    val durationFormatted: String
        get() {
            val minutes = (duration / 1000 / 60).toInt()
            val seconds = (duration / 1000 % 60).toInt()
            return String.format("%02d:%02d", minutes, seconds)
        }
}

/**
 * Composable function to remember and manage ExoPlayer instance
 * 
 * @param audioUrl The URL of the audio to play
 * @param autoPlay Whether to auto-play when ready (after buffer delay)
 * @param bufferDelayMs Delay in milliseconds before starting playback (default 1500ms)
 * @param onPlaybackEnded Callback when playback ends
 */
@Composable
@OptIn(UnstableApi::class)
fun rememberAudioPlayer(
    audioUrl: String?,
    autoPlay: Boolean = true,
    bufferDelayMs: Long = 1500L,
    onPlaybackEnded: () -> Unit = {}
): AudioPlayerController {
    val context = LocalContext.current
    
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }
    
    var state by remember { mutableStateOf(AudioPlayerState()) }
    var hasAutoPlayed by remember(audioUrl) { mutableStateOf(false) }
    
    // Setup media when URL changes
    LaunchedEffect(audioUrl) {
        if (!audioUrl.isNullOrEmpty()) {
            state = state.copy(isBuffering = true, isReady = false, hasEnded = false, error = null)
            hasAutoPlayed = false
            
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                val mediaItem = MediaItem.fromUri(audioUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } catch (e: Exception) {
                state = state.copy(isBuffering = false, error = e.message)
            }
        }
    }
    
    // Listen to player state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        state = state.copy(isBuffering = true)
                    }
                    Player.STATE_READY -> {
                        state = state.copy(
                            isBuffering = false,
                            isReady = true,
                            duration = exoPlayer.duration.coerceAtLeast(0L)
                        )
                    }
                    Player.STATE_ENDED -> {
                        state = state.copy(isPlaying = false, hasEnded = true)
                        onPlaybackEnded()
                    }
                    Player.STATE_IDLE -> {
                        state = state.copy(isBuffering = false, isReady = false)
                    }
                }
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                state = state.copy(isPlaying = playing)
            }
            
            override fun onPlayerError(error: PlaybackException) {
                state = state.copy(
                    isBuffering = false,
                    error = error.message ?: "Playback error"
                )
            }
        }
        
        exoPlayer.addListener(listener)
        
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    // Auto-play after buffer delay when ready
    LaunchedEffect(state.isReady, autoPlay, hasAutoPlayed) {
        if (state.isReady && autoPlay && !hasAutoPlayed) {
            delay(bufferDelayMs) // Wait for buffer
            if (state.isReady && !state.hasEnded) {
                exoPlayer.play()
                hasAutoPlayed = true
            }
        }
    }
    
    // Update progress while playing
    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            delay(200L)
            val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration.coerceAtLeast(1L)
            state = state.copy(
                currentPosition = currentPos,
                progress = if (dur > 0) currentPos.toFloat() / dur.toFloat() else 0f
            )
        }
    }
    
    return remember(exoPlayer) {
        AudioPlayerController(exoPlayer) { newState ->
            state = newState
        }
    }.also {
        it.updateState(state)
    }
}

/**
 * Controller class for ExoPlayer operations
 */
class AudioPlayerController(
    private val exoPlayer: ExoPlayer,
    private val onStateChange: (AudioPlayerState) -> Unit
) {
    private var _state = AudioPlayerState()
    val state: AudioPlayerState get() = _state
    
    fun updateState(newState: AudioPlayerState) {
        _state = newState
    }
    
    fun play() {
        exoPlayer.play()
    }
    
    fun pause() {
        exoPlayer.pause()
    }
    
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        onStateChange(_state.copy(currentPosition = positionMs))
    }
    
    fun seekToProgress(progress: Float) {
        if (_state.duration > 0) {
            val newPosition = (progress * _state.duration).toLong()
            seekTo(newPosition)
        }
    }
    
    fun replay() {
        exoPlayer.seekTo(0)
        exoPlayer.play()
    }
    
    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }
    
    fun release() {
        exoPlayer.release()
    }
}

/**
 * Simple composable wrapper for audio player state observation
 */
@Composable
fun AudioPlayerEffect(
    controller: AudioPlayerController,
    onStateChange: (AudioPlayerState) -> Unit
) {
    val state = controller.state
    LaunchedEffect(state) {
        onStateChange(state)
    }
}
