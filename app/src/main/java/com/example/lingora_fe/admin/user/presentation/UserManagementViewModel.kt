package com.example.lingora_fe.admin.user.presentation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.user.data.remote.dto.toDto
import com.example.lingora_fe.admin.user.domain.model.CreateUserData
import com.example.lingora_fe.admin.user.domain.model.SortOption
import com.example.lingora_fe.core.domain.model.ProficiencyLevel
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.model.UserFilterOptions
import com.example.lingora_fe.admin.user.domain.model.UserStatus
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val repository: UserManagementRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UserManagementState())
    val state: StateFlow<UserManagementState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(UserFormState())
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)

    init {
        loadUsers()
    }

    fun onEvent(event: UserManagementEvent) {
        when (event) {
            is UserManagementEvent.LoadUsers -> loadUsers(event.page)
            is UserManagementEvent.SearchUsers -> searchUsers(event.query)
            is UserManagementEvent.FilterByProficiency -> filterByProficiency(event.proficiency)
            is UserManagementEvent.FilterByStatus -> filterByStatus(event.status)
            is UserManagementEvent.SortBy -> sortBy(event.sortOption)
            is UserManagementEvent.ClearFilters -> clearFilters()
            is UserManagementEvent.RefreshUsers -> refreshUsers()
            
            is UserManagementEvent.LoadUserDetails -> loadUserDetails(event.userId)
            is UserManagementEvent.ClearSelectedUser -> clearSelectedUser()
            
            is UserManagementEvent.CreateUser -> createUser(
                username = event.username,
                email = event.email,
                password = event.password,
                roleIds = event.roleIds,
                proficiency = event.proficiency
            )
            
            is UserManagementEvent.UpdateUser -> updateUser(
                userId = event.userId,
                username = event.username,
                email = event.email,
                newPassword = event.newPassword,
                roleIds = event.roleIds,
                proficiency = event.proficiency,
                status = event.status
            )
            
            is UserManagementEvent.DeleteUser -> deleteUser(event.userId)
            is UserManagementEvent.RestoreUser -> restoreUser(event.userId)
            
            is UserManagementEvent.ClearError -> clearError()
            is UserManagementEvent.ClearActionMessages -> clearActionMessages()
        }
    }

    private fun loadUsers(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val token = getToken() ?: run {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Authentication token not found"
                )
                return@launch
            }

            val filterOptions = UserFilterOptions(
                search = _state.value.searchQuery.takeIf { it.isNotBlank() },
                proficiency = _state.value.selectedProficiency?.value,
                status = _state.value.selectedStatus?.value,
                sort = _state.value.selectedSort?.apiValue,
                page = page,
                limit = 20
            )

            repository.getAllUsers(token, filterOptions)
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        users = metadata.users,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        totalUsers = metadata.total,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun searchUsers(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadUsers(page = 1)
    }

    private fun filterByProficiency(proficiency: ProficiencyLevel?) {
        _state.value = _state.value.copy(selectedProficiency = proficiency)
        loadUsers(page = 1)
    }

    private fun filterByStatus(status: UserStatus?) {
        _state.value = _state.value.copy(selectedStatus = status)
        loadUsers(page = 1)
    }

    private fun sortBy(sortOption: SortOption?) {
        _state.value = _state.value.copy(selectedSort = sortOption)
        loadUsers(page = 1)
    }

    private fun clearFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedProficiency = null,
            selectedStatus = null,
            selectedSort = null
        )
        loadUsers(page = 1)
    }

    private fun refreshUsers() {
        loadUsers(_state.value.currentPage)
    }

    private fun loadUserDetails(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUserDetailsLoading = true)
            
            val token = getToken() ?: return@launch

            repository.getUserById(token, userId)
                .onRight { user ->
                    _state.value = _state.value.copy(
                        selectedUser = user,
                        isUserDetailsLoading = false
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUserDetailsLoading = false,
                        error = failure.message
                    )
                }
        }
    }

    private fun clearSelectedUser() {
        _state.value = _state.value.copy(selectedUser = null)
    }

    private fun createUser(
        username: String,
        email: String,
        password: String,
        roleIds: List<Int>,
        proficiency: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, actionError = null)
            
            val token = getToken() ?: return@launch

            val userData = CreateUserData(
                username = username,
                email = email,
                password = password,
                roleIds = roleIds,
                proficiency = proficiency
            )

            repository.createUser(token, userData)
                .onRight {
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionSuccess = "User created successfully"
                    )
                    loadUsers()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun updateUser(
        userId: Int,
        username: String?,
        email: String?,
        newPassword: String?,
        roleIds: List<Int>?,
        proficiency: String?,
        status: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, actionError = null)
            
            val token = getToken() ?: return@launch
            Log.d("UserManagementViewModel", "Preparing to update user $roleIds")
            val userData = UpdateUserData(
                username = username,
                email = email,
                newPassword = newPassword,
                roleIds = roleIds,
                proficiency = proficiency,
                status = status
            )
            Log.d("UserManagementViewModel", "Updating user $userId with data: ${userData.toDto()}")
            repository.updateUser(token, userId, userData)
                .onRight {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionSuccess = "User updated successfully"
                    )
                    loadUsers()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, actionError = null)
            
            val token = getToken() ?: return@launch

            repository.deleteUser(token, userId)
                .onRight {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionSuccess = "User deleted successfully"
                    )
                    loadUsers()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun restoreUser(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRestoring = true, actionError = null)
            
            val token = getToken() ?: return@launch

            repository.restoreUser(token, userId)
                .onRight {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        actionSuccess = "User restored successfully"
                    )
                    loadUsers()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        actionError = failure.message
                    )
                }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun clearActionMessages() {
        _state.value = _state.value.copy(
            actionSuccess = null,
            actionError = null
        )
    }

    // Form state management
    fun updateFormState(formState: UserFormState) {
        _formState.value = formState.validate()
    }

    fun resetFormState() {
        _formState.value = UserFormState()
    }

    fun loadUserIntoForm(userId: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch

            repository.getUserById(token, userId)
                .onRight { user ->
                    _formState.value = UserFormState(
                        username = user.username,
                        email = user.email,
                        selectedRoleIds = user.roles.map { it.id },
                        selectedProficiency = ProficiencyLevel.values()
                            .find { it.value == user.proficiency } 
                            ?: ProficiencyLevel.BEGINNER,
                        selectedStatus = UserStatus.values()
                            .find { it.value == user.status }
                            ?: UserStatus.ACTIVE
                    ).validate()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(error = failure.message)
                }
        }
    }

    private fun getToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }
}

