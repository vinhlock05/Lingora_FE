package com.example.lingora_fe.user.vocabulary.domain.model

data class CategoryTopic(
    val id: Int,
    val categoryId: Int,
    val topicId: Int,
    val orderIndex: Int
)

