package com.example.lingora_fe.admin.word.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.word.data.remote.api.CreateWordRequest
import com.example.lingora_fe.admin.word.data.remote.api.UpdateWordRequest
import com.example.lingora_fe.admin.word.data.remote.api.WordApiService
import com.example.lingora_fe.admin.word.data.remote.api.WordListMetaData
import com.example.lingora_fe.admin.word.data.remote.dto.toDomain
import com.example.lingora_fe.admin.word.domain.model.Word
import com.example.lingora_fe.admin.word.domain.repository.WordFilterOptions
import com.example.lingora_fe.admin.word.domain.repository.WordListMetadata
import com.example.lingora_fe.admin.word.domain.repository.WordRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val api: WordApiService
) : WordRepository {

    override suspend fun getAllWords(token: String, filter: WordFilterOptions): Either<AppFailure, WordListMetadata> {
        return Either.catch {
            val res = api.getAllWords(
                limit = filter.limit,
                page = filter.page,
                search = filter.search,
                sort = filter.sort,
                hasTopic = filter.hasTopic,
                cefrLevel = filter.cefrLevel,
                type = filter.type
            )
            res.metaData!!.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getTopicWords(token: String, topicId: Int, filter: WordFilterOptions): Either<AppFailure, WordListMetadata> {
        return Either.catch {
            val res = api.getTopicWords(
                topicId = topicId,
                limit = filter.limit,
                page = filter.page,
                search = filter.search,
                sort = filter.sort,
                cefrLevel = filter.cefrLevel,
                type = filter.type
            )
            res.metaData!!.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getWordById(token: String, wordId: Int): Either<AppFailure, Word> {
        return Either.catch { api.getWordById(wordId).metaData!!.toDomain() }
            .mapLeft { it.toAppFailure() }
    }

    override suspend fun createWord(token: String, word: Word): Either<AppFailure, Word> {
        return Either.catch {
            val res = api.createWord(
                CreateWordRequest(
                    word = word.word,
                    meaning = word.meaning,
                    phonetic = word.phonetic,
                    cefrLevel = word.cefrLevel,
                    type = word.type,
                    example = word.example,
                    exampleTranslation = word.exampleTranslation,
                    audioUrl = word.audioUrl,
                    imageUrl = word.imageUrl,
                    topicId = word.topicId
                )
            )
            res.metaData!!.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateWord(token: String, wordId: Int, word: Word): Either<AppFailure, Word> {
        return Either.catch {
            val res = api.updateWord(
                wordId,
                UpdateWordRequest(
                    word = word.word,
                    meaning = word.meaning,
                    phonetic = word.phonetic,
                    cefrLevel = word.cefrLevel,
                    type = word.type,
                    example = word.example,
                    exampleTranslation = word.exampleTranslation,
                    audioUrl = word.audioUrl,
                    imageUrl = word.imageUrl,
                    topicId = word.topicId
                )
            )
            res.metaData!!.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteWord(token: String, wordId: Int): Either<AppFailure, Unit> {
        return Either.catch { api.deleteWord(wordId); Unit }.mapLeft { it.toAppFailure() }
    }
}

// Mapper from API metadata to domain metadata
private fun WordListMetaData.toDomain(): WordListMetadata = WordListMetadata(
    currentPage = currentPage,
    totalPages = totalPages,
    total = total,
    totalWords = totalWords,
    words = words.map { it.toDomain() }
)


