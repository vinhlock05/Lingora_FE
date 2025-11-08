package com.example.lingora_fe.user.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.core.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val userManagementRepository: UserManagementRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)
            
            val token = tokenManager.getAccessToken()
            if (token != null) {
                authRepository.getProfile(token)
                    .onRight { user ->
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            user = user,
                            error = null
                        )
                    }
                    .onLeft { failure ->
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            error = failure.message
                        )
                    }
            } else {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = "No authentication token found"
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoggingOut = true)
            
            // Call logout API to invalidate refresh token on backend
            // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
            authRepository.logout("")
                .onRight {
                    Log.d("ProfileViewModel", "Logout successful")
                    clearLocalData()
                    onSuccess()
                }
                .onLeft { failure ->
                    Log.e("ProfileViewModel", "Logout failed: ${failure.message}")
                    // Clear local data anyway
                    clearLocalData()
                    onSuccess()
                }
        }
    }

    private fun clearLocalData() {
        tokenManager.clearTokens()
        _profileState.value = ProfileState()
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }
    
    /**
     * Kiểm tra user có thể chuyển đổi roles không
     */
    fun canSwitchRoles(): Boolean {
        return tokenManager.canSwitchRoles()
    }
    
    /**
     * Lấy active role hiện tại
     */
    fun getActiveRole(): String? {
        return tokenManager.getActiveRole()
    }
    
    /**
     * Lấy tất cả roles của user
     */
    fun getAllRoles(): List<String> {
        return tokenManager.getAllRoles()
    }
    
    /**
     * Chuyển đổi role
     */
    fun switchRole(newRole: String, onSuccess: (String) -> Unit) {
        if (tokenManager.switchRole(newRole)) {
            val destination = if (newRole == "ADMIN") {
                com.example.lingora_fe.navigation.Route.AdminNavigation.route
            } else {
                com.example.lingora_fe.navigation.Route.UserNavigation.route
            }
            onSuccess(destination)
        }
    }
    
    /**
     * Cập nhật proficiency
     */
    fun updateProficiency(newProficiency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)
            
            val token = tokenManager.getAccessToken()
            val userId = tokenManager.getUserId()
            val currentUser = _profileState.value.user

            if (token == null || userId == null) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = "Không tìm thấy thông tin đăng nhập"
                )
                return@launch
            }
            
            // Use updateProficiencyOnly to only send proficiency field
            // This prevents sending null/empty values for other fields
            val result = if (userManagementRepository is com.example.lingora_fe.admin.user.data.repository.UserManagementRepositoryImpl) {
                userManagementRepository.updateProficiencyOnly(token, userId, newProficiency)
            } else {
                // Fallback to regular update if repository doesn't have the method
                val updateData = UpdateUserData(proficiency = newProficiency)
                userManagementRepository.updateUser(token, userId, updateData)
            }
            
            result
                .onRight { updatedUser ->
                    Log.d("ProfileViewModel", "Proficiency updated successfully: $newProficiency")
                    // Reload profile to get updated data
                    loadUserProfile()
                    onSuccess()
                }
                .onLeft { failure ->
                    Log.e("ProfileViewModel", "Failed to update proficiency: ${failure.message}")
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        error = failure.message ?: "Có lỗi xảy ra khi cập nhật trình độ"
                    )
                }
        }
    }
}

