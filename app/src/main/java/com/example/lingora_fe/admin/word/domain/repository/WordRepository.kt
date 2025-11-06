package com.example.lingora_fe.admin.word.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.word.domain.model.Word
import com.example.lingora_fe.core.error.AppFailure

data class WordFilterOptions(
    val search: String? = null,
    val sort: String? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val hasTopic: Boolean? = null,
    val cefrLevel: String? = null,
    val type: String? = null
)

data class WordListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val totalWords: Int? = null, // For topic/{id}/words response
    val words: List<Word>
)

interface WordRepository {
    suspend fun getAllWords(token: String, filter: WordFilterOptions): Either<AppFailure, WordListMetadata>
    suspend fun getTopicWords(token: String, topicId: Int, filter: WordFilterOptions): Either<AppFailure, WordListMetadata>
    suspend fun getWordById(token: String, wordId: Int): Either<AppFailure, Word>
    suspend fun createWord(token: String, word: Word): Either<AppFailure, Word>
    suspend fun updateWord(token: String, wordId: Int, word: Word): Either<AppFailure, Word>
    suspend fun deleteWord(token: String, wordId: Int): Either<AppFailure, Unit>
}


