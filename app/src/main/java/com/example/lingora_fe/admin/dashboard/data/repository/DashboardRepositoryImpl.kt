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
    
    override suspend fun getOverview(): Either<AppFailure, DashboardOverview> {
        return Either.catch {
            val response = apiService.getOverview()
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getUserAnalytics(): Either<AppFailure, UserAnalytics> {
        return Either.catch {
            val response = apiService.getUserAnalytics()
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getLearningAnalytics(): Either<AppFailure, LearningAnalytics> {
        return Either.catch {
            val response = apiService.getLearningAnalytics()
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getRevenueAnalytics(): Either<AppFailure, RevenueAnalytics> {
        return Either.catch {
            val response = apiService.getRevenueAnalytics()
            response.metaData?.toDomain() ?: throw Exception("No data received")
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getExamAnalytics(): Either<AppFailure, ExamAnalytics> {
        return Either.catch {
            val response = apiService.getExamAnalytics()
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
