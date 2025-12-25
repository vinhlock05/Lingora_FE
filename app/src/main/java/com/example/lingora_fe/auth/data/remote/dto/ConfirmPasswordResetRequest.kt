package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConfirmPasswordResetRequest(
    @SerializedName("newPassword")
    val newPassword: String
)
