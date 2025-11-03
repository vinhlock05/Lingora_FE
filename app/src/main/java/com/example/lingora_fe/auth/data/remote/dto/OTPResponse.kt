package com.example.lingora_fe.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OTPResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("metaData")
    val metaData: OTPMetaData?
)

data class OTPMetaData(
    @SerializedName("verified")
    val verified: Boolean
)

