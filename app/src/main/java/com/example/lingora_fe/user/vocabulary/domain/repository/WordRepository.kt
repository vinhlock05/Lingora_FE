package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    suspend fun getWordsByTopic(topicId: Int): Either<String, List<Word>>
    suspend fun getWordById(wordId: Int): Either<String, Word>
    fun observeWordsByTopic(topicId: Int): Flow<List<Word>>
}

