package com.example.lingora_fe.core.network

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 * Wraps all API responses with consistent structure
 */
data class ApiResponse<T>(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("metaData")
    val metaData: T? = null
)

