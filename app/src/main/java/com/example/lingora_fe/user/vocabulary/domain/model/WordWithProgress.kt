package com.example.lingora_fe.user.vocabulary.domain.model

data class WordWithProgress(
    val id: Int,
    val word: String,
    val phonetic: String?,
    val cefrLevel: String,
    val type: String,
    val meaning: String?,
    val vnMeaning: String?,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val progress: WordProgress?
)

