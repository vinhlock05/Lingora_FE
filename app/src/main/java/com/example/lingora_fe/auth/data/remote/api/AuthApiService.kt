package com.example.lingora_fe.auth.data.remote.api

import com.example.lingora_fe.auth.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthMetaData>

    @POST("auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): ApiResponse<AuthMetaData>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthMetaData>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(): ApiResponse<RefreshTokenMetaData>
    
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Any>
    
    @GET("auth/me")
    suspend fun getProfile(): ApiResponse<UserDto>
    
    // Password Reset Flow
    @POST("auth/password-reset/request")
    suspend fun sendPasswordResetEmail(
        @Body request: SendPasswordResetRequest
    ): ApiResponse<Any>
    
    @POST("auth/password-reset/verify")
    suspend fun verifyPasswordResetOtp(
        @Query("code") code: String,
        @Body request: VerifyPasswordResetRequest
    ): ApiResponse<VerifyPasswordResetResponse>
    
    @POST("auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @retrofit2.http.Header("Authorization") authorization: String,
        @Body request: ConfirmPasswordResetRequest
    ): ApiResponse<Any>
    
    // Email Verification Flow
    @POST("auth/email-verification/request")
    suspend fun sendEmailVerification(): ApiResponse<Any>
    
    @POST("auth/email-verification/verify")
    suspend fun verifyEmail(
        @Body request: VerifyAccountRequest
    ): ApiResponse<UserDto>
}

