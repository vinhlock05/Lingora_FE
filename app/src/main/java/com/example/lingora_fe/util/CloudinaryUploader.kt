package com.example.lingora_fe.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloudinary Uploader for audio files
 * Using unsigned upload to simplify the process
 */
@Singleton
class CloudinaryUploader @Inject constructor() {
    
    companion object {
        // TODO: Replace with your Cloudinary credentials
        private const val CLOUD_NAME = "dm8mg7lim"
        private const val UPLOAD_PRESET = "speaking_audio" // Create unsigned preset in Cloudinary
        private const val API_KEY = "292416514698949"
        private const val API_SECRET = "veoo_pfy_3aie6U20sExGy7skLY"
        
        private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload"
        private const val TAG = "CloudinaryUploader"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    /**
     * Upload audio file to Cloudinary using unsigned upload
     * @param filePath Local file path
     * @param folder Optional folder in Cloudinary
     * @param publicId Optional public ID (filename)
     * @return UploadResult with URL or error
     */
    suspend fun uploadAudio(
        filePath: String,
        folder: String = "speaking_answers",
        publicId: String? = null
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext UploadResult.Error("File không tồn tại")
            }
            
            // Build multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart("resource_type", "auto")
                .apply {
                    publicId?.let { addFormDataPart("public_id", it) }
                }
                .build()
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()
            
            Log.d(TAG, "Uploading: ${file.name} (${file.length()} bytes)")
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url")
                val publicIdResult = json.optString("public_id")
                val duration = json.optDouble("duration", 0.0)
                val format = json.optString("format")
                
                Log.d(TAG, "Upload success: $secureUrl")
                
                UploadResult.Success(
                    url = secureUrl,
                    publicId = publicIdResult,
                    duration = duration,
                    format = format
                )
            } else {
                val error = responseBody ?: "Upload failed"
                Log.e(TAG, "Upload error: $error")
                UploadResult.Error("Upload thất bại: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            UploadResult.Error("Lỗi upload: ${e.localizedMessage}")
        }
    }
    
    /**
     * Upload with signed request (more secure, requires API secret)
     */
    suspend fun uploadAudioSigned(
        filePath: String,
        folder: String = "speaking_answers",
        publicId: String? = null
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File không tồn tại: $filePath")
                return@withContext UploadResult.Error("File không tồn tại")
            }
            
            Log.d(TAG, "Uploading (signed): ${file.name} (${file.length()} bytes)")
            
            val timestamp = System.currentTimeMillis() / 1000
            
            // Generate signature
            val params = mutableMapOf<String, String>()
            params["folder"] = folder
            params["timestamp"] = timestamp.toString()
            publicId?.let { params["public_id"] = it }
            
            val signature = generateSignature(params)
            Log.d(TAG, "Generated signature for params: $params")
            
            // Build multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                )
                .addFormDataPart("api_key", API_KEY)
                .addFormDataPart("timestamp", timestamp.toString())
                .addFormDataPart("signature", signature)
                .addFormDataPart("folder", folder)
                .apply {
                    publicId?.let { addFormDataPart("public_id", it) }
                }
                .build()
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            Log.d(TAG, "Response code: ${response.code}")
            Log.d(TAG, "Response body: $responseBody")
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url")
                Log.d(TAG, "✅ Upload success! URL: $secureUrl")
                
                UploadResult.Success(
                    url = secureUrl,
                    publicId = json.optString("public_id"),
                    duration = json.optDouble("duration", 0.0),
                    format = json.optString("format")
                )
            } else {
                Log.e(TAG, "❌ Upload failed: ${response.code} - $responseBody")
                UploadResult.Error("Upload thất bại: ${response.code} - $responseBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Signed upload exception", e)
            UploadResult.Error("Lỗi upload: ${e.localizedMessage}")
        }
    }
    
    /**
     * Generate Cloudinary signature
     */
    private fun generateSignature(params: Map<String, String>): String {
        val sortedParams = params.toSortedMap()
        val paramString = sortedParams.entries.joinToString("&") { "${it.key}=${it.value}" }
        val toSign = "$paramString$API_SECRET"
        
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(toSign.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Delete audio from Cloudinary
     */
    suspend fun deleteAudio(publicId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis() / 1000
            val signature = generateSignature(
                mapOf(
                    "public_id" to publicId,
                    "timestamp" to timestamp.toString()
                )
            )
            
            val requestBody = FormBody.Builder()
                .add("public_id", publicId)
                .add("api_key", API_KEY)
                .add("timestamp", timestamp.toString())
                .add("signature", signature)
                .build()
            
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/destroy")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed", e)
            false
        }
    }
}

/**
 * Upload result sealed class
 */
sealed class UploadResult {
    data class Success(
        val url: String,
        val publicId: String,
        val duration: Double,
        val format: String
    ) : UploadResult()
    
    data class Error(val message: String) : UploadResult()
    
    object Uploading : UploadResult()
}
