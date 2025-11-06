package com.example.lingora_fe.core.error

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

/**
 * Sealed class representing application failures
 */
sealed class AppFailure(
    open val message: String,
    open val code: Int? = null
) {
    data class NetworkError(
        override val message: String,
        override val code: Int? = null
    ) : AppFailure(message, code)

    data class ServerError(
        override val message: String,
        override val code: Int? = null
    ) : AppFailure(message, code)

    data class ValidationError(
        override val message: String,
        override val code: Int? = null
    ) : AppFailure(message, code)

    data class UnauthorizedError(
        override val message: String = "Unauthorized access",
        override val code: Int? = 401
    ) : AppFailure(message, code)

    data class NotFoundError(
        override val message: String = "Resource not found",
        override val code: Int? = 404
    ) : AppFailure(message, code)

    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val code: Int? = null
    ) : AppFailure(message, code)
}

/**
 * Error response DTO to parse backend error messages
 */
private data class ErrorResponse(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("statusCode")
    val statusCode: Int? = null,
    @SerializedName("metaData")
    val metaData: Any? = null
)

/**
 * Extension function to convert Throwable to AppFailure
 * Properly extracts error messages from backend HTTP responses
 */
fun Throwable.toAppFailure(): AppFailure {
    return when (this) {
        // Handle Retrofit HTTP exceptions
        is HttpException -> {
            val code = this.code()
            val errorBody = this.response()?.errorBody()?.string()
            
            // Try to parse the error body to get backend message
            val backendMessage = try {
                if (!errorBody.isNullOrEmpty()) {
                    val gson = Gson()
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
            
            // Use backend message if available, otherwise use default message
            val message = backendMessage ?: when (code) {
                400 -> "Yêu cầu không hợp lệ"
                401 -> "Không có quyền truy cập"
                403 -> "Truy cập bị từ chối"
                404 -> "Không tìm thấy tài nguyên"
                408 -> "Yêu cầu hết thời gian chờ"
                500 -> "Lỗi máy chủ nội bộ"
                502 -> "Lỗi cổng kết nối"
                503 -> "Dịch vụ không khả dụng"
                else -> "Lỗi HTTP $code"
            }
            
            // Return appropriate failure type based on status code
            when (code) {
                400 -> AppFailure.ValidationError(message, code)
                401 -> AppFailure.UnauthorizedError(message, code)
                404 -> AppFailure.NotFoundError(message, code)
                in 500..599 -> AppFailure.ServerError(message, code)
                else -> AppFailure.ServerError(message, code)
            }
        }
        
        // Handle network errors
        is java.net.UnknownHostException -> AppFailure.NetworkError(
            message = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.",
            code = null
        )
        
        is java.net.SocketTimeoutException -> AppFailure.NetworkError(
            message = "Kết nối hết thời gian chờ. Vui lòng thử lại.",
            code = null
        )
        
        is java.io.IOException -> AppFailure.NetworkError(
            message = "Lỗi kết nối mạng. Vui lòng thử lại.",
            code = null
        )
        
        // Handle other exceptions
        else -> AppFailure.UnknownError(
            message = this.message ?: "Đã xảy ra lỗi không xác định",
            code = null
        )
    }
}
