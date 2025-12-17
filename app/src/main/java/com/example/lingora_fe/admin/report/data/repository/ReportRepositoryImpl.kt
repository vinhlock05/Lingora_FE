package com.example.lingora_fe.admin.report.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.report.data.remote.api.ReportApiService
import com.example.lingora_fe.admin.report.data.remote.dto.toDomain
import com.example.lingora_fe.admin.report.data.remote.dto.toDto
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.admin.report.domain.repository.ReportRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val apiService: ReportApiService,
) : ReportRepository {

    override suspend fun createReport(
        reportData: CreateReportData
    ): Either<AppFailure, Report> {
        return Either.catch {
            val response = apiService.createReport(reportData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAllReports(
        filterOptions: ReportFilterOptions
    ): Either<AppFailure, ReportListMetadata> {
        return Either.catch {
            val response = apiService.getAllReports(
                page = filterOptions.page,
                limit = filterOptions.limit,
                sort = filterOptions.sort,
                status = filterOptions.status?.value,
                targetType = filterOptions.targetType?.value,
                reportType = filterOptions.reportType?.value,
                createdBy = filterOptions.createdBy,
                search = filterOptions.search
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getReportById(
        reportId: Int
    ): Either<AppFailure, ReportDetail> {
        return Either.catch {
            android.util.Log.d("ReportRepository", "Fetching report ID: $reportId")
            val response = apiService.getReportById(reportId)
            android.util.Log.d("ReportRepository", "Response: statusCode=${response.statusCode}, message=${response.message}, metaData=${response.metaData}")
            
            if (response.metaData == null) {
                throw Exception("Report data is null. Status: ${response.statusCode}, Message: ${response.message}")
            }
            
            response.metaData.toDomain()
        }.mapLeft { error ->
            android.util.Log.e("ReportRepository", "Error fetching report: ${error.message}", error)
            error.toAppFailure()
        }
    }

    override suspend fun updateReportStatus(
        reportId: Int,
        statusData: UpdateReportStatusData
    ): Either<AppFailure, Report> {
        return Either.catch {
            val response = apiService.updateReportStatus(reportId, statusData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun handleReport(
        reportId: Int,
        handleData: HandleReportData
    ): Either<AppFailure, HandleReportResult> {
        return Either.catch {
            val response = apiService.handleReport(reportId, handleData.toDto())
            
            // Log response for debugging
            android.util.Log.d("ReportRepo", "HandleReport response: $response")
            android.util.Log.d("ReportRepo", "MetaData: ${response.metaData}")
            android.util.Log.d("ReportRepo", "Message: ${response.message}")
            
            response.metaData?.toDomain() 
                ?: throw Exception("Backend returned null metaData. This may indicate a backend error.")
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteReport(
        reportId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteReport(reportId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}
