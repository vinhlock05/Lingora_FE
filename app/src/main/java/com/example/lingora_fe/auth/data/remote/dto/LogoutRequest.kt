package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

