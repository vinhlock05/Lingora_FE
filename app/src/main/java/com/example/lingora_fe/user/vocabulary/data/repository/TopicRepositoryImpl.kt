package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.repository.TopicRepository
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) : TopicRepository {

    override suspend fun getCategoryTopicsWithProgress(
        categoryId: Int,
        limit: Int,
        page: Int,
        search: String?,
        sort: String?
    ): Either<String, Pair<com.example.lingora_fe.user.vocabulary.domain.repository.CategoryTopicProgressMeta, List<com.example.lingora_fe.user.vocabulary.domain.model.TopicProgress>>> {
        return try {
            val response = remoteDataSource.getCategoryTopicsWithProgress(categoryId, limit, page, search, sort)
            val topics = response.metaData?.topics?.map { dto ->
                com.example.lingora_fe.user.vocabulary.domain.model.TopicProgress(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    totalWords = dto.totalWords,
                    learnedWords = dto.learnedWords,
                    completed = dto.completed
                )
            } ?: emptyList()
            val meta = response.metaData?.let {
                com.example.lingora_fe.user.vocabulary.domain.repository.CategoryTopicProgressMeta(
                    categoryId = it.categoryId,
                    name = it.name,
                    description = it.description,
                    totalTopics = it.totalTopics,
                    completedTopics = it.completedTopics,
                    progressPercent = it.progressPercent,
                    completed = it.completed,
                    currentPage = it.currentPage,
                    totalPages = it.totalPages
                )
            } ?: com.example.lingora_fe.user.vocabulary.domain.repository.CategoryTopicProgressMeta(
                categoryId = categoryId,
                name = "",
                description = "",
                totalTopics = 0,
                completedTopics = 0,
                progressPercent = 0f,
                completed = false,
                currentPage = page,
                totalPages = 1
            )
            Either.Right(Pair(meta, topics))
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to fetch category topics with progress")
        }
    }

    override suspend fun getWordsForStudy(
        topicId: Int,
        count: Int
    ): Either<String, List<Word>> {
        return try {
            android.util.Log.d("TopicRepositoryImpl", "Getting words for study: topicId=$topicId, count=$count")
            val response = remoteDataSource.getWordsForStudy(topicId, count)
            android.util.Log.d("TopicRepositoryImpl", "Response: statusCode=${response.statusCode}, message=${response.message}")
            android.util.Log.d("TopicRepositoryImpl", "MetaData: ${response.metaData}")
            android.util.Log.d("TopicRepositoryImpl", "MetaData type: ${response.metaData?.javaClass?.name}")
            android.util.Log.d("TopicRepositoryImpl", "Words in metaData: ${response.metaData?.words?.size ?: 0}")
            
            // Log each word in detail
            response.metaData?.words?.forEachIndexed { index, wordDto ->
                android.util.Log.d("TopicRepositoryImpl", "Word[$index]: id=${wordDto.id}, word=${wordDto.word}, phonetic=${wordDto.phonetic}, audioUrl=${wordDto.audioUrl}, meaning=${wordDto.meaning}, example=${wordDto.example}")
            }
            
            val words = response.metaData?.words?.map { it.toDomain() } ?: emptyList()
            android.util.Log.d("TopicRepositoryImpl", "Mapped ${words.size} words to domain")
            
            // Log mapped words
            words.forEachIndexed { index, word ->
                android.util.Log.d("TopicRepositoryImpl", "Mapped Word[$index]: id=${word.id}, word=${word.word}, phonetic=${word.phonetic}, audioUrl=${word.audioUrl}, meaning=${word.meaning}")
            }
            
            if (words.isEmpty()) {
                android.util.Log.w("TopicRepositoryImpl", "No words returned! Check API response.")
            }
            
            Either.Right(words)
        } catch (e: Exception) {
            android.util.Log.e("TopicRepositoryImpl", "Error getting words for study", e)
            e.printStackTrace()
            Either.Left(e.message ?: "Failed to fetch words for study")
        }
    }
}

