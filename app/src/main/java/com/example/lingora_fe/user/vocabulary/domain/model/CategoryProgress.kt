package com.example.lingora_fe.user.vocabulary.domain.model

data class CategoryProgress(
    val id: Int,
    val name: String,
    val description: String,
    val totalTopics: Int,
    val completedTopics: Int,
    val progressPercent: Float,
    val completed: Boolean
)

