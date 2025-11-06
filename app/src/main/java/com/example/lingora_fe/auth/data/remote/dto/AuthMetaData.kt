package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthMetaData(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null
)

