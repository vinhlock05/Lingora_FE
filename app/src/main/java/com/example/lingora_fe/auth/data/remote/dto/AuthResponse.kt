package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("metaData")
    val metaData: AuthMetaData?
)

data class AuthMetaData(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("accessToken")
    val accessToken: String
)

