package com.example.lingora_fe.admin.report.domain.model

import java.util.Date

// User info in reports
data class ReportUser(
    val id: Int,
    val username: String,
    val avatar: String?,
    val email: String? = null
)

// Basic report information
data class Report(
    val id: Int,
    val targetType: TargetType,
    val targetId: Int,
    val reportType: ReportType,
    val reason: String?,
    val status: ReportStatus,
    val createdAt: Date,
    val createdBy: ReportUser,
    val targetInfo: TargetInfo? = null
)

// Target content preview info
data class TargetInfo(
    val title: String? = null,
    val contentPreview: String? = null,
    val description: String? = null
)

// Detailed report with full target content
data class ReportDetail(
    val id: Int,
    val targetType: TargetType,
    val targetId: Int,
    val reportType: ReportType,
    val reason: String?,
    val status: ReportStatus,
    val createdAt: Date,
    val createdBy: ReportUser,
    val reportHistory: List<Report>,
    val totalReports: Int
)

// Report list metadata
data class ReportListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val reports: List<Report>
)

// Request data for creating reports
data class CreateReportData(
    val targetType: TargetType,
    val targetId: Int,
    val reportType: ReportType,
    val reason: String? = null
)

// Request data for updating report status
data class UpdateReportStatusData(
    val status: ReportStatus
)

// Action details for handling reports
data class ReportAction(
    val type: ReportActionType,
    val reason: String? = null,
    val duration: Int? = null  // For SUSPEND_USER (days)
)

// Request data for handling reports with actions
data class HandleReportData(
    val status: ReportStatus,
    val actions: List<ReportAction>? = null
)

// Result from handling report with auto-resolved count
data class HandleReportResult(
    val report: ReportDetail?,  // Backend may return null or incomplete report
    val autoResolvedCount: Int
)

// Filter and pagination options
data class ReportFilterOptions(
    val page: Int = 1,
    val limit: Int = 10,
    val sort: String? = null,
    val status: ReportStatus? = null,
    val targetType: TargetType? = null,
    val reportType: ReportType? = null,
    val createdBy: Int? = null,
    val search: String? = null
)

// Sort options for reports
enum class ReportSortOption(val displayName: String, val apiValue: String) {
    CREATED_AT_DESC("Newest First", "-createdAt"),
    CREATED_AT_ASC("Oldest First", "+createdAt")
}
