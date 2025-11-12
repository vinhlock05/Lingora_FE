package com.example.lingora_fe.user.adaptivetest.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.adaptivetest.domain.model.*

interface AdaptiveTestRepository {
    suspend fun getQuestionBank(): Either<AppFailure, QuestionBank>
    suspend fun getNextQuestion(answeredQuestions: List<AdaptiveTestAnswer>): Either<AppFailure, NextQuestionResult>
}

