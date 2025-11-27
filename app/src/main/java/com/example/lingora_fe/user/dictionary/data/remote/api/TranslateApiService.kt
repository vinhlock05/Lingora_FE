package com.example.lingora_fe.user.dictionary.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.dictionary.data.remote.dto.TranslatePhraseRequest
import com.example.lingora_fe.user.dictionary.data.remote.dto.TranslatePhraseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslateApiService {

    @POST("translate/phrase")
    suspend fun translatePhrase(
        @Body body: TranslatePhraseRequest
    ): ApiResponse<TranslatePhraseDto>
}



