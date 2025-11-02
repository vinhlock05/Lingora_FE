package com.example.lingora_fe.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    companion object {
        private const val TAG = "AudioRecorder"
    }

    fun startRecording(): Boolean {
        try {
            // Create audio file
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(null)
            audioFile = File.createTempFile(
                "AUDIO_${timeStamp}_",
                ".3gp",
                storageDir
            )

            Log.d(TAG, "Audio file created at: ${audioFile?.absolutePath}")

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Recording started successfully")
                    return true
                } catch (e: IOException) {
                    Log.e(TAG, "MediaRecorder prepare() failed: ${e.message}", e)
                    release()
                    return false
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording: ${e.message}", e)
            return false
        }
    }

    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "Stop recording called but not currently recording")
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val filePath = audioFile?.absolutePath
            val fileSize = audioFile?.length() ?: 0

            Log.d(TAG, "Recording stopped successfully")
            Log.d(TAG, "Audio file path: $filePath")
            Log.d(TAG, "Audio file size: $fileSize bytes")

            // Log audio file details
            if (audioFile?.exists() == true) {
                Log.d(TAG, "✅ Audio file exists and is valid")
                Log.d(TAG, "File details:")
                Log.d(TAG, "  - Name: ${audioFile?.name}")
                Log.d(TAG, "  - Size: ${fileSize / 1024f} KB")
                Log.d(TAG, "  - Can read: ${audioFile?.canRead()}")
            } else {
                Log.e(TAG, "❌ Audio file does not exist!")
            }

            return filePath
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            return null
        }
    }

    fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }

            // Delete the audio file
            audioFile?.delete()
            audioFile = null
            Log.d(TAG, "Recording cancelled and file deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling recording: ${e.message}", e)
        }
    }

    fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        Log.d(TAG, "AudioRecorder released")
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    fun getAudioFilePath(): String? = audioFile?.absolutePath
}

