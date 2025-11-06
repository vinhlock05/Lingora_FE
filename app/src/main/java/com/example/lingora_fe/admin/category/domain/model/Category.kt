package com.example.lingora_fe.admin.category.domain.model

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val totalTopics: Int = 0,
    val createdAt: String? = null
)

data class CategoryListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val categories: List<Category>
)

// Request data classes for domain layer
data class CreateCategoryData(
    val name: String,
    val description: String
)

data class UpdateCategoryData(
    val name: String? = null,
    val description: String? = null
)

// Filter data for queries
data class CategoryFilterOptions(
    val search: String? = null,
    val sort: String? = null,
    val page: Int = 1,
    val limit: Int = 20
)

// Enums for type safety
enum class CategorySortOption(val displayName: String, val apiValue: String) {
    ID_DESC("ID (Newest First)", "-id"),
    ID_ASC("ID (Oldest First)", "+id"),
    NAME_ASC("Name (A-Z)", "+name"),
    NAME_DESC("Name (Z-A)", "-name"),
    CREATED_AT_DESC("Created Date (Newest)", "-createdAt"),
    CREATED_AT_ASC("Created Date (Oldest)", "+createdAt")
}

