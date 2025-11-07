package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) : ProgressRepository {

    override suspend fun createWordProgress(wordIds: List<Int>): Either<String, List<com.example.lingora_fe.user.vocabulary.domain.model.WordProgress>> {
        return try {
            val request = com.example.lingora_fe.user.vocabulary.data.remote.dto.CreateWordProgressRequest(wordIds)
            val response = remoteDataSource.createWordProgress(request)
            val userId = response.metaData?.userId ?: 0
            val wordProgressList = response.metaData?.wordProgresses?.map { dto ->
                dto.toDomain(userId)
            } ?: emptyList()
            Either.Right(wordProgressList)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to create word progress")
        }
    }

    override suspend fun updateWordProgress(
        wordProgressList: List<Triple<Int, Int, String>>
    ): Either<String, List<com.example.lingora_fe.user.vocabulary.domain.model.WordProgress>> {
        return try {
            val request = com.example.lingora_fe.user.vocabulary.data.remote.dto.UpdateWordProgressRequest(
                wordProgress = wordProgressList.map { (wordId, wrongCount, reviewedDate) ->
                    com.example.lingora_fe.user.vocabulary.data.remote.dto.WordProgressRequest(
                        wordId = wordId,
                        wrongCount = wrongCount,
                        reviewedDate = reviewedDate
                    )
                }
            )
            val response = remoteDataSource.updateWordProgress(request)
            val userId = response.metaData?.userId ?: 0
            val updatedWordProgressList = response.metaData?.wordProgresses?.map { dto ->
                dto.toDomain(userId)
            } ?: emptyList()
            Either.Right(updatedWordProgressList)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to update word progress")
        }
    }
}

