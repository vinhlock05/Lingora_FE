package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyPasswordResetResponse(
    @SerializedName("resetToken")
    val resetToken: String?
)
