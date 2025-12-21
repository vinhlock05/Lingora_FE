package com.example.lingora_fe.admin.dashboard.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.dashboard.data.remote.api.DashboardApiService
import com.example.lingora_fe.admin.dashboard.data.remote.dto.*
import com.example.lingora_fe.admin.dashboard.domain.model.*
import com.example.lingora_fe.admin.dashboard.domain.repository.DashboardRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

/**
 * Dashboard Repository Implementation
 */
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: DashboardApiService
) : DashboardRepository {
    
    override suspend fun getOverview(startDate: String?, endDate: String?): Either<AppFailure, DashboardOverview> {
        return Either.catch {
            val response = apiService.getOverview(startDate, endDate)
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getUserAnalytics(startDate: String?, endDate: String?): Either<AppFailure, UserAnalytics> {
        return Either.catch {
            val response = apiService.getUserAnalytics(startDate, endDate)
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getLearningAnalytics(startDate: String?, endDate: String?): Either<AppFailure, LearningAnalytics> {
        return Either.catch {
            val response = apiService.getLearningAnalytics(startDate, endDate)
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getRevenueAnalytics(startDate: String?, endDate: String?): Either<AppFailure, RevenueAnalytics> {
        return Either.catch {
            val response = apiService.getRevenueAnalytics(startDate, endDate)
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getExamAnalytics(startDate: String?, endDate: String?): Either<AppFailure, ExamAnalytics> {
        return Either.catch {
            val response = apiService.getExamAnalytics(startDate, endDate)
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getRecentActivities(limit: Int): Either<AppFailure, List<Activity>> {
        return Either.catch {
            val response = apiService.getRecentActivities(limit)
            response.metaData?.map { it.toDomain() } ?: emptyList()
        }.mapLeft { it.toAppFailure() }
    }
}
