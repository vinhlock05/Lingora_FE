package com.example.lingora_fe.admin.user.data.remote.dto

import com.google.gson.annotations.SerializedName

// MetaData DTOs
data class UserListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("users")
    val users: List<AdminUserDto>
)

data class AdminUserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("roles")
    val roles: List<UserRoleDto>,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("proficiency")
    val proficiency: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("suspendedUntil")
    val suspendedUntil: String? = null,
    @SerializedName("banReason")
    val banReason: String? = null
)

data class UserRoleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

// Request DTOs
data class CreateUserRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("roleIds")
    val roleIds: List<Int>,
    @SerializedName("proficiency")
    val proficiency: String
)

data class UpdateUserRequest(
    @SerializedName("username")
    val username: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("newPassword")
    val newPassword: String?,
    @SerializedName("oldPassword")
    val oldPassword: String?,
    @SerializedName("roleIds")
    val roleIds: List<Int>?,
    @SerializedName("proficiency")
    val proficiency: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("banReason")
    val banReason: String? = null,
    @SerializedName("suspendedUntil")
    val suspendedUntil: String? = null
)

// Request for updating only proficiency field
data class UpdateProficiencyRequest(
    @SerializedName("proficiency")
    val proficiency: String
)

