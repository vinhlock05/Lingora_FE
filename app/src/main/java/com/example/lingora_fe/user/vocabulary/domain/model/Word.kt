package com.example.lingora_fe.user.vocabulary.domain.model

import com.example.lingora_fe.admin.word.domain.model.CefrLevel

data class Word(
    val id: Int,
    val topicId: Int,
    val cefrLevel: CefrLevel?,
    val phonetic: String?,
    val type: String?,
    val word: String,
    val meaning: String?,
    val vnMeaning: String?,
    val example: String?,
    val exampleTranslation: String?,
    val audioUrl: String?,
    val imageUrl: String?
)

