package com.example.lingora_fe.admin.user.data.remote.api

import com.example.lingora_fe.admin.user.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface UserManagementApiService {

    @GET("users")
    suspend fun getAllUsers(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("proficiency") proficiency: String? = null,
        @Query("status") status: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<UserListMetaData>

    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): ApiResponse<AdminUserDto>

    @POST("users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): ApiResponse<AdminUserDto>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): ApiResponse<AdminUserDto>

    @PATCH("users/restore/{id}")
    suspend fun restoreUser(
        @Path("id") userId: Int
    ): ApiResponse<AdminUserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Int
    ): ApiResponse<Any>
}

