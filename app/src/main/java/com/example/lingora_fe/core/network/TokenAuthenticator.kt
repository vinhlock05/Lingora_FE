package com.example.lingora_fe.core.network

import android.util.Log
import com.example.lingora_fe.auth.data.remote.api.AuthApiService
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
        
        // If response is 401, try to refresh token using cookie
        // Cookie sẽ tự động được gửi bởi CookieJar
        if (response.code == 401) {
            val currentToken = tokenManager.getAccessToken()

            if (currentToken != null) {
                // Avoid infinite loop: if this is already a retry, don't retry again
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (requestToken != currentToken) {
                    Log.d(TAG, "Token mismatch - already retried, giving up")
                    return null
                }

                return try {
                    Log.d(TAG, "Attempting to refresh access token using cookie...")
                    // Call refresh token endpoint synchronously
                    // Cookie sẽ tự động được gửi bởi CookieJar, không cần gửi trong body
                    val result = runBlocking {
                        refreshAccessToken()
                    }

                    if (result != null) {
                        Log.d(TAG, "✅ Token refresh successful")
                        // Save new access token
                        // Refresh token sẽ được lưu tự động trong cookie bởi CookieJar
                        tokenManager.updateAccessToken(result.accessToken)

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
                Log.d(TAG, "No access token available - clearing tokens")
                // No access token available, clear everything
                tokenManager.clearTokens()
            }
        }

        return null
    }

    private suspend fun refreshAccessToken(): RefreshResult? {
        return try {
            // Gọi refresh token endpoint không cần body
            // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
            val response = authApiService.refreshToken()
            
            Log.d(TAG, "Refresh token response: statusCode=${response.statusCode}")
            
            if (response.statusCode == 200 && response.metaData != null) {
                RefreshResult(
                    accessToken = response.metaData.accessToken
                    // Refresh token sẽ được lưu tự động trong cookie bởi CookieJar
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
        val accessToken: String
    )
}

