package com.example.lingora_fe.user.profile.presentation

import com.example.lingora_fe.auth.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false
)

