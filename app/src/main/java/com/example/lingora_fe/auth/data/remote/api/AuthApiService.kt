package com.example.lingora_fe.auth.data.remote.api

import com.example.lingora_fe.auth.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthMetaData>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthMetaData>
    
    @POST("auth/verify-otp")
    suspend fun verifyOTP(
        @Body request: VerifyOTPRequest
    ): ApiResponse<OTPMetaData>
    
    @POST("auth/resend-otp")
    suspend fun resendOTP(
        @Body request: ResendOTPRequest
    ): ApiResponse<OTPMetaData>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenMetaData>
    
    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): ApiResponse<Any>
    
    @GET("auth/me")
    suspend fun getProfile(): ApiResponse<UserDto>
}

