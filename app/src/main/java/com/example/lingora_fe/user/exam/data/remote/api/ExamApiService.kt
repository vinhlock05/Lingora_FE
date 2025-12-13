package com.example.lingora_fe.user.exam.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.exam.data.remote.dto.ExamAttemptDto
import com.example.lingora_fe.user.exam.data.remote.dto.ExamDto
import com.example.lingora_fe.user.exam.data.remote.dto.ExamListMetaDataDto
import com.example.lingora_fe.user.exam.data.remote.dto.ExamQuestionDto
import com.example.lingora_fe.user.exam.data.remote.dto.ExamSectionDto
import com.example.lingora_fe.user.exam.data.remote.dto.FinalizeAttemptResponseDto
import com.example.lingora_fe.user.exam.data.remote.dto.StartAttemptRequestDto
import com.example.lingora_fe.user.exam.data.remote.dto.SubmitSectionRequestDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptListMetaDataDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExamApiService {
    @GET("exams")
    suspend fun getExams(
        @Query("examType") examType: String? = null,
        @Query("isPublished") isPublished: Boolean? = true,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<ExamListMetaDataDto>

    @GET("exams/{examId}")
    suspend fun getExamById(
        @Path("examId") examId: Int
    ): ApiResponse<ExamDto>

    @GET("exams/{examId}/sections/{sectionId}")
    suspend fun getSection(
        @Path("examId") examId: Int,
        @Path("sectionId") sectionId: Int
    ): ApiResponse<ExamSectionDto>

    @POST("exams/{examId}/start")
    suspend fun startExamAttempt(
        @Path("examId") examId: Int,
        @Body body: StartAttemptRequestDto
    ): ApiResponse<ExamAttemptDto>

    @POST("exam-attempts/{attemptId}/sections/{sectionId}/submit")
    suspend fun submitSection(
        @Path("attemptId") attemptId: Int,
        @Path("sectionId") sectionId: Int,
        @Body body: SubmitSectionRequestDto
    ): ApiResponse<ExamAttemptDto>

    @POST("exam-attempts/{attemptId}/submit")
    suspend fun submitAttempt(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<FinalizeAttemptResponseDto>

    @GET("exam-attempts")
    suspend fun getAttempts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<AttemptListMetaDataDto>

    @GET("exam-attempts/{attemptId}")
    suspend fun getAttemptDetail(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<AttemptDetailResponseDto>
}

