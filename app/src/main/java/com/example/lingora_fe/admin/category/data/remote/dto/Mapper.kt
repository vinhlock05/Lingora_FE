package com.example.lingora_fe.admin.category.data.remote.dto

import com.example.lingora_fe.admin.category.domain.model.Category
import com.example.lingora_fe.admin.category.domain.model.CategoryListMetadata
import com.example.lingora_fe.admin.category.domain.model.CreateCategoryData
import com.example.lingora_fe.admin.category.domain.model.UpdateCategoryData

// DTO to Domain
fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        description = description,
        totalTopics = totalTopics,
        createdAt = createdAt
    )
}

fun CategoryListMetaData.toDomain(): CategoryListMetadata {
    return CategoryListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        categories = categories.map { it.toDomain() }
    )
}

// Domain to DTO
fun CreateCategoryData.toDto(): CreateCategoryRequest {
    return CreateCategoryRequest(
        name = name,
        description = description
    )
}

fun UpdateCategoryData.toDto(): UpdateCategoryRequest {
    return UpdateCategoryRequest(
        name = name,
        description = description
    )
}

