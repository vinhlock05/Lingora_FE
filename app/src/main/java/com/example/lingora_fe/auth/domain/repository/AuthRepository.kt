package com.example.lingora_fe.auth.domain.repository

import arrow.core.Either
import com.example.lingora_fe.auth.domain.model.User
import com.example.lingora_fe.core.error.AppFailure

data class AuthData(
    val user: User,
    val accessToken: String,
    val refreshToken: String?
)

interface AuthRepository {
    suspend fun login(identifier: String, password: String): Either<AppFailure, AuthData>
    suspend fun register(email: String, username: String, password: String): Either<AppFailure, AuthData>
    suspend fun verifyOTP(email: String, otp: String): Either<AppFailure, Boolean>
    suspend fun resendOTP(email: String): Either<AppFailure, Boolean>
    suspend fun refreshToken(token: String): Either<AppFailure, String>
    suspend fun logout(token: String): Either<AppFailure, Unit>
    suspend fun getProfile(token: String): Either<AppFailure, User>
    
    // Password Reset Flow
    suspend fun sendPasswordResetEmail(email: String): Either<AppFailure, Boolean>
    suspend fun verifyPasswordResetOtp(email: String, code: String): Either<AppFailure, String> // Returns resetToken
    suspend fun confirmPasswordReset(resetToken: String, newPassword: String): Either<AppFailure, Boolean>
    
    // Email Verification Flow
    suspend fun sendEmailVerification(): Either<AppFailure, Boolean>
    suspend fun verifyEmail(code: String): Either<AppFailure, User>
}

