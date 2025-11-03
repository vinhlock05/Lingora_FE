package com.example.lingora_fe.auth.presentation

import com.example.lingora_fe.auth.domain.model.User

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

