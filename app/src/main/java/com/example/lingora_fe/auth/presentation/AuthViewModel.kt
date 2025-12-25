package com.example.lingora_fe.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.navigation.Route
import com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val tokenManager: TokenManager,
    private val socketManager: NotificationSocketManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        Log.d("AuthViewModel", "Checking authentication status")
        val token = tokenManager.getAccessToken()
        // Chỉ check nếu authState chưa được set (tránh check lại sau khi logout)
        // IMPORTANT: Don't auto-authenticate if we have a user but isAuthenticated is false
        // This can happen after registration - user needs to verify OTP first
        if (token != null && !_authState.value.isAuthenticated && _authState.value.user == null) {
            // Only auto-authenticate if we don't have user data yet
            // If we have user data but isAuthenticated is false, it means user registered but hasn't verified OTP
            viewModelScope.launch {
                authRepository.getProfile(token)
                    .onRight { user ->
                        // Update roles if they've changed or not stored
                        val existingRoles = tokenManager.getAllRoles()
                        if (existingRoles.isEmpty() || existingRoles.size != user.roles.size) {
                            // Save all roles if not already saved or if roles have changed
                            val allRoles = user.roles.map { it.name }
                            val activeRole = tokenManager.getActiveRole() ?: if (allRoles.contains("ADMIN")) {
                                "ADMIN"
                            } else {
                                allRoles.firstOrNull() ?: "LEARNER"
                            }
                            
                            tokenManager.saveTokensWithRoles(
                                accessToken = token,
                                refreshToken = null,
                                userId = user.id,
                                allRoles = allRoles,
                                activeRole = activeRole
                            )
                        }
                        
                        _authState.value = _authState.value.copy(
                            user = user,
                            token = token,
                            isAuthenticated = true
                        )
                    }
                    .onLeft {
                        clearAuthData()
                    }
            }
        } else if (token == null) {
            // Nếu không có token, đảm bảo authState là cleared
            if (_authState.value.isAuthenticated) {
                clearAuthData()
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            authRepository.login(identifier, password)
                .onRight { authData ->
                    // Check if user account is verified (status = ACTIVE)
                    if (authData.user.status != "ACTIVE") {
                        // User hasn't verified email yet
                        // Save token to TokenManager so API calls (like sendEmailVerification) can use it
                        // But DON'T set isAuthenticated = true yet
                        val allRoles = authData.user.roles.map { it.name }
                        val defaultActiveRole = if (allRoles.contains("ADMIN")) {
                            "ADMIN"
                        } else {
                            allRoles.firstOrNull() ?: "LEARNER"
                        }
                        
                        // Save token to TokenManager (needed for authenticated API calls)
                        saveAuthDataWithRoles(
                            token = authData.accessToken,
                            userId = authData.user.id,
                            allRoles = allRoles,
                            activeRole = defaultActiveRole,
                            refreshToken = authData.refreshToken
                        )
                        
                        // Store user data in state but NOT authenticated yet
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            user = authData.user,
                            token = authData.accessToken,
                            isAuthenticated = false, // Not authenticated until OTP verified
                            error = null
                        )
                        Log.d("AuthViewModel", "User status is ${authData.user.status}, needs email verification")
                        // Navigation to OTP screen will be handled by AuthScreen
                    } else {
                        // User is verified, proceed with normal login
                        // Get all roles from user
                        val allRoles = authData.user.roles.map { it.name }
                        // Determine default active role (ADMIN if available, else first role, else LEARNER)
                        val defaultActiveRole = if (allRoles.contains("ADMIN")) {
                            "ADMIN"
                        } else {
                            allRoles.firstOrNull() ?: "LEARNER"
                        }
                        Log.d("AuthViewModel", "User roles: $allRoles, default active role: $defaultActiveRole")
                        
                        // Save all roles and active role
                        saveAuthDataWithRoles(
                            token = authData.accessToken,
                            userId = authData.user.id,
                            allRoles = allRoles,
                            activeRole = defaultActiveRole,
                            refreshToken = authData.refreshToken
                        )
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            user = authData.user,
                            token = authData.accessToken,
                            isAuthenticated = true,
                            error = null
                        )
                    }
                }
                .onLeft { failure ->
                    Log.d("AuthViewModel", "Login failed: $failure")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            authRepository.register(email, username, password)
                .onRight { authData ->
                    // Save token to TokenManager so API calls (like sendEmailVerification) can use it
                    // But DON'T set isAuthenticated = true yet - user needs to verify OTP first
                    val allRoles = authData.user.roles.map { it.name }
                    val defaultActiveRole = if (allRoles.contains("ADMIN")) {
                        "ADMIN"
                    } else {
                        allRoles.firstOrNull() ?: "LEARNER"
                    }
                    
                    // Save token to TokenManager (needed for authenticated API calls)
                    saveAuthDataWithRoles(
                        token = authData.accessToken,
                        userId = authData.user.id,
                        allRoles = allRoles,
                        activeRole = defaultActiveRole,
                        refreshToken = authData.refreshToken
                    )
                    
                    // Store user data in state but NOT authenticated yet
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = authData.user,
                        token = authData.accessToken,
                        isAuthenticated = false, // Not authenticated until OTP verified
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            val currentToken = _authState.value.token
            if (currentToken != null) {
                authRepository.refreshToken(currentToken)
                    .onRight { newToken ->
                        // Preserve existing roles when refreshing token
                        val existingRoles = tokenManager.getAllRoles()
                        val activeRole = tokenManager.getActiveRole() ?: if (existingRoles.contains("ADMIN")) {
                            "ADMIN"
                        } else {
                            existingRoles.firstOrNull() ?: "LEARNER"
                        }
                        val userId = _authState.value.user?.id ?: tokenManager.getUserId() ?: 0
                        
                        if (existingRoles.isNotEmpty()) {
                            // Save with existing roles
                            saveAuthDataWithRoles(
                                token = newToken,
                                userId = userId,
                                allRoles = existingRoles,
                                activeRole = activeRole
                            )
                        } else {
                            // Fallback to old method if no roles stored
                            val userRole = if (_authState.value.user?.roles?.any { it.name == "ADMIN" } == true) {
                                "ADMIN"
                            } else {
                                "LEARNER"
                            }
                            saveAuthData(newToken, userId, userRole)
                        }
                        _authState.value = _authState.value.copy(token = newToken)
                    }
                    .onLeft {
                        clearAuthData()
                        _authState.value = AuthState()
                    }
            }
        }
    }

    fun verifyOTP(email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // TEST MODE: Accept "123456" to bypass OTP verification (remove this in production)
            val isTestOTP = otp == "123456"
            
            if (isTestOTP) {
                // Bypass API call for testing - directly proceed with profile fetch
                Log.d("AuthViewModel", "Using test OTP mode: $otp")
                // Get token from TokenManager (already saved during register/login)
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    // Get user profile to update status
                    authRepository.getProfile(token)
                        .onRight { user ->
                            // Token already saved during register/login
                            // Just update state to authenticated
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                user = user,
                                token = token,
                                isAuthenticated = true,
                                error = null
                            )
                        }
                        .onLeft { failure ->
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = failure.message
                            )
                        }
                } else {
                    // If no token, mark as verified (user will need to login)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                // Normal flow: Call API to verify OTP
                authRepository.verifyEmail(otp)
                    .onRight { user ->
                        // After successful OTP verification, user status is now ACTIVE
                        // Token already saved during register/login
                        // Just update state to authenticated
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            user = user,
                            token = tokenManager.getAccessToken(),
                            isAuthenticated = true,
                            error = null
                        )
                    }
                    .onLeft { failure ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = failure.message
                        )
                    }
            }
        }
    }
    
    fun resendOTP(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.resendOTP(email)
                .onRight {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    private fun saveAuthData(
        token: String,
        userId: Int,
        userRole: String,
        refreshToken: String? = null
    ) {
        Log.d("AuthViewModel", "Saving auth data - userId: $userId, role: $userRole, refreshToken: $refreshToken")
        // Lưu access token và thông tin user
        // refreshToken sẽ được lưu tự động trong cookie bởi CookieJar, không cần lưu vào SharedPreferences
        tokenManager.saveTokens(
            accessToken = token,
            refreshToken = null, // Không lưu refreshToken vào SharedPreferences vì nó đã có trong cookie
            userId = userId,
            userRole = userRole
        )
    }
    
    private fun saveAuthDataWithRoles(
        token: String,
        userId: Int,
        allRoles: List<String>,
        activeRole: String,
        refreshToken: String? = null
    ) {
        Log.d("AuthViewModel", "Saving auth data with roles - userId: $userId, roles: $allRoles, activeRole: $activeRole")
        // Lưu access token, tất cả roles và active role
        // refreshToken sẽ được lưu tự động trong cookie bởi CookieJar
        tokenManager.saveTokensWithRoles(
            accessToken = token,
            refreshToken = null, // Không lưu refreshToken vào SharedPreferences vì nó đã có trong cookie
            userId = userId,
            allRoles = allRoles,
            activeRole = activeRole
        )
    }
    
    /**
     * Chuyển đổi active role và navigate đến view tương ứng
     */
    fun switchRole(newRole: String, onNavigate: (String) -> Unit) {
        if (tokenManager.switchRole(newRole)) {
            val destination = if (newRole == "ADMIN") {
                Route.AdminNavigation.route
            } else {
                Route.UserNavigation.route
            }
            onNavigate(destination)
        }
    }

    fun clearAuthData() {
        Log.d("AuthViewModel", "Clearing auth data")
        runCatching { socketManager.disconnect() }
        tokenManager.clearTokens()
        _authState.value = AuthState()
    }

    fun logout() {
        viewModelScope.launch {
            // Call logout API to invalidate refresh token on backend
            // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
            authRepository.logout("")
                .onRight {
                    Log.d("AuthViewModel", "Logout successful on backend")
                }
                .onLeft { failure ->
                    Log.e("AuthViewModel", "Logout error: ${failure.message}")
                }
            // Không clear data ở đây, để caller tự quyết định khi nào clear
            // (cần đợi logout API hoàn thành trước khi clear cookies)
        }
    }
    
    /**
     * Logout và clear data - gọi logout API trước, sau đó clear data
     */
    suspend fun logoutAndClear() {
        // Gọi logout API trước để xóa refreshToken trên backend
        // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
        authRepository.logout("")
            .onRight {
                Log.d("AuthViewModel", "Logout successful on backend")
            }
            .onLeft { failure ->
                Log.e("AuthViewModel", "Logout error: ${failure.message}")
            }
        // Đợi một chút để đảm bảo logout API đã hoàn thành
        kotlinx.coroutines.delay(200)
        // Sau đó mới clear local data (bao gồm cookies)
        clearAuthData()
    }
    
    // Password Reset Flow
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.sendPasswordResetEmail(email)
                .onRight {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordResetEmail = email,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }
    
    fun verifyPasswordResetOtp(email: String, code: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.verifyPasswordResetOtp(email, code)
                .onRight { resetToken ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        resetToken = resetToken,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }
    
    fun confirmPasswordReset(resetToken: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.confirmPasswordReset(resetToken, newPassword)
                .onRight {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        resetToken = null,
                        passwordResetEmail = null,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }
    
    // Email Verification Flow
    fun sendEmailVerification() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.sendEmailVerification()
                .onRight {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }
    
    fun verifyEmailWithCode(code: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.verifyEmail(code)
                .onRight { user ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }
}

