package com.example.lingora_fe.admin.user.presentation

import android.util.Patterns
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.UserStatus

// Main UI State
data class UserManagementState(
    val users: List<AdminUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalUsers: Int = 0,
    
    // Filters
    val searchQuery: String = "",
    val selectedProficiency: ProficiencyLevel? = null,
    val selectedStatus: UserStatus? = null,
    
    // Selected user for details/edit
    val selectedUser: AdminUser? = null,
    val isUserDetailsLoading: Boolean = false,
    
    // Action states
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val isRestoring: Boolean = false,
    
    val actionSuccess: String? = null,
    val actionError: String? = null
)

// Form State for Create/Edit
data class UserFormState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRoleIds: List<Int> = listOf(2), // Default to LEARNER
    val selectedProficiency: ProficiencyLevel = ProficiencyLevel.BEGINNER,
    val selectedStatus: UserStatus = UserStatus.ACTIVE,
    
    // Validation errors
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    
    val isValid: Boolean = false
) {
    fun validate(): UserFormState {
        val errors = mutableListOf<String>()
        
        val usernameErr = when {
            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            else -> null
        }
        
        val emailErr = when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
        
        val passwordErr = when {
            password.isBlank() && confirmPassword.isBlank() -> null // Allow empty for updates
            password.isNotBlank() && password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        
        val confirmPasswordErr = when {
            password.isNotBlank() && confirmPassword != password -> "Passwords do not match"
            else -> null
        }
        
        return copy(
            usernameError = usernameErr,
            emailError = emailErr,
            passwordError = passwordErr,
            confirmPasswordError = confirmPasswordErr,
            isValid = usernameErr == null && emailErr == null && 
                     passwordErr == null && confirmPasswordErr == null
        )
    }
}

// UI Events
sealed class UserManagementEvent {
    // List events
    data class LoadUsers(val page: Int = 1) : UserManagementEvent()
    data class SearchUsers(val query: String) : UserManagementEvent()
    data class FilterByProficiency(val proficiency: ProficiencyLevel?) : UserManagementEvent()
    data class FilterByStatus(val status: UserStatus?) : UserManagementEvent()
    object ClearFilters : UserManagementEvent()
    object RefreshUsers : UserManagementEvent()
    
    // User details
    data class LoadUserDetails(val userId: Int) : UserManagementEvent()
    object ClearSelectedUser : UserManagementEvent()
    
    // CRUD operations
    data class CreateUser(
        val username: String,
        val email: String,
        val password: String,
        val roleIds: List<Int>,
        val proficiency: String
    ) : UserManagementEvent()
    
    data class UpdateUser(
        val userId: Int,
        val username: String?,
        val email: String?,
        val newPassword: String?,
        val roleIds: List<Int>?,
        val proficiency: String?,
        val status: String?
    ) : UserManagementEvent()
    
    data class DeleteUser(val userId: Int) : UserManagementEvent()
    data class RestoreUser(val userId: Int) : UserManagementEvent()
    
    // UI actions
    object ClearError : UserManagementEvent()
    object ClearActionMessages : UserManagementEvent()
}

// Navigation Events
sealed class UserManagementNavigation {
    object NavigateBack : UserManagementNavigation()
    data class NavigateToUserDetails(val userId: Int) : UserManagementNavigation()
    object NavigateToCreateUser : UserManagementNavigation()
    data class NavigateToEditUser(val userId: Int) : UserManagementNavigation()
}

