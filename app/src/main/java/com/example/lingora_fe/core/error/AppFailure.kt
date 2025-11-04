package com.example.lingora_fe.core.error

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
 * Extension function to convert Throwable to AppFailure
 */
fun Throwable.toAppFailure(): AppFailure {
    return when (this) {
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        is java.io.IOException -> AppFailure.NetworkError(
            message = this.message ?: "Network connection failed"
        )
        else -> AppFailure.UnknownError(
            message = this.message ?: "An unknown error occurred"
        )
    }
}
