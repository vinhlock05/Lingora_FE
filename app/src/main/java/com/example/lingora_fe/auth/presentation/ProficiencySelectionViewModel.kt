package com.example.lingora_fe.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import com.example.lingora_fe.core.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProficiencySelectionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isAdmin: Boolean = false
)

@HiltViewModel
class ProficiencySelectionViewModel @Inject constructor(
    private val repository: UserManagementRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProficiencySelectionState())
    val state: StateFlow<ProficiencySelectionState> = _state.asStateFlow()

    fun updateProficiency(proficiency: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val token = tokenManager.getAccessToken()
            val userId = tokenManager.getUserId()
            
            if (token == null || userId == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Không tìm thấy thông tin đăng nhập"
                )
                return@launch
            }

            // Use updateProficiencyOnly to only send proficiency field
            // This prevents sending null/empty values for other fields
            val result = if (repository is com.example.lingora_fe.admin.user.data.repository.UserManagementRepositoryImpl) {
                repository.updateProficiencyOnly(token, userId, proficiency)
            } else {
                // Fallback to regular update if repository doesn't have the method
                val updateData = UpdateUserData(proficiency = proficiency)
                repository.updateUser(token, userId, updateData)
            }
            
            result
                .onRight { user ->
                    Log.d("ProficiencySelection", "Proficiency updated successfully: $proficiency")
                    
                    // Check if user is admin
                    val isAdmin = tokenManager.hasRole("ADMIN")
                    val activeRole = tokenManager.getActiveRole()
                    val isAdminActive = activeRole == "ADMIN"
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isAdmin = isAdminActive
                    )
                }
                .onLeft { failure ->
                    Log.e("ProficiencySelection", "Failed to update proficiency: ${failure.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message ?: "Có lỗi xảy ra khi cập nhật trình độ"
                    )
                }
        }
    }
}

