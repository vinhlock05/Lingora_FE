package com.example.lingora_fe.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audio Recorder Manager for Speaking Practice
 * Uses MediaRecorder for recording and ExoPlayer for playback
 * Tracks duration with timer for accuracy
 */
class AudioRecorderManager(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var exoPlayer: ExoPlayer? = null
    private var currentFilePath: String? = null
    
    // Timer for tracking recording duration
    private var recordingStartTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var durationUpdateRunnable: Runnable? = null
    
    // Timer for tracking playback progress
    private var playbackUpdateRunnable: Runnable? = null
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState
    
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration
    
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress
    
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition
    
    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration
    
    sealed class RecordingState {
        object Idle : RecordingState()
        object Recording : RecordingState()
        data class Recorded(val filePath: String, val durationMs: Long) : RecordingState()
        data class Error(val message: String) : RecordingState()
    }
    
    sealed class PlaybackState {
        object Idle : PlaybackState()
        object Playing : PlaybackState()
        object Paused : PlaybackState()
    }
    
    /**
     * Generate unique file path for recording
     * Files are saved to: /storage/emulated/0/Android/data/com.example.lingora_fe/files/speaking_recordings/
     */
    fun getRecordingFilePath(questionId: Int): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Use getExternalFilesDir for better reliability and accessibility
        val audioDir = File(context.getExternalFilesDir(null), "speaking_recordings")
        if (!audioDir.exists()) audioDir.mkdirs()
        val filePath = File(audioDir, "question_${questionId}_${timeStamp}.m4a").absolutePath
        Log.d(TAG, "Recording will be saved to: $filePath")
        return filePath
    }
    
    /**
     * Start recording audio
     * Uses MPEG_4 format with AAC encoder - good quality and compatibility
     */
    fun startRecording(questionId: Int): Boolean {
        try {
            // Stop any existing recording
            stopRecordingInternal()
            releasePlayer()
            
            currentFilePath = getRecordingFilePath(questionId)
            
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            // Set error listener to catch any issues
            recorder.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "❌ MediaRecorder ERROR: what=$what, extra=$extra")
                when (what) {
                    MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN -> Log.e(TAG, "Error type: UNKNOWN")
                    MediaRecorder.MEDIA_ERROR_SERVER_DIED -> Log.e(TAG, "Error type: SERVER_DIED")
                }
            }
            
            // Set info listener to catch any warnings
            recorder.setOnInfoListener { _, what, extra ->
                Log.w(TAG, "⚠️ MediaRecorder INFO: what=$what, extra=$extra")
                when (what) {
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> Log.w(TAG, "Info: MAX_DURATION_REACHED")
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> Log.w(TAG, "Info: MAX_FILESIZE_REACHED")
                }
            }
            
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(currentFilePath)
            
            recorder.prepare()
            Log.d(TAG, "✓ MediaRecorder prepared successfully")
            
            recorder.start()
            Log.d(TAG, "✓ MediaRecorder started successfully")
            
            mediaRecorder = recorder
            
            // Start duration timer
            recordingStartTime = System.currentTimeMillis()
            _recordingDuration.value = 0L
            startDurationTimer()
            
            _recordingState.value = RecordingState.Recording
            Log.d(TAG, "Recording started: $currentFilePath")
            Log.d(TAG, "Format: MPEG_4/AAC with system defaults")
            return true
            
        } catch (e: IOException) {
            Log.e(TAG, "Recording prepare/start failed", e)
            _recordingState.value = RecordingState.Error("Không thể bắt đầu ghi âm")
            mediaRecorder?.release()
            mediaRecorder = null
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Recording failed", e)
            _recordingState.value = RecordingState.Error("Lỗi ghi âm: ${e.localizedMessage}")
            mediaRecorder?.release()
            mediaRecorder = null
            return false
        }
    }
    
    private fun startDurationTimer() {
        durationUpdateRunnable = object : Runnable {
            override fun run() {
                if (_recordingState.value == RecordingState.Recording) {
                    _recordingDuration.value = System.currentTimeMillis() - recordingStartTime
                    handler.postDelayed(this, 100) // Update every 100ms
                }
            }
        }
        handler.post(durationUpdateRunnable!!)
    }
    
    private fun stopDurationTimer() {
        durationUpdateRunnable?.let { handler.removeCallbacks(it) }
        durationUpdateRunnable = null
    }
    
    /**
     * Get actual audio duration from file using MediaMetadataRetriever
     */
    private fun getActualAudioDuration(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get actual duration", e)
            0L
        }
    }
    
    /**
     * Stop recording and return file info
     */
    fun stopRecording(): RecordingInfo? {
        stopDurationTimer()
        
        return try {
            // Calculate duration from timer
            val timerDuration = if (recordingStartTime > 0) {
                System.currentTimeMillis() - recordingStartTime
            } else {
                0L
            }
            
            // Stop MediaRecorder properly
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.e(TAG, "MediaRecorder stop failed", e)
                }
                // Small delay to ensure buffers are flushed
                Thread.sleep(100)
                release()
            }
            mediaRecorder = null
            
            val filePath = currentFilePath
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    // Get actual duration from file metadata
                    val actualDuration = getActualAudioDuration(filePath)
                    
                    // Use actual duration if available, otherwise use timer duration
                    val finalDuration = if (actualDuration > 0) actualDuration else timerDuration
                    
                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "Recording stopped!")
                    Log.d(TAG, "File path: $filePath")
                    Log.d(TAG, "File size: ${file.length()} bytes (${file.length() / 1024} KB)")
                    Log.d(TAG, "Timer duration: ${timerDuration}ms (${timerDuration / 1000}s)")
                    Log.d(TAG, "Actual file duration: ${actualDuration}ms (${actualDuration / 1000}s)")
                    Log.d(TAG, "Final duration used: ${finalDuration}ms (${finalDuration / 1000}s)")
                    Log.d(TAG, "═══════════════════════════════════════")
                    
                    _recordingState.value = RecordingState.Recorded(filePath, finalDuration)
                    
                    return RecordingInfo(
                        filePath = filePath,
                        durationMs = finalDuration,
                        fileName = file.name,
                        fileSize = file.length()
                    )
                } else {
                    Log.e(TAG, "File does not exist after recording: $filePath")
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Stop recording failed", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }
    
    private fun stopRecordingInternal() {
        try {
            stopDurationTimer()
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (_: Exception) {}
    }
    
    /**
     * Initialize ExoPlayer for playback
     */
    private fun initializePlayer(filePath: String) {
        releasePlayer()
        
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(filePath))
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            _playbackState.value = PlaybackState.Idle
                            _playbackProgress.value = 0f
                            _playbackPosition.value = 0L
                            stopPlaybackTimer()
                            seekTo(0)
                        }
                        Player.STATE_READY -> {
                            _playbackDuration.value = duration
                            Log.d(TAG, "Player ready, duration: ${duration}ms")
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.value = if (isPlaying) PlaybackState.Playing else PlaybackState.Paused
                    if (isPlaying) {
                        startPlaybackTimer()
                    } else {
                        stopPlaybackTimer()
                    }
                }
            })
            
            prepare()
        }
    }
    
    private fun startPlaybackTimer() {
        playbackUpdateRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    val duration = player.duration
                    val position = player.currentPosition
                    _playbackPosition.value = position
                    _playbackProgress.value = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                }
                handler.postDelayed(this, 100) // Update every 100ms
            }
        }
        handler.post(playbackUpdateRunnable!!)
    }
    
    private fun stopPlaybackTimer() {
        playbackUpdateRunnable?.let { handler.removeCallbacks(it) }
        playbackUpdateRunnable = null
    }
    
    /**
     * Start playback of recorded audio
     */
    fun startPlayback(filePath: String, onComplete: () -> Unit = {}) {
        try {
            if (exoPlayer == null) {
                initializePlayer(filePath)
            }
            
            exoPlayer?.apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onComplete()
                        }
                    }
                })
                play()
            }
            _playbackState.value = PlaybackState.Playing
        } catch (e: Exception) {
            Log.e(TAG, "Playback failed", e)
            _playbackState.value = PlaybackState.Idle
        }
    }
    
    /**
     * Pause playback
     */
    fun pausePlayback() {
        try {
            exoPlayer?.pause()
            _playbackState.value = PlaybackState.Paused
        } catch (e: Exception) {
            Log.e(TAG, "Pause failed", e)
        }
    }
    
    /**
     * Resume playback
     */
    fun resumePlayback() {
        try {
            exoPlayer?.play()
            _playbackState.value = PlaybackState.Playing
        } catch (e: Exception) {
            Log.e(TAG, "Resume failed", e)
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayback(filePath: String) {
        when (_playbackState.value) {
            PlaybackState.Idle -> startPlayback(filePath)
            PlaybackState.Playing -> pausePlayback()
            PlaybackState.Paused -> resumePlayback()
        }
    }
    
    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Long {
        return try {
            exoPlayer?.currentPosition ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get total duration in milliseconds
     */
    fun getDuration(): Long {
        return try {
            exoPlayer?.duration ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
            _playbackPosition.value = position
        } catch (e: Exception) {
            Log.e(TAG, "Seek failed", e)
        }
    }
    
    /**
     * Seek to progress (0.0 to 1.0)
     */
    fun seekToProgress(progress: Float) {
        try {
            val duration = exoPlayer?.duration ?: 0L
            if (duration > 0) {
                val position = (duration * progress).toLong()
                exoPlayer?.seekTo(position)
                _playbackPosition.value = position
                _playbackProgress.value = progress
            }
        } catch (e: Exception) {
            Log.e(TAG, "Seek to progress failed", e)
        }
    }
    
    /**
     * Delete recording file
     */
    fun deleteRecording(filePath: String) {
        try {
            releasePlayer()
            File(filePath).delete()
            if (currentFilePath == filePath) {
                _recordingState.value = RecordingState.Idle
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed", e)
        }
    }
    
    /**
     * Reset to idle state
     */
    fun reset() {
        stopRecordingInternal()
        releasePlayer()
        _recordingState.value = RecordingState.Idle
        _playbackState.value = PlaybackState.Idle
        _recordingDuration.value = 0L
        _playbackProgress.value = 0f
        _playbackPosition.value = 0L
        _playbackDuration.value = 0L
    }
    
    private fun releasePlayer() {
        try {
            stopPlaybackTimer()
            exoPlayer?.apply {
                stop()
                release()
            }
            exoPlayer = null
            _playbackState.value = PlaybackState.Idle
        } catch (e: Exception) {
            Log.e(TAG, "Release player failed", e)
        }
    }
    
    /**
     * Release all resources
     */
    fun release() {
        try {
            stopDurationTimer()
            stopPlaybackTimer()
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Release recorder failed", e)
        }
        releasePlayer()
    }
    
    companion object {
        private const val TAG = "AudioRecorderManager"
    }
}

/**
 * Recording info data class
 */
data class RecordingInfo(
    val filePath: String,
    val durationMs: Long,
    val fileName: String,
    val fileSize: Long
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    
    val fileSizeFormatted: String
        get() {
            return when {
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                else -> String.format("%.1f MB", fileSize / (1024.0 * 1024.0))
            }
        }
}

/**
 * Format milliseconds to MM:SS
 */
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
