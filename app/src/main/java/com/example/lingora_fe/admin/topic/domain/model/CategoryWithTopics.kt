package com.example.lingora_fe.admin.topic.domain.model

/**
 * Model for Category with its Topics
 * Used when getting /category/{id}/topics
 */
data class CategoryWithTopics(
    val id: Int,
    val name: String,
    val description: String,
    val totalTopics: Int,
    val currentPage: Int,
    val totalPages: Int,
    val topics: List<Topic>
)

