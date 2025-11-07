package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.core.network.ApiResponse
import com.google.gson.annotations.SerializedName

data class CategoryProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalTopics")
    val totalTopics: Int,
    @SerializedName("completedTopics")
    val completedTopics: Int,
    @SerializedName("progressPercent")
    val progressPercent: Float,
    @SerializedName("completed")
    val completed: Boolean
)

data class CategoryProgressListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("categories")
    val categories: List<CategoryProgressDto>
)

typealias CategoryProgressListResponse = ApiResponse<CategoryProgressListMetaData>

