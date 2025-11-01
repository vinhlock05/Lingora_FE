package com.example.lingora_fe.user.vocabulary.domain.model

data class Word(
    val id: Int,
    val topicId: Int,
    val level: String,
    val word: String,
    val meaning: String,
    val example: String,
    val exampleTranslation: String,
    val position: Int,
    val audioUrl: String,
    val imageUrl: String
)

