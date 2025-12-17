package com.example.lingora_fe.admin.report.data.remote.api

import com.example.lingora_fe.admin.report.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface ReportApiService {

    @POST("reports")
    suspend fun createReport(
        @Body request: CreateReportRequest
    ): ApiResponse<ReportDto>

    @GET("reports")
    suspend fun getAllReports(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sort") sort: String? = null,
        @Query("status") status: String? = null,
        @Query("targetType") targetType: String? = null,
        @Query("reportType") reportType: String? = null,
        @Query("createdBy") createdBy: Int? = null,
        @Query("search") search: String? = null
    ): ApiResponse<ReportListMetaData>

    @GET("reports/{id}")
    suspend fun getReportById(
        @Path("id") id: Int
    ): ApiResponse<ReportDetailDto>

    @PATCH("reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: Int,
        @Body request: UpdateReportStatusRequest
    ): ApiResponse<ReportDto>

    @PATCH("reports/{id}/handle")
    suspend fun handleReport(
        @Path("id") id: Int,
        @Body request: HandleReportRequest
    ): ApiResponse<HandleReportResponse>

    @DELETE("reports/{id}")
    suspend fun deleteReport(
        @Path("id") id: Int
    ): ApiResponse<Any>
}
