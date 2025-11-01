package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.Category
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class CategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description,
            createdAt = Timestamp.valueOf(createdAt)
        )
    }
}

data class CategoryResponse(
    @SerializedName("data")
    val data: CategoryDto
)

data class CategoriesResponse(
    @SerializedName("data")
    val data: List<CategoryDto>
)

