package com.example.lingora_fe.user.forum.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LikeDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("targetId")
    val targetId: Int,
    @SerializedName("targetType")
    val targetType: String,
    @SerializedName("createdBy")
    val createdBy: UserDto? = null
)


