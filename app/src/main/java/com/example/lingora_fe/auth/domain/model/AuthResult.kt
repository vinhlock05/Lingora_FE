package com.example.lingora_fe.auth.domain.model

sealed class AuthResult {
    data class Success(val user: User, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

