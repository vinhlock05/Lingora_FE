package com.example.lingora_fe.core.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class UploadResult(
    val url: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long
)

/**
 * [RequestBody] that streams content directly from a [Uri] via [ContentResolver]
 * without ever loading the entire file into heap memory.
 *
 * This prevents [OutOfMemoryError] when uploading large files (videos, etc.).
 */
private class UriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val contentType: MediaType?,
    private val contentLength: Long = -1L
) : RequestBody() {

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = contentLength

    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            sink.writeAll(inputStream.source())
        } ?: throw IllegalStateException("Cannot open InputStream for URI: $uri")
    }
}

@Singleton
class UploadRepository @Inject constructor(
    private val apiService: UploadApiService
) {
    // Dedicated OkHttpClient without auth interceptor — used for direct Cloudinary calls.
    private val cloudinaryClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)  // large video uploads can take time
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    /**
     * Upload a file from a [Uri].
     * - Video  → signed-URL flow (direct to Cloudinary, bypasses BE server)
     * - Audio  → multipart through BE
     * - Others → multipart through BE
     *
     * Always dispatched to [Dispatchers.IO] — never blocks the main thread.
     * Streams the file content in chunks; never reads the whole file into memory.
     */
    suspend fun uploadFromUri(context: Context, uri: Uri): Either<AppFailure, UploadResult> =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            var fileName = "attachment"
            var fileSize  = 0L
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: "attachment"
                    if (sizeIndex >= 0) fileSize  = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(uri)
                ?: MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(fileName.substringAfterLast('.', ""))
                ?: "application/octet-stream"

            if (mimeType.startsWith("video/")) {
                uploadVideoViaSignedUrl(context, uri, fileName, mimeType, fileSize)
            } else {
                uploadFileViaBe(context, uri, fileName, mimeType, fileSize)
            }
        }

    // ─── Video: Signed URL → stream directly to Cloudinary ────────────────────

    private suspend fun uploadVideoViaSignedUrl(
        context: Context,
        uri: Uri,
        fileName: String,
        mimeType: String,
        fileSize: Long
    ): Either<AppFailure, UploadResult> {
        return Either.catch {
            // 1. Fetch signed params from BE (Retrofit suspend — safe on IO thread)
            val signedResponse = apiService.getSignedUrl(
                folder       = "lingora/videos",
                uploadPreset = null
            )
            val signed = signedResponse.metaData
                ?: throw Exception(signedResponse.message ?: "Không thể lấy signed URL")

            // 2. Build a streaming RequestBody — file is NEVER fully loaded into RAM
            val fileBody = UriRequestBody(
                context       = context,
                uri           = uri,
                contentType   = mimeType.toMediaTypeOrNull(),
                contentLength = fileSize
            )

            // 3. Build multipart request to Cloudinary
            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("api_key", signed.apiKey)
                .addFormDataPart("timestamp", signed.timestamp.toString())
                .addFormDataPart("signature", signed.signature)
                .addFormDataPart("folder", signed.folder)
                .apply {
                    signed.uploadPreset?.let { addFormDataPart("upload_preset", it) }
                }
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${signed.cloudName}/video/upload")
                .post(requestBody)
                .build()

            // 4. Execute on IO thread (blocking OkHttp call — MUST NOT run on Main)
            val response = cloudinaryClient.newCall(request).execute()
            val body = response.body?.string()
                ?: throw Exception("Cloudinary không trả về dữ liệu")
            if (!response.isSuccessful) {
                throw Exception("Cloudinary upload thất bại (${response.code}): $body")
            }

            val json = Gson().fromJson(body, JsonObject::class.java)
            val secureUrl = json.get("secure_url")?.asString
                ?: throw Exception("Không tìm thấy URL trong response Cloudinary")

            UploadResult(
                url           = secureUrl,
                fileName      = fileName,
                mimeType      = mimeType,
                fileSizeBytes = json.get("bytes")?.asLong ?: fileSize
            )
        }.mapLeft { it.toAppFailure() }
    }

    // ─── Non-video: multipart through BE ──────────────────────────────────────

    private suspend fun uploadFileViaBe(
        context: Context,
        uri: Uri,
        fileName: String,
        mimeType: String,
        fileSize: Long
    ): Either<AppFailure, UploadResult> {
        return Either.catch {
            // Stream the file — do NOT call readBytes()
            val fileBody = UriRequestBody(
                context       = context,
                uri           = uri,
                contentType   = mimeType.toMediaTypeOrNull(),
                contentLength = fileSize
            )
            val part = MultipartBody.Part.createFormData("file", fileName, fileBody)

            val response = when {
                mimeType.startsWith("image/") -> apiService.uploadImage(part)
                mimeType.startsWith("audio/") -> apiService.uploadAudio(part)
                else                          -> apiService.uploadFile(part)
            }

            val dto = response.metaData
                ?: throw Exception(response.message ?: "Upload thất bại")

            UploadResult(
                url           = dto.url,
                fileName      = dto.name     ?: fileName,
                mimeType      = dto.mimeType ?: mimeType,
                fileSizeBytes = dto.size     ?: fileSize
            )
        }.mapLeft { it.toAppFailure() }
    }
}
