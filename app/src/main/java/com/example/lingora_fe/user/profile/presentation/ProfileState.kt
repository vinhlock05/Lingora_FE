package com.example.lingora_fe.user.profile.presentation

import com.example.lingora_fe.auth.domain.model.User

data class ProfileState(
    /** True only on the very first load when we have no cached user yet. */
    val isLoading: Boolean = false,
    /** True while a background refresh is in flight; UI should keep showing cached data. */
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false
)

data class EditProfileState(
    val isUpdating: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)

data class ChangePasswordState(
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)

