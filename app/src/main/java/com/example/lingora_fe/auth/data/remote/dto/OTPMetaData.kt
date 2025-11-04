package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OTPMetaData(
    @SerializedName("verified")
    val verified: Boolean
)

