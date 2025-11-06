package com.example.lingora_fe.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val token = tokenManager.getAccessToken()
        if (token != null) {
            viewModelScope.launch {
                authRepository.getProfile(token)
                    .onRight { user ->
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
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            authRepository.login(identifier, password)
                .onRight { authData ->
                    // Determine user role (check if user has ADMIN role)
                    val userRole = if (authData.user.roles.any { it.name == "ADMIN" }) {
                        "ADMIN"
                    } else {
                        "LEARNER"
                    }
                    Log.d("AuthViewModel", "User role determined: $userRole")
                    saveAuthData(authData.accessToken, authData.user.id, userRole, authData.refreshToken)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = authData.user,
                        token = authData.accessToken,
                        isAuthenticated = true,
                        error = null
                    )
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
                    // Determine user role (check if user has ADMIN role)
                    val userRole = if (authData.user.roles.any { it.name == "ADMIN" }) {
                        "ADMIN"
                    } else {
                        "LEARNER"
                    }
                    saveAuthData(authData.accessToken, authData.user.id, userRole, authData.refreshToken)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = authData.user,
                        token = authData.accessToken,
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

    fun refreshToken() {
        viewModelScope.launch {
            val currentToken = _authState.value.token
            if (currentToken != null) {
                authRepository.refreshToken(currentToken)
                    .onRight { newToken ->
                        val userRole = if (_authState.value.user?.roles?.any { it.name == "ADMIN" } == true) {
                            "ADMIN"
                        } else {
                            "LEARNER"
                        }
                        saveAuthData(newToken, _authState.value.user?.id ?: 0, userRole)
                        _authState.value = _authState.value.copy(token = newToken)
                    }
                    .onLeft {
                        clearAuthData()
                        _authState.value = AuthState()
                    }
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            val token = _authState.value.token
            if (token != null) {
                _authState.value = _authState.value.copy(isLoading = true)
                authRepository.getProfile(token)
                    .onRight { user ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            user = user
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

    fun verifyOTP(email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            authRepository.verifyOTP(email, otp)
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

    fun clearAuthData() {
        Log.d("AuthViewModel", "Clearing auth data")
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
            // Always clear local data regardless of API result
            clearAuthData()
        }
    }
}

