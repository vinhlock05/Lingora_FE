package com.example.lingora_fe.auth.data.repository

import arrow.core.Either
import com.example.lingora_fe.auth.data.remote.api.AuthApiService
import com.example.lingora_fe.auth.data.remote.dto.*
import com.example.lingora_fe.auth.domain.model.User
import com.example.lingora_fe.auth.domain.repository.AuthData
import com.example.lingora_fe.auth.domain.repository.AuthRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {
    
    override suspend fun login(identifier: String, password: String): Either<AppFailure, AuthData> {
        return Either.catch {
            val response = authApiService.login(LoginRequest(identifier, password))
            val metadata = response.metaData ?: throw Exception(response.message)
            
            AuthData(
                user = metadata.user.toDomainModel(),
                accessToken = metadata.accessToken,
                refreshToken = metadata.refreshToken
            )
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun register(email: String, username: String, password: String): Either<AppFailure, AuthData> {
        return Either.catch {
            val response = authApiService.register(RegisterRequest(email, username, password))
            val metadata = response.metaData ?: throw Exception(response.message)
            
            AuthData(
                user = metadata.user.toDomainModel(),
                accessToken = metadata.accessToken,
                refreshToken = metadata.refreshToken
            )
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun verifyOTP(email: String, otp: String): Either<AppFailure, Boolean> {
        return Either.catch {
            val response = authApiService.verifyOTP(VerifyOTPRequest(email, otp))
            response.metaData?.verified ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun resendOTP(email: String): Either<AppFailure, Boolean> {
        return Either.catch {
            authApiService.resendOTP(ResendOTPRequest(email))
            true
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun refreshToken(token: String): Either<AppFailure, String> {
        return Either.catch {
            val response = authApiService.refreshToken(RefreshTokenRequest(token))
            response.metaData?.accessToken ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun logout(token: String): Either<AppFailure, Unit> {
        return Either.catch {
            authApiService.logout(LogoutRequest(token))
            Unit
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getProfile(token: String): Either<AppFailure, User> {
        return Either.catch {
            val response = authApiService.getProfile()
            val userDto = response.metaData ?: throw Exception(response.message)
            userDto.toDomainModel()
        }.mapLeft { it.toAppFailure() }
    }
}

