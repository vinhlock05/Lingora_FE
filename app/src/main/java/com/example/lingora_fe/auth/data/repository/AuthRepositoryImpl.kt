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

    override suspend fun googleLogin(idToken: String): Either<AppFailure, AuthData> {
        return Either.catch {
            val response = authApiService.googleLogin(GoogleLoginRequest(idToken))
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
    

    
    override suspend fun refreshToken(token: String): Either<AppFailure, String> {
        return Either.catch {
            // Gọi refresh token endpoint không cần body
            // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
            val response = authApiService.refreshToken()
            response.metaData?.accessToken ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun logout(token: String): Either<AppFailure, Unit> {
        return Either.catch {
            // Gọi logout endpoint không cần body
            // Cookie (refreshToken) sẽ tự động được gửi bởi CookieJar
            authApiService.logout()
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
    
    override suspend fun sendPasswordResetEmail(email: String): Either<AppFailure, Boolean> {
        return Either.catch {
            authApiService.sendPasswordResetEmail(SendPasswordResetRequest(email))
            true
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun verifyPasswordResetOtp(email: String, code: String): Either<AppFailure, String> {
        return Either.catch {
            val response = authApiService.verifyPasswordResetOtp(code, VerifyPasswordResetRequest(email))
            response.metaData?.resetToken ?: throw Exception("Reset token not found in response")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun confirmPasswordReset(resetToken: String, newPassword: String): Either<AppFailure, Boolean> {
        return Either.catch {
            authApiService.confirmPasswordReset(
                authorization = "Bearer $resetToken",
                request = ConfirmPasswordResetRequest(newPassword)
            )
            true
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun sendEmailVerification(): Either<AppFailure, Boolean> {
        return Either.catch {
            authApiService.sendEmailVerification()
            true
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun verifyEmail(code: String): Either<AppFailure, User> {
        return Either.catch {
            val response = authApiService.verifyEmail(VerifyAccountRequest(code))
            val userDto = response.metaData ?: throw Exception(response.message)
            userDto.toDomainModel()
        }.mapLeft { it.toAppFailure() }
    }
}

