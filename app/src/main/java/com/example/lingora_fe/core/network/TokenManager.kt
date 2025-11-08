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
        private const val KEY_ALL_ROLES = "all_roles"
        private const val KEY_ACTIVE_ROLE = "active_role"
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
            // Save active role (default to userRole for backward compatibility)
            putString(KEY_ACTIVE_ROLE, userRole)
            apply()
        }
        // Verify token was saved
        val savedToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        Log.d(TAG, "Token saved successfully: ${savedToken != null}, token length: ${savedToken?.length}")
    }
    
    /**
     * Lưu access token, refresh token và tất cả roles của user
     */
    fun saveTokensWithRoles(
        accessToken: String,
        refreshToken: String?,
        userId: Int,
        allRoles: List<String>,
        activeRole: String? = null
    ) {
        Log.d(TAG, "Saving tokens with roles - userId: $userId, roles: $allRoles, activeRole: $activeRole")
        val rolesString = allRoles.joinToString(",")
        val defaultActiveRole = activeRole ?: if (allRoles.contains("ADMIN")) "ADMIN" else allRoles.firstOrNull() ?: "LEARNER"
        
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putInt(KEY_USER_ID, userId)
            putString(KEY_ALL_ROLES, rolesString)
            putString(KEY_ACTIVE_ROLE, defaultActiveRole)
            // Keep backward compatibility with KEY_USER_ROLE
            putString(KEY_USER_ROLE, defaultActiveRole)
            apply()
        }
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
     * Lấy user role (backward compatibility - returns active role)
     */
    fun getUserRole(): String? {
        return getActiveRole() ?: sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    
    /**
     * Lấy active role (role hiện tại đang được sử dụng)
     */
    fun getActiveRole(): String? {
        return sharedPreferences.getString(KEY_ACTIVE_ROLE, null)
    }
    
    /**
     * Lấy tất cả roles của user
     */
    fun getAllRoles(): List<String> {
        val rolesString = sharedPreferences.getString(KEY_ALL_ROLES, null)
        return if (rolesString.isNullOrBlank()) {
            // Backward compatibility: check KEY_USER_ROLE
            val singleRole = sharedPreferences.getString(KEY_USER_ROLE, null)
            if (singleRole != null) listOf(singleRole) else emptyList()
        } else {
            rolesString.split(",").map { it.trim() }
        }
    }
    
    /**
     * Chuyển đổi active role
     */
    fun switchRole(newRole: String): Boolean {
        val allRoles = getAllRoles()
        if (allRoles.contains(newRole)) {
            sharedPreferences.edit()
                .putString(KEY_ACTIVE_ROLE, newRole)
                .putString(KEY_USER_ROLE, newRole) // Keep backward compatibility
                .apply()
            Log.d(TAG, "Switched active role to: $newRole")
            return true
        }
        Log.w(TAG, "Cannot switch to role $newRole - not in available roles: $allRoles")
        return false
    }
    
    /**
     * Kiểm tra user có role cụ thể không
     */
    fun hasRole(role: String): Boolean {
        return getAllRoles().contains(role)
    }
    
    /**
     * Kiểm tra user có thể chuyển đổi giữa ADMIN và LEARNER không
     */
    fun canSwitchRoles(): Boolean {
        val roles = getAllRoles()
        return roles.contains("ADMIN") && roles.contains("LEARNER")
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
            .remove(KEY_ALL_ROLES)
            .remove(KEY_ACTIVE_ROLE)
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

