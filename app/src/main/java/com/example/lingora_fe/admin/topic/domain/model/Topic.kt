package com.example.lingora_fe.admin.topic.domain.model

data class Topic(
    val id: Int,
    val name: String,
    val description: String,
    val category: TopicCategory?,
    val totalWords: Int = 0,
    val createdAt: String?
)

data class TopicCategory(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: String?
)

data class TopicListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val topics: List<Topic>
)

// Request data classes for domain layer
data class CreateTopicData(
    val name: String,
    val description: String,
    val categoryId: Int?
)

data class UpdateTopicData(
    val name: String? = null,
    val description: String? = null,
    val categoryId: Int? = null
)

// Filter data for queries
data class TopicFilterOptions(
    val search: String? = null,
    val sort: String? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val hasCategory: Boolean? = null // Filter by hasCategory (true/false/null for all)
)

// Enums for type safety
enum class TopicSortOption(val displayName: String, val apiValue: String) {
    ID_DESC("ID (Newest First)", "-id"),
    ID_ASC("ID (Oldest First)", "+id"),
    NAME_ASC("Name (A-Z)", "+name"),
    NAME_DESC("Name (Z-A)", "-name")
}

