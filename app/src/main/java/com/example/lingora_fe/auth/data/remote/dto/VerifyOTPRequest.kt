package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyOTPRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

