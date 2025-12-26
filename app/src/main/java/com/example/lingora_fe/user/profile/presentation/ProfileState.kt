package com.example.lingora_fe.user.profile.presentation

import com.example.lingora_fe.auth.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
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

