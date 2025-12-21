package com.example.lingora_fe.admin.dashboard.presentation

import com.example.lingora_fe.admin.dashboard.domain.model.*

/**
 * Dashboard UI State
 */
data class DashboardState(
    // Loading states per tab
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isLoadingLearning: Boolean = false,
    val isLoadingRevenue: Boolean = false,
    val isLoadingExams: Boolean = false,
    
    // Date Filter
    val startDate: Long? = null,
    val endDate: Long? = null,
    
    // Overview KPIs
    val overview: DashboardOverview? = null,
    
    // User Analytics
    val userAnalytics: UserAnalytics? = null,
    
    // Learning Analytics
    val learningAnalytics: LearningAnalytics? = null,
    
    // Revenue Analytics
    val revenueAnalytics: RevenueAnalytics? = null,
    
    // Exam Analytics
    val examAnalytics: ExamAnalytics? = null,
    
    // Recent Activities
    val activities: List<Activity> = emptyList(),
    
    // Error
    val error: String? = null
)
