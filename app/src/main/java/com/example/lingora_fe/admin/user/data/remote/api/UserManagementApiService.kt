package com.example.lingora_fe.admin.user.data.remote.api

import com.example.lingora_fe.admin.user.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface UserManagementApiService {

    @GET("user")
    suspend fun getAllUsers(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("proficiency") proficiency: String? = null,
        @Query("status") status: String? = null
    ): ApiResponse<UserListMetaData>

    @GET("user/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): ApiResponse<AdminUserDto>

    @POST("user")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): ApiResponse<AdminUserDto>

    @PATCH("user/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): ApiResponse<AdminUserDto>

    @PATCH("user/restore/{id}")
    suspend fun restoreUser(
        @Path("id") userId: Int
    ): ApiResponse<AdminUserDto>

    @DELETE("user/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Int
    ): ApiResponse<Any>
}

