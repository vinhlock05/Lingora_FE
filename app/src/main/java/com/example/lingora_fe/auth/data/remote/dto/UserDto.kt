package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("roles")
    val roles: List<RoleDto>,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("proficiency")
    val proficiency: String,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("deletedAt")
    val deletedAt: String?
)

data class RoleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

