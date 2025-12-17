package com.example.lingora_fe.admin.report.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.report.domain.model.*
import com.example.lingora_fe.core.error.AppFailure

interface ReportRepository {

    /**
     * Create a new report
     */
    suspend fun createReport(
        reportData: CreateReportData
    ): Either<AppFailure, Report>

    /**
     * Get all reports with filtering and pagination
     */
    suspend fun getAllReports(
        filterOptions: ReportFilterOptions
    ): Either<AppFailure, ReportListMetadata>

    /**
     * Get report details by ID
     */
    suspend fun getReportById(
        reportId: Int
    ): Either<AppFailure, ReportDetail>

    /**
     * Update report status
     */
    suspend fun updateReportStatus(
        reportId: Int,
        statusData: UpdateReportStatusData
    ): Either<AppFailure, Report>

    /**
     * Handle report with action
     */
    suspend fun handleReport(
        reportId: Int,
        handleData: HandleReportData
    ): Either<AppFailure, HandleReportResult>

    /**
     * Delete a report
     */
    suspend fun deleteReport(
        reportId: Int
    ): Either<AppFailure, Unit>
}
