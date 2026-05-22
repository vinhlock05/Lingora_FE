package com.example.lingora_fe.core.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class UploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("mimeType") val mimeType: String? = null
)

/** Response từ GET /upload/signed-url */
data class SignedUrlResponse(
    @SerializedName("signature") val signature: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("cloudName") val cloudName: String,
    @SerializedName("apiKey") val apiKey: String,
    @SerializedName("folder") val folder: String,
    @SerializedName("uploadPreset") val uploadPreset: String? = null
)

interface UploadApiService {

    /** Lấy signed params để FE upload thẳng lên Cloudinary (dùng cho video lớn) */
    @GET("upload/signed-url")
    suspend fun getSignedUrl(
        @Query("folder") folder: String? = null,
        @Query("uploadPreset") uploadPreset: String? = null
    ): ApiResponse<SignedUrlResponse>

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResponse>

    @Multipart
    @POST("upload/audio")
    suspend fun uploadAudio(
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResponse>

    @Multipart
    @POST("upload/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResponse>
}
