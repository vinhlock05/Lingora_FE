package com.example.lingora_fe.util

import android.content.Context
import android.net.Uri
import android.util.Log
import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object FileUploadHelper {
    private const val TAG = "FileUploadHelper"
    private const val UPLOAD_ENDPOINT = "upload" // Adjust based on your API

    /**
     * Upload image file to server
     * @param context Android context
     * @param imageUri URI of the selected image
     * @param accessToken Access token for authentication
     * @param baseUrl Base URL of the API
     * @return Either<AppFailure, String> - URL of uploaded image or error
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        accessToken: String,
        baseUrl: String = Constant.BASE_URL
    ): Either<AppFailure, String> = withContext(Dispatchers.IO) {
        Either.Companion.catch {
            val file =
                uriToFile(context, imageUri) ?: throw Exception("Failed to convert URI to file")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.Companion.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("${baseUrl}${UPLOAD_ENDPOINT}/image")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Upload failed: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string()
            // Parse response to get URL
            // Adjust based on your API response format
            // For now, assuming response contains URL directly or in JSON
            val imageUrl = responseBody ?: throw Exception("Empty response from server")

            // Clean up temp file
            file.delete()

            Log.d(TAG, "Image uploaded successfully: $imageUrl")
            imageUrl
        }.mapLeft { it.toAppFailure() }
    }

    /**
     * Upload audio file to server
     * @param context Android context
     * @param audioUri URI of the selected audio
     * @param accessToken Access token for authentication
     * @param baseUrl Base URL of the API
     * @return Either<AppFailure, String> - URL of uploaded audio or error
     */
    suspend fun uploadAudio(
        context: Context,
        audioUri: Uri,
        accessToken: String,
        baseUrl: String = Constant.BASE_URL
    ): Either<AppFailure, String> = withContext(Dispatchers.IO) {
        Either.Companion.catch {
            val file =
                uriToFile(context, audioUri) ?: throw Exception("Failed to convert URI to file")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.Companion.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("${baseUrl}${UPLOAD_ENDPOINT}/audio")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Upload failed: ${response.code} ${response.message}")
            }

            val responseBody = response.body?.string()
            // Parse response to get URL
            // Adjust based on your API response format
            val audioUrl = responseBody ?: throw Exception("Empty response from server")

            // Clean up temp file
            file.delete()

            Log.d(TAG, "Audio uploaded successfully: $audioUrl")
            audioUrl
        }.mapLeft { it.toAppFailure() }
    }

    /**
     * Convert URI to File
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream ?: return null

            val tempFile = File.createTempFile(
                "upload_${System.currentTimeMillis()}",
                getFileExtension(context, uri),
                context.cacheDir
            )

            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }

    /**
     * Get file extension from URI
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.startsWith("image/") == true -> {
                when (mimeType) {
                    "image/jpeg", "image/jpg" -> ".jpg"
                    "image/png" -> ".png"
                    "image/gif" -> ".gif"
                    "image/webp" -> ".webp"
                    else -> ".jpg"
                }
            }
            mimeType?.startsWith("audio/") == true -> {
                when (mimeType) {
                    "audio/mpeg", "audio/mp3" -> ".mp3"
                    "audio/wav" -> ".wav"
                    "audio/ogg" -> ".ogg"
                    "audio/m4a" -> ".m4a"
                    else -> ".mp3"
                }
            }
            else -> ".tmp"
        }
    }
}