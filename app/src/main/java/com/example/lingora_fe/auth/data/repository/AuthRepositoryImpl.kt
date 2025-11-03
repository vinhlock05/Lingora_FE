package com.example.lingora_fe.auth.data.repository

import com.example.lingora_fe.auth.data.remote.api.AuthApiService
import com.example.lingora_fe.auth.data.remote.dto.*
import com.example.lingora_fe.auth.domain.model.AuthResult
import com.example.lingora_fe.auth.domain.model.User
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {
    
    override suspend fun login(identifier: String, password: String): AuthResult {
        return try {
            val response = authApiService.login(
                LoginRequest(identifier = identifier, password = password)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.metaData != null) {
                    val user = body.metaData.user.toDomainModel()
                    val token = body.metaData.accessToken
                    AuthResult.Success(user = user, token = token)
                } else {
                    AuthResult.Error(message = body.message)
                }
            } else {
                AuthResult.Error(message = "Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun register(email: String, username: String, password: String): AuthResult {
        return try {
            val response = authApiService.register(
                RegisterRequest(email = email, username = username, password = password)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 201 && body.metaData != null) {
                    val user = body.metaData.user.toDomainModel()
                    val token = body.metaData.accessToken
                    AuthResult.Success(user = user, token = token)
                } else {
                    AuthResult.Error(message = body.message)
                }
            } else {
                AuthResult.Error(message = "Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun verifyOTP(email: String, otp: String): AuthResult {
        return try {
            val response = authApiService.verifyOTP(
                VerifyOTPRequest(email = email, otp = otp)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.metaData?.verified == true) {
                    // Create a temporary user object for OTP verification success
                    val tempUser = User(
                        id = 0,
                        username = "",
                        email = email,
                        roles = emptyList(),
                        avatar = null,
                        status = "pending_verification",
                        proficiency = "",
                        createdAt = ""
                    )
                    AuthResult.Success(user = tempUser, token = "")
                } else {
                    AuthResult.Error(message = body.message)
                }
            } else {
                AuthResult.Error(message = "OTP verification failed: ${response.message()}")
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun resendOTP(email: String): AuthResult {
        return try {
            val response = authApiService.resendOTP(
                ResendOTPRequest(email = email)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200) {
                    // Create a temporary user object for resend OTP success
                    val tempUser = User(
                        id = 0,
                        username = "",
                        email = email,
                        roles = emptyList(),
                        avatar = null,
                        status = "otp_resent",
                        proficiency = "",
                        createdAt = ""
                    )
                    AuthResult.Success(user = tempUser, token = "")
                } else {
                    AuthResult.Error(message = body.message)
                }
            } else {
                AuthResult.Error(message = "Resend OTP failed: ${response.message()}")
            }
        } catch (e: Exception) {
            AuthResult.Error(message = e.message ?: "Unknown error occurred")
        }
    }
    
    override suspend fun refreshToken(token: String): String? {
        return try {
            val response = authApiService.refreshToken("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.metaData != null) {
                    body.metaData.accessToken
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun logout(token: String): Boolean {
        return try {
            val response = authApiService.logout("Bearer $token")
            response.isSuccessful && response.body()?.statusCode == 200
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getProfile(token: String): User? {
        return try {
            val response = authApiService.getProfile("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.statusCode == 200 && body.metaData != null) {
                    body.metaData.toDomainModel()
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

