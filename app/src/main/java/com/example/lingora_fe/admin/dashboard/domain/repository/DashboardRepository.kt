package com.example.lingora_fe.admin.dashboard.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.dashboard.domain.model.*
import com.example.lingora_fe.core.error.AppFailure

/**
 * Dashboard Repository Interface
 */
interface DashboardRepository {
    
    /**
     * Get overview metrics for 4 KPI cards
     */
    suspend fun getOverview(startDate: String? = null, endDate: String? = null): Either<AppFailure, DashboardOverview>
    
    /**
     * Get user analytics
     */
    suspend fun getUserAnalytics(startDate: String? = null, endDate: String? = null): Either<AppFailure, UserAnalytics>
    
    /**
     * Get learning analytics
     */
    suspend fun getLearningAnalytics(startDate: String? = null, endDate: String? = null): Either<AppFailure, LearningAnalytics>
    
    /**
     * Get revenue analytics
     */
    suspend fun getRevenueAnalytics(startDate: String? = null, endDate: String? = null): Either<AppFailure, RevenueAnalytics>
    
    /**
     * Get exam analytics
     */
    suspend fun getExamAnalytics(startDate: String? = null, endDate: String? = null): Either<AppFailure, ExamAnalytics>
    
    /**
     * Get recent activities
     */
    suspend fun getRecentActivities(limit: Int = 20): Either<AppFailure, List<Activity>>
}
