package com.example.lingora_fe.auth.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.auth.domain.model.AuthResult
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val token = sharedPreferences.getString("access_token", null)
        if (token != null) {
            viewModelScope.launch {
                val user = authRepository.getProfile(token)
                if (user != null) {
                    _authState.value = _authState.value.copy(
                        user = user,
                        token = token,
                        isAuthenticated = true
                    )
                } else {
                    clearAuthData()
                }
            }
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.login(identifier, password)) {
                is AuthResult.Success -> {
                    saveAuthData(result.token, result.user.id)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        token = result.token,
                        isAuthenticated = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.register(email, username, password)) {
                is AuthResult.Success -> {
                    saveAuthData(result.token, result.user.id)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        token = result.token,
                        isAuthenticated = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val token = _authState.value.token
            if (token != null) {
                authRepository.logout(token)
            }
            clearAuthData()
            _authState.value = AuthState()
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            val currentToken = _authState.value.token
            if (currentToken != null) {
                val newToken = authRepository.refreshToken(currentToken)
                if (newToken != null) {
                    saveAuthData(newToken, _authState.value.user?.id ?: 0)
                    _authState.value = _authState.value.copy(token = newToken)
                } else {
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
                val user = authRepository.getProfile(token)
                if (user != null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = user
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Failed to load profile"
                    )
                }
            }
        }
    }

    fun verifyOTP(email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.verifyOTP(email, otp)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun resendOTP(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.resendOTP(email)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    private fun saveAuthData(token: String, userId: Int) {
        sharedPreferences.edit().apply {
            putString("access_token", token)
            putInt("user_id", userId)
            apply()
        }
    }

    private fun clearAuthData() {
        sharedPreferences.edit().apply {
            remove("access_token")
            remove("user_id")
            apply()
        }
    }
}

