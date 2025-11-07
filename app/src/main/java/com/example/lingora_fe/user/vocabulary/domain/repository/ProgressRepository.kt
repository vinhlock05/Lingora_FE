package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.WordProgress

interface ProgressRepository {
    // Word Progress methods
    suspend fun createWordProgress(wordIds: List<Int>): Either<String, List<WordProgress>>
    suspend fun updateWordProgress(
        wordProgressList: List<Triple<Int, Int, String>>
    ): Either<String, List<WordProgress>> // wordId, wrongCount, reviewedDate
}

