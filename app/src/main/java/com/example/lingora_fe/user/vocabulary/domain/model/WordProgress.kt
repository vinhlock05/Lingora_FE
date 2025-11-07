package com.example.lingora_fe.user.vocabulary.domain.model

import java.util.Date

data class WordProgress(
    val id: Int,
    val wordId: Int,
    val userId: Int,
    val status: WordStatus,
    val srsLevel: Int,
    val learnedAt: Date?,
    val nextReviewDay: Date?,
    val wrongCount: Int = 0,
    val reviewedDate: Date? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

