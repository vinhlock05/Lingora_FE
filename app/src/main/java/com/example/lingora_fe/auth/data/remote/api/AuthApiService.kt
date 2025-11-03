package com.example.lingora_fe.auth.data.remote.api

import com.example.lingora_fe.auth.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>
    
    @POST("auth/verify-otp")
    suspend fun verifyOTP(
        @Body request: VerifyOTPRequest
    ): Response<OTPResponse>
    
    @POST("auth/resend-otp")
    suspend fun resendOTP(
        @Body request: ResendOTPRequest
    ): Response<OTPResponse>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<RefreshTokenResponse>
    
    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>
}

