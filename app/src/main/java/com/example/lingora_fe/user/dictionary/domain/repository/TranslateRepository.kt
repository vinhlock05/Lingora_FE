package com.example.lingora_fe.user.dictionary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.dictionary.domain.model.TranslateResult

interface TranslateRepository {

    suspend fun translatePhrase(
        text: String,
        sourceLang: String? = null,
        targetLang: String? = null
    ): Either<AppFailure, TranslateResult>
}



