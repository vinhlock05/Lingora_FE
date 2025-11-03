package com.example.lingora_fe.auth.domain.repository

import com.example.lingora_fe.auth.domain.model.AuthResult
import com.example.lingora_fe.auth.domain.model.User

interface AuthRepository {
    suspend fun login(identifier: String, password: String): AuthResult
    suspend fun register(email: String, username: String, password: String): AuthResult
    suspend fun verifyOTP(email: String, otp: String): AuthResult
    suspend fun resendOTP(email: String): AuthResult
    suspend fun refreshToken(token: String): String?
    suspend fun logout(token: String): Boolean
    suspend fun getProfile(token: String): User?
}

