package com.example.lingora_fe.admin.dashboard.data.remote.api

import com.example.lingora_fe.admin.dashboard.data.remote.dto.*
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Dashboard API Service for Admin Analytics
 */
interface DashboardApiService {
    
    /**
     * Get Overview Metrics - 4 KPI cards
     */
    @GET("admin/dashboard/overview")
    suspend fun getOverview(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<OverviewDto>
    
    /**
     * Get User Analytics
     */
    @GET("admin/dashboard/users")
    suspend fun getUserAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<UserAnalyticsDto>
    
    /**
     * Get Learning Analytics
     */
    @GET("admin/dashboard/learning")
    suspend fun getLearningAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<LearningAnalyticsDto>
    
    /**
     * Get Revenue Analytics
     */
    @GET("admin/dashboard/revenue")
    suspend fun getRevenueAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<RevenueAnalyticsDto>
    
    /**
     * Get Exam Analytics
     */
    @GET("admin/dashboard/exams")
    suspend fun getExamAnalytics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<ExamAnalyticsDto>
    
    /**
     * Get Recent Activities
     */
    @GET("admin/dashboard/activities")
    suspend fun getRecentActivities(
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<ActivityDto>>
}
