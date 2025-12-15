package com.example.lingora_fe.util

import android.media.MediaPlayer
import android.util.Log

/**
 * Simple helper to play audio from a remote URL using a single [MediaPlayer] instance.
 * Call [playAudio] with the desired URL and optional callbacks. Remember to call [stop]
 * when the screen is disposed to release resources.
 */
object AudioPlayerHelper {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Play audio from the given [audioUrl]. Any currently playing audio will be stopped first.
     */
    fun playAudio(
        audioUrl: String,
        onCompletion: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (audioUrl.isBlank()) {
            onError?.invoke(IllegalArgumentException("Audio url is blank"))
            return
        }

        try {
            stop()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    onCompletion?.invoke()
                    stop()
                }
                setOnErrorListener { _, what, extra ->
                    val exception = Exception("MediaPlayer error what=$what extra=$extra")
                    Log.e("AudioPlayerHelper", "Error playing audio", exception)
                    onError?.invoke(exception)
                    stop()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Failed to play audio", e)
            onError?.invoke(e)
            stop()
        }
    }

    /**
     * Stop the current audio (if any) and release the underlying [MediaPlayer].
     */
    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
            }
        } catch (e: Exception) {
            Log.w("AudioPlayerHelper", "Error stopping MediaPlayer", e)
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}