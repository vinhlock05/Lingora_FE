package com.example.lingora_fe.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor - Tự động gắn access token vào mọi request
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private val EXCLUDED_PATHS = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/refresh-token",
            "/auth/verify-otp",
            "/auth/resend-otp"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Bỏ qua các endpoint không cần authentication
        if (EXCLUDED_PATHS.any { path.contains(it) }) {
            Log.d(TAG, "Skipping auth for path: $path")
            return chain.proceed(originalRequest)
        }

        // Lấy access token
        val accessToken = tokenManager.getAccessToken()

        // Nếu không có token, proceed với request gốc
        if (accessToken == null) {
            Log.d(TAG, "No access token available for path: $path")
            return chain.proceed(originalRequest)
        }

        // Gắn Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        Log.d(TAG, "Added auth token to request: $path")
        return chain.proceed(authenticatedRequest)
    }
}

