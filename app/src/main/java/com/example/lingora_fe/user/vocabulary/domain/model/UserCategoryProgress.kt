package com.example.lingora_fe.user.vocabulary.domain.model

data class UserCategoryProgress(
    val id: Int,
    val userId: Int,
    val categoryId: Int,
    val totalTopics: Int,
    val progressPercent: Float,
    val completed: Boolean
)

