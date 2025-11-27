package com.example.lingora_fe.user.dictionary.data.remote

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.dictionary.data.remote.api.TranslateApiService
import com.example.lingora_fe.user.dictionary.data.remote.dto.TranslatePhraseDto
import com.example.lingora_fe.user.dictionary.data.remote.dto.TranslatePhraseRequest
import javax.inject.Inject

class TranslateRemoteDataSource @Inject constructor(
    private val apiService: TranslateApiService
) {

    suspend fun translatePhrase(
        text: String,
        sourceLang: String? = null,
        targetLang: String? = null
    ): ApiResponse<TranslatePhraseDto> {
        return apiService.translatePhrase(
            TranslatePhraseRequest(
                text = text,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
        )
    }
}



