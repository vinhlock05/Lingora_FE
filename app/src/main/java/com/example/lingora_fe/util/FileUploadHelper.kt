package com.example.lingora_fe.util

import android.content.Context
import android.net.Uri
import android.util.Log
import arrow.core.Either
import com.example.lingora_fe.auth.data.remote.api.AuthApiService
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.core.network.AuthInterceptor
import com.example.lingora_fe.core.network.PersistentCookieJar
import com.example.lingora_fe.core.network.SelectiveNullsAdapterFactory
import com.example.lingora_fe.core.network.TokenAuthenticator
import com.example.lingora_fe.core.network.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.lingora_fe.util.Constant
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object FileUploadHelper {
    private const val TAG = "FileUploadHelper"
    private const val SIGNED_URL_ENDPOINT = "uploads/signed-url"
    private const val CLOUDINARY_API_BASE = "https://api.cloudinary.com/v1_1"

    private val cloudinaryClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class SignedUploadMeta(
        val signature: String,
        val timestamp: Long,
        val cloudName: String,
        val apiKey: String,
        val folder: String
    )

    private fun createBackendClient(context: Context): OkHttpClient {
        val sharedPrefs = context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)
        val cookieJar = PersistentCookieJar(sharedPrefs)
        val tokenManager = TokenManager(sharedPrefs, cookieJar)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val gson = GsonBuilder()
            .setLenient()
            .registerTypeAdapterFactory(SelectiveNullsAdapterFactory())
            .create()

        val refreshClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val refreshAuthApiService = refreshRetrofit.create(AuthApiService::class.java)
        val tokenAuthenticator = TokenAuthenticator(tokenManager, refreshAuthApiService)
        val authInterceptor = AuthInterceptor(tokenManager)

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun fetchSignedMeta(
        context: Context,
        folder: String
    ): SignedUploadMeta = withContext(Dispatchers.IO) {
        val baseUrl = Constant.BASE_URL
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val url = "${normalizedBaseUrl}${SIGNED_URL_ENDPOINT}?folder=$folder"

        Log.d(TAG, "Requesting signed meta url=$url folder=$folder")

        val client = createBackendClient(context)

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            Log.e(
                TAG,
                "Signed meta request failed code=${response.code} message=${response.message} body=$errorBody"
            )
            throw Exception("Failed to get signed upload meta: ${response.code} ${response.message}")
        }

        val body = response.body?.string() ?: throw Exception("Empty response when fetching signed meta")
        Log.d(TAG, "Signed meta response body=$body")
        val root = JSONObject(body)
        val meta = root.optJSONObject("metaData") ?: root

        SignedUploadMeta(
            signature = meta.getString("signature"),
            timestamp = meta.getLong("timestamp"),
            cloudName = meta.getString("cloudName"),
            apiKey = meta.getString("apiKey"),
            folder = meta.getString("folder")
        )
    }

    /**
     * Upload image file to server
     * @param context Android context
     * @param imageUri URI of the selected image
     * @return Either<AppFailure, String> - URL of uploaded image or error
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "lingora/images"
    ): Either<AppFailure, String> = withContext(Dispatchers.IO) {
        Either.Companion.catch {
            val file =
                uriToFile(context, imageUri) ?: throw Exception("Failed to convert URI to file")

            Log.d(TAG, "uploadImage start filePath=${file.absolutePath} size=${file.length()} folder=$folder")

            val signedMeta = fetchSignedMeta(
                context = context,
                folder = folder
            )

            Log.d(TAG, "uploadImage signedMeta cloudName=${signedMeta.cloudName} folder=${signedMeta.folder}")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("api_key", signedMeta.apiKey)
                .addFormDataPart("timestamp", signedMeta.timestamp.toString())
                .addFormDataPart("signature", signedMeta.signature)
                .addFormDataPart("folder", signedMeta.folder)
                .build()

            val request = Request.Builder()
                .url("$CLOUDINARY_API_BASE/${signedMeta.cloudName}/image/upload")
                .post(requestBody)
                .build()

            val response = cloudinaryClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(
                    TAG,
                    "uploadImage failed code=${response.code} message=${response.message} body=$errorBody"
                )
                throw Exception("Upload failed: ${response.code} ${response.message} $errorBody")
            }

            val responseBody = response.body?.string()
            val json = responseBody?.let { JSONObject(it) }
                ?: throw Exception("Empty response from Cloudinary")
            val imageUrl = json.optString("secure_url")
            if (imageUrl.isNullOrBlank()) {
                throw Exception("Cloudinary response missing secure_url")
            }

            Log.d(TAG, "uploadImage success url=$imageUrl")

            // Clean up temp file
            file.delete()

            imageUrl
        }.mapLeft { it.toAppFailure() }
    }

    /**
     * Upload audio file to server
     * @param context Android context
     * @param audioUri URI of the selected audio
     * @return Either<AppFailure, String> - URL of uploaded audio or error
     */
    suspend fun uploadAudio(
        context: Context,
        audioUri: Uri,
        folder: String = "lingora/audios"
    ): Either<AppFailure, String> = withContext(Dispatchers.IO) {
        Either.Companion.catch {
            val file =
                uriToFile(context, audioUri) ?: throw Exception("Failed to convert URI to file")

            Log.d(TAG, "uploadAudio start filePath=${file.absolutePath} size=${file.length()} folder=$folder")

            val signedMeta = fetchSignedMeta(
                context = context,
                folder = folder
            )

            Log.d(TAG, "uploadAudio signedMeta cloudName=${signedMeta.cloudName} folder=${signedMeta.folder}")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/*".toMediaTypeOrNull())
                )
                .addFormDataPart("api_key", signedMeta.apiKey)
                .addFormDataPart("timestamp", signedMeta.timestamp.toString())
                .addFormDataPart("signature", signedMeta.signature)
                .addFormDataPart("folder", signedMeta.folder)
                .build()

            val request = Request.Builder()
                // For audio we use Cloudinary video resource type
                .url("$CLOUDINARY_API_BASE/${signedMeta.cloudName}/video/upload")
                .post(requestBody)
                .build()

            val response = cloudinaryClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(
                    TAG,
                    "uploadAudio failed code=${response.code} message=${response.message} body=$errorBody"
                )
                throw Exception("Upload failed: ${response.code} ${response.message} $errorBody")
            }

            val responseBody = response.body?.string()
            val json = responseBody?.let { JSONObject(it) }
                ?: throw Exception("Empty response from Cloudinary")
            val audioUrl = json.optString("secure_url")
            if (audioUrl.isNullOrBlank()) {
                throw Exception("Cloudinary response missing secure_url")
            }

            Log.d(TAG, "uploadAudio success url=$audioUrl")

            // Clean up temp file
            file.delete()

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
            Log.d(TAG, "uriToFile created temp file path=${tempFile.absolutePath} size=${tempFile.length()}")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "uriToFile failed for uri=$uri", e)
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
