package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResendOTPRequest(
    @SerializedName("email")
    val email: String
)

