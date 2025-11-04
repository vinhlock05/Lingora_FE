package com.example.lingora_fe.admin.user.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.user.data.remote.api.UserManagementApiService
import com.example.lingora_fe.admin.user.data.remote.dto.toDomain
import com.example.lingora_fe.admin.user.data.remote.dto.toDto
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.CreateUserData
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.model.UserFilterOptions
import com.example.lingora_fe.admin.user.domain.model.UserListMetadata
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class UserManagementRepositoryImpl @Inject constructor(
    private val apiService: UserManagementApiService
) : UserManagementRepository {

    override suspend fun getAllUsers(
        token: String,
        filterOptions: UserFilterOptions
    ): Either<AppFailure, UserListMetadata> {
        return Either.catch {
            val response = apiService.getAllUsers(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                proficiency = filterOptions.proficiency,
                status = filterOptions.status
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getUserById(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.getUserById(userId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createUser(
        token: String,
        userData: CreateUserData
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.createUser(userData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateUser(
        token: String,
        userId: Int,
        userData: UpdateUserData
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.updateUser(userId, userData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun restoreUser(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser> {
        return Either.catch {
            val response = apiService.restoreUser(userId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteUser(
        token: String,
        userId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteUser(userId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}

