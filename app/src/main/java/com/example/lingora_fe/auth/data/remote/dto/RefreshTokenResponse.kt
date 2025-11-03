package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("metaData")
    val metaData: RefreshTokenMetaData?
)

data class RefreshTokenMetaData(
    @SerializedName("accessToken")
    val accessToken: String
)

