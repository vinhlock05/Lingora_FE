package com.example.lingora_fe.core.network

import android.util.Log
import com.example.lingora_fe.auth.data.remote.api.AuthApiService
import com.example.lingora_fe.auth.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * TokenAuthenticator - Tự động refresh token khi nhận 401 Unauthorized
 */
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "authenticate() called with response code: ${response.code}")
        
        // If response is 401 and we have a refresh token, try to refresh
        if (response.code == 401) {
            val refreshToken = tokenManager.getRefreshToken()
            val currentToken = tokenManager.getAccessToken()

            if (refreshToken != null && currentToken != null) {
                // Avoid infinite loop: if this is already a retry, don't retry again
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (requestToken != currentToken) {
                    Log.d(TAG, "Token mismatch - already retried, giving up")
                    return null
                }

                return try {
                    Log.d(TAG, "Attempting to refresh access token...")
                    // Call refresh token endpoint synchronously
                    val result = runBlocking {
                        refreshAccessToken(refreshToken)
                    }

                    if (result != null) {
                        Log.d(TAG, "✅ Token refresh successful")
                        // Save new tokens
                        tokenManager.updateTokens(result.accessToken, result.refreshToken)

                        // Retry the request with new token
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${result.accessToken}")
                            .build()
                    } else {
                        Log.e(TAG, "❌ Token refresh failed - clearing tokens")
                        // Refresh failed, clear tokens
                        tokenManager.clearTokens()
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Token refresh exception: ${e.message}", e)
                    // Refresh failed, clear tokens
                    tokenManager.clearTokens()
                    null
                }
            } else {
                Log.d(TAG, "No refresh token available - clearing tokens")
                // No refresh token available, clear everything
                tokenManager.clearTokens()
            }
        }

        return null
    }

    private suspend fun refreshAccessToken(refreshToken: String): RefreshResult? {
        return try {
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val response = authApiService.refreshToken(request)
            
            Log.d(TAG, "Refresh token response: statusCode=${response.statusCode}")
            
            if (response.statusCode == 200 && response.metaData != null) {
                RefreshResult(
                    accessToken = response.metaData.accessToken,
                    refreshToken = response.metaData.refreshToken
                )
            } else {
                Log.e(TAG, "Refresh failed: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh exception: ${e.message}", e)
            null
        }
    }

    private data class RefreshResult(
        val accessToken: String,
        val refreshToken: String?
    )
}

