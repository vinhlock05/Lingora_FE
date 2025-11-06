package com.example.lingora_fe.admin.user.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.user.domain.model.AdminUser
import com.example.lingora_fe.admin.user.domain.model.CreateUserData
import com.example.lingora_fe.admin.user.domain.model.UpdateUserData
import com.example.lingora_fe.admin.user.domain.model.UserFilterOptions
import com.example.lingora_fe.admin.user.domain.model.UserListMetadata
import com.example.lingora_fe.core.error.AppFailure

interface UserManagementRepository {

    /**
     * Get list of users with filtering and pagination
     */
    suspend fun getAllUsers(
        token: String,
        filterOptions: UserFilterOptions
    ): Either<AppFailure, UserListMetadata>

    /**
     * Get user details by ID
     */
    suspend fun getUserById(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser>

    /**
     * Create a new user
     */
    suspend fun createUser(
        token: String,
        userData: CreateUserData
    ): Either<AppFailure, AdminUser>

    /**
     * Update existing user
     */
    suspend fun updateUser(
        token: String,
        userId: Int,
        userData: UpdateUserData
    ): Either<AppFailure, AdminUser>

    /**
     * Restore a deleted user
     */
    suspend fun restoreUser(
        token: String,
        userId: Int
    ): Either<AppFailure, AdminUser>

    /**
     * Delete a user (soft delete)
     */
    suspend fun deleteUser(
        token: String,
        userId: Int
    ): Either<AppFailure, Unit>
}

