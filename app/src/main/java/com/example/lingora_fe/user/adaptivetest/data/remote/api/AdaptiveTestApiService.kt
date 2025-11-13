package com.example.lingora_fe.user.adaptivetest.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.adaptivetest.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AdaptiveTestApiService {
    
    @GET("adaptive-test/questions")
    suspend fun getQuestionBank(): ApiResponse<QuestionBankMetaData>
    
    @POST("adaptive-test/next")
    suspend fun getNextQuestion(
        @Body request: GetNextQuestionRequest
    ): ApiResponse<NextQuestionMetaData>
}

