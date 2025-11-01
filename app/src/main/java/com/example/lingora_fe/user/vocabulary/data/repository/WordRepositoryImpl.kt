package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.local.VocabularyLocalDataSource
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.data.local.entity.WordEntity
import com.example.lingora_fe.user.vocabulary.domain.model.Word
import com.example.lingora_fe.user.vocabulary.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource
) : WordRepository {

    override suspend fun getWordsByTopic(topicId: Int): Either<String, List<Word>> {
        return try {
            val response = remoteDataSource.getWordsByTopic(topicId)
            val words = response.data.map { it.toDomain() }
            
            // Cache in local database
            val entities = response.data.map { dto ->
                WordEntity(
                    id = dto.id,
                    topicId = dto.topicId,
                    level = dto.level,
                    word = dto.word,
                    meaning = dto.meaning,
                    example = dto.example,
                    exampleTranslation = dto.exampleTranslation,
                    position = dto.position,
                    audioUrl = dto.audioUrl,
                    imageUrl = dto.imageUrl
                )
            }
            localDataSource.insertWords(entities)
            
            Either.Right(words)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localWords = localDataSource.getWordsByTopic(topicId).map { it.toDomain() }
                Either.Right(localWords)
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch words")
            }
        }
    }

    override suspend fun getWordById(wordId: Int): Either<String, Word> {
        return try {
            val response = remoteDataSource.getWordById(wordId)
            val word = response.data.toDomain()
            
            // Cache in local database
            val entity = WordEntity(
                id = response.data.id,
                topicId = response.data.topicId,
                level = response.data.level,
                word = response.data.word,
                meaning = response.data.meaning,
                example = response.data.example,
                exampleTranslation = response.data.exampleTranslation,
                position = response.data.position,
                audioUrl = response.data.audioUrl,
                imageUrl = response.data.imageUrl
            )
            localDataSource.insertWord(entity)
            
            Either.Right(word)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localWord = localDataSource.getWordById(wordId)?.toDomain()
                if (localWord != null) {
                    Either.Right(localWord)
                } else {
                    Either.Left("Word not found")
                }
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch word")
            }
        }
    }

    override fun observeWordsByTopic(topicId: Int): Flow<List<Word>> {
        return localDataSource.observeWordsByTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

