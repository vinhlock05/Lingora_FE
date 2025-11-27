package com.example.lingora_fe.user.dictionary.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.dictionary.data.remote.TranslateRemoteDataSource
import com.example.lingora_fe.user.dictionary.domain.model.TranslateResult
import com.example.lingora_fe.user.dictionary.domain.repository.TranslateRepository
import javax.inject.Inject

class TranslateRepositoryImpl @Inject constructor(
    private val remoteDataSource: TranslateRemoteDataSource
) : TranslateRepository {

    override suspend fun translatePhrase(
        text: String,
        sourceLang: String?,
        targetLang: String?
    ): Either<AppFailure, TranslateResult> {
        return Either.catch {
            val response = remoteDataSource.translatePhrase(text, sourceLang, targetLang)
            response.metaData!!.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
}



