package com.example.lingora_fe.admin.exam.data.remote.api

import com.example.lingora_fe.admin.exam.data.remote.dto.*
import com.example.lingora_fe.user.exam.data.remote.dto.ExamDto
import com.example.lingora_fe.user.exam.data.remote.dto.AttemptDetailResponseDto
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface AdminExamApiService {

    /**
     * Get list of exams with filtering
     */
    @GET("exams")
    suspend fun getExams(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("examType") examType: String? = null,
        @Query("isPublished") isPublished: Boolean? = null
    ): ApiResponse<AdminExamListResponse>

    /**
     * Get exam detail (reusing User API)
     */
    @GET("exams/{id}")
    suspend fun getExamDetail(
        @Path("id") id: Int
    ): ApiResponse<ExamDto>

    /**
     * Import new exam from JSON
     */
    @POST("admin/exams/import")
    suspend fun importExam(
        @Body examJson: List<ImportExamBodyReq>
    ): ApiResponse<List<ImportExamResponse>>

    /**
     * Update basic exam info
     */
    @PATCH("admin/exams/{id}")
    suspend fun updateExam(
        @Path("id") id: Int,
        @Body request: AdminUpdateExamRequest
    ): ApiResponse<AdminExamDto>

    /**
     * Delete exam
     */
    @DELETE("admin/exams/{id}")
    suspend fun deleteExam(
        @Path("id") id: Int
    ): ApiResponse<Unit>

    /**
     * Get list of exam attempts (history)
     */
    @GET("admin/exam-attempts")
    suspend fun getExamAttempts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("userId") userId: Int? = null,
        @Query("examId") examId: Int? = null,
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("minScore") minScore: Double? = null,
        @Query("maxScore") maxScore: Double? = null
    ): ApiResponse<AdminExamAttemptListResponse>

    /**
     * Get detail of an exam attempt
     */
    @GET("admin/exam-attempts/{id}")
    suspend fun getExamAttemptDetail(
        @Path("id") id: Int
    ): ApiResponse<AttemptDetailResponseDto>
}
