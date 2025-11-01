package com.example.lingora_fe.user.vocabulary.domain.model

data class UserTopicProgress(
    val id: Int,
    val userId: Int,
    val topicId: Int,
    val totalWords: Int,
    val completed: Boolean
)

