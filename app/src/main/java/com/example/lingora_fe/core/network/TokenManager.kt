package com.example.lingora_fe.core.network

import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val cookieJar: PersistentCookieJar
) {
    companion object {
        private const val TAG = "TokenManager"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
    }

    /**
     * Lưu access token và refresh token
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        userId: Int,
        userRole: String
    ) {
        Log.d(TAG, "Saving tokens - userId: $userId, role: $userRole, accessToken length: ${accessToken.length}")
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, userRole)
            apply()
        }
        // Verify token was saved
        val savedToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        Log.d(TAG, "Token saved successfully: ${savedToken != null}, token length: ${savedToken?.length}")
    }

    /**
     * Lấy access token
     */
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        Log.d(TAG, "Getting access token: ${if (token != null) "found (length: ${token.length})" else "not found"}")
        return token
    }

    /**
     * Lấy refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Lấy user ID
     */
    fun getUserId(): Int? {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        return if (userId != -1) userId else null
    }

    /**
     * Lấy user role
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    /**
     * Cập nhật access token sau khi refresh
     */
    fun updateAccessToken(newAccessToken: String) {
        Log.d(TAG, "Updating access token")
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .apply()
    }

    /**
     * Cập nhật cả access token và refresh token
     */
    fun updateTokens(newAccessToken: String, newRefreshToken: String?) {
        Log.d(TAG, "Updating both tokens")
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, newAccessToken)
            newRefreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            apply()
        }
    }

    /**
     * Xóa tất cả token và thông tin user (logout)
     * Cũng xóa cookies (refreshToken cookie)
     */
    fun clearTokens() {
        Log.d(TAG, "Clearing all tokens and user data")
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .apply()
        // Xóa cookies (refreshToken cookie)
        cookieJar.clearCookies()
    }

    /**
     * Kiểm tra xem user đã login chưa
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Kiểm tra xem có refresh token không
     */
    fun hasRefreshToken(): Boolean {
        return getRefreshToken() != null
    }
}

