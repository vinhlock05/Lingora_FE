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
    suspend fun getOverview(): Either<AppFailure, DashboardOverview>
    
    /**
     * Get user analytics
     */
    suspend fun getUserAnalytics(): Either<AppFailure, UserAnalytics>
    
    /**
     * Get learning analytics
     */
    suspend fun getLearningAnalytics(): Either<AppFailure, LearningAnalytics>
    
    /**
     * Get revenue analytics
     */
    suspend fun getRevenueAnalytics(): Either<AppFailure, RevenueAnalytics>
    
    /**
     * Get exam analytics
     */
    suspend fun getExamAnalytics(): Either<AppFailure, ExamAnalytics>
    
    /**
     * Get recent activities
     */
    suspend fun getRecentActivities(limit: Int = 20): Either<AppFailure, List<Activity>>
}
