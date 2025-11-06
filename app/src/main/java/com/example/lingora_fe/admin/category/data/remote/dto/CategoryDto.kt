package com.example.lingora_fe.admin.category.data.remote.dto

import com.google.gson.annotations.SerializedName

// MetaData DTOs
data class CategoryListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("categories")
    val categories: List<CategoryDto>
)

data class CategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("totalTopics")
    val totalTopics: Int = 0,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

// Request DTOs
data class CreateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
)

data class UpdateCategoryRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?
)

