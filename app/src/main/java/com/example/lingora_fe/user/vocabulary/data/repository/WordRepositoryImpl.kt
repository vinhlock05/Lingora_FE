package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) : WordRepository {

    override suspend fun getTopicWordsWithProgress(
        topicId: Int,
        limit: Int,
        page: Int,
        search: String?,
        hasLearned: Boolean?
    ): Either<String, Pair<com.example.lingora_fe.user.vocabulary.domain.repository.TopicWordProgressMeta, List<com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress>>> {
        return try {
            val response = remoteDataSource.getTopicWordsWithProgress(topicId, limit, page, search, hasLearned)
            
            // Log response for debugging
            android.util.Log.d("WordRepository", "Response: statusCode=${response.statusCode}, message=${response.message}")
            android.util.Log.d("WordRepository", "MetaData: ${response.metaData}")
            android.util.Log.d("WordRepository", "Words count: ${response.metaData?.words?.size ?: 0}")
            
            val words = (response.metaData?.words ?: emptyList()).map { dto ->
                val progress = dto.progress?.let { progressDto ->
                    com.example.lingora_fe.user.vocabulary.domain.model.WordProgress(
                        id = progressDto.id,
                        wordId = dto.id,
                        userId = 0, // Will be set from context
                        status = com.example.lingora_fe.user.vocabulary.domain.model.WordStatus.values().find { it.value == progressDto.status } ?: com.example.lingora_fe.user.vocabulary.domain.model.WordStatus.NEW,
                        srsLevel = progressDto.srsLevel,
                        learnedAt = progressDto.learnedAt?.let { parseDate(it) },
                        nextReviewDay = progressDto.nextReviewDay?.let { parseDate(it) },
                        wrongCount = progressDto.wrongCount ?: 0,
                        reviewedDate = progressDto.reviewedDate?.let { parseDate(it) },
                        createdAt = progressDto.createdAt?.let { parseDate(it) },
                        updatedAt = progressDto.updatedAt?.let { parseDate(it) }
                    )
                }
                com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress(
                    id = dto.id,
                    word = dto.word,
                    phonetic = dto.phonetic,
                    cefrLevel = dto.cefrLevel,
                    type = dto.type,
                    meaning = dto.meaning,
                    example = dto.example,
                    exampleTranslation = dto.exampleTranslation,
                    audioUrl = dto.audioUrl,
                    imageUrl = dto.imageUrl,
                    progress = progress
                )
            }
            val meta = response.metaData?.let {
                com.example.lingora_fe.user.vocabulary.domain.repository.TopicWordProgressMeta(
                    topicId = it.topicId,
                    totalWordsAll = it.totalWordsAll,
                    learnedCountAll = it.learnedCountAll,
                    completed = it.completed,
                    progressPercent = it.progressPercent,
                    currentPage = it.currentPage,
                    totalPages = it.totalPages,
                    totalWordsFiltered = it.totalWordsFiltered
                )
            } ?: run {
                android.util.Log.w("WordRepository", "MetaData is null, using default values")
                com.example.lingora_fe.user.vocabulary.domain.repository.TopicWordProgressMeta(
                    topicId = topicId,
                    totalWordsAll = 0,
                    learnedCountAll = 0,
                    completed = false,
                    progressPercent = 0f,
                    currentPage = page,
                    totalPages = 1,
                    totalWordsFiltered = words.size
                )
            }
            
            android.util.Log.d("WordRepository", "Returning: ${words.size} words, meta: $meta")
            Either.Right(Pair(meta, words))
        } catch (e: Exception) {
            android.util.Log.e("WordRepository", "Error fetching topic words", e)
            Either.Left(e.message ?: "Failed to fetch topic words with progress")
        }
    }

    override suspend fun getWordsForReview(
        limit: Int,
        page: Int
    ): Either<String, Pair<com.example.lingora_fe.user.vocabulary.domain.repository.ReviewWordsMeta, List<com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress>>> {
        return try {
            val response = remoteDataSource.getWordsForReview(limit, page)
            val words = response.words.map { dto ->
                val progress = dto.progress?.let { progressDto ->
                    com.example.lingora_fe.user.vocabulary.domain.model.WordProgress(
                        id = progressDto.id,
                        wordId = dto.id,
                        userId = 0, // Will be set from context
                        status = com.example.lingora_fe.user.vocabulary.domain.model.WordStatus.values().find { it.value == progressDto.status } ?: com.example.lingora_fe.user.vocabulary.domain.model.WordStatus.NEW,
                        srsLevel = progressDto.srsLevel,
                        learnedAt = progressDto.learnedAt?.let { parseDate(it) },
                        nextReviewDay = progressDto.nextReviewDay?.let { parseDate(it) },
                        wrongCount = progressDto.wrongCount ?: 0,
                        reviewedDate = progressDto.reviewedDate?.let { parseDate(it) },
                        createdAt = progressDto.createdAt?.let { parseDate(it) },
                        updatedAt = progressDto.updatedAt?.let { parseDate(it) }
                    )
                }
                com.example.lingora_fe.user.vocabulary.domain.model.WordWithProgress(
                    id = dto.id,
                    word = dto.word,
                    phonetic = dto.phonetic,
                    cefrLevel = dto.cefrLevel,
                    type = dto.type,
                    meaning = dto.meaning,
                    example = dto.example,
                    exampleTranslation = dto.exampleTranslation,
                    audioUrl = dto.audioUrl,
                    imageUrl = dto.imageUrl,
                    progress = progress
                )
            }
            val meta = response.metadata?.let {
                com.example.lingora_fe.user.vocabulary.domain.repository.ReviewWordsMeta(
                    page = it.page,
                    limit = it.limit,
                    total = it.total
                )
            } ?: com.example.lingora_fe.user.vocabulary.domain.repository.ReviewWordsMeta(
                page = page,
                limit = limit,
                total = words.size
            )
            Either.Right(Pair(meta, words))
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to fetch words for review")
        }
    }

    private fun parseDate(dateString: String): java.util.Date? {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

