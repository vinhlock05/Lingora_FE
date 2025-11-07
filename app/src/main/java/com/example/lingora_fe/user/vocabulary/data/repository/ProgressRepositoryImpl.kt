package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressRepository
import com.example.lingora_fe.user.vocabulary.domain.repository.ProgressSummary
import com.example.lingora_fe.user.vocabulary.domain.repository.StatisticItem
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) : ProgressRepository {

    override suspend fun createWordProgress(wordIds: List<Int>): Either<String, List<com.example.lingora_fe.user.vocabulary.domain.model.WordProgress>> {
        return try {
            val request = com.example.lingora_fe.user.vocabulary.data.remote.dto.CreateWordProgressRequest(wordIds)
            val response = remoteDataSource.createWordProgress(request)
            val userId = response.metaData?.userId ?: 0
            // Filter out null results (when word is null in response)
            val wordProgressList = response.metaData?.wordProgresses
                ?.mapNotNull { dto ->
                    try {
                        dto.toDomain(userId)
                    } catch (e: Exception) {
                        android.util.Log.e("ProgressRepositoryImpl", "Error converting WordProgressWithWordDto to domain: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
            Either.Right(wordProgressList)
        } catch (e: Exception) {
            android.util.Log.e("ProgressRepositoryImpl", "Error creating word progress", e)
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
            
            // Log for debugging
            android.util.Log.d("ProgressRepositoryImpl", "Update progress response: statusCode=${response.statusCode}")
            android.util.Log.d("ProgressRepositoryImpl", "WordProgresses count: ${response.metaData?.wordProgresses?.size ?: 0}")
            
            val userId = response.metaData?.userId ?: 0
            // Filter out null results (when word is null in response)
            val updatedWordProgressList = response.metaData?.wordProgresses
                ?.mapNotNull { dto ->
                    try {
                        dto.toDomain(userId)
                    } catch (e: Exception) {
                        android.util.Log.e("ProgressRepositoryImpl", "Error converting WordProgressWithWordDto to domain: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
            
            android.util.Log.d("ProgressRepositoryImpl", "Successfully converted ${updatedWordProgressList.size} word progresses")
            Either.Right(updatedWordProgressList)
        } catch (e: Exception) {
            android.util.Log.e("ProgressRepositoryImpl", "Error updating word progress", e)
            Either.Left(e.message ?: "Failed to update word progress")
        }
    }

    override suspend fun getProgressSummary(): Either<String, ProgressSummary> {
        return try {
            val response = remoteDataSource.getProgressSummary()
            
            // Log for debugging
            android.util.Log.d("ProgressRepositoryImpl", "Progress summary response: statusCode=${response.statusCode}")
            android.util.Log.d("ProgressRepositoryImpl", "MetaData: ${response.metaData}")
            android.util.Log.d("ProgressRepositoryImpl", "TotalLearnedWord: ${response.metaData?.totalLearnedWord}")
            android.util.Log.d("ProgressRepositoryImpl", "Statistics count: ${response.metaData?.statistics?.size ?: 0}")
            
            val metaData = response.metaData
            val summary = ProgressSummary(
                totalLearnedWord = metaData?.totalLearnedWord ?: 0,
                statistics = metaData?.statistics?.map {
                    StatisticItem(
                        srsLevel = it.srsLevel,
                        wordCount = it.wordCount
                    )
                } ?: emptyList()
            )
            
            android.util.Log.d("ProgressRepositoryImpl", "Mapped summary: totalLearnedWord=${summary.totalLearnedWord}, statistics=${summary.statistics.size}")
            Either.Right(summary)
        } catch (e: Exception) {
            android.util.Log.e("ProgressRepositoryImpl", "Error fetching progress summary", e)
            Either.Left(e.message ?: "Failed to fetch progress summary")
        }
    }
}

