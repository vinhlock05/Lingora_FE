package com.example.lingora_fe.user.vocabulary.domain.model

data class TopicProgress(
    val id: Int,
    val name: String,
    val description: String,
    val totalWords: Int,
    val learnedWords: Int,
    val completed: Boolean
)

