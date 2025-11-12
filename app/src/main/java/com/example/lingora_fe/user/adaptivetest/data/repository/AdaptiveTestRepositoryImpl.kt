package com.example.lingora_fe.user.adaptivetest.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.adaptivetest.data.remote.api.AdaptiveTestApiService
import com.example.lingora_fe.user.adaptivetest.data.remote.dto.*
import com.example.lingora_fe.user.adaptivetest.domain.model.AdaptiveTestAnswer
import com.example.lingora_fe.user.adaptivetest.domain.repository.AdaptiveTestRepository
import javax.inject.Inject

class AdaptiveTestRepositoryImpl @Inject constructor(
    private val apiService: AdaptiveTestApiService
) : AdaptiveTestRepository {
    
    override suspend fun getQuestionBank(): Either<AppFailure, com.example.lingora_fe.user.adaptivetest.domain.model.QuestionBank> {
        return Either.catch {
            val response = apiService.getQuestionBank()
            val metadata = response.metaData ?: throw Exception(response.message)
            metadata.toDomainModel()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getNextQuestion(
        answeredQuestions: List<AdaptiveTestAnswer>
    ): Either<AppFailure, com.example.lingora_fe.user.adaptivetest.domain.model.NextQuestionResult> {
        return Either.catch {
            val request = GetNextQuestionRequest(
                answeredQuestions = answeredQuestions.map { it.toDto() }
            )
            val response = apiService.getNextQuestion(request)
            val metadata = response.metaData ?: throw Exception(response.message)
            metadata.toDomainModel()
        }.mapLeft { it.toAppFailure() }
    }
}

