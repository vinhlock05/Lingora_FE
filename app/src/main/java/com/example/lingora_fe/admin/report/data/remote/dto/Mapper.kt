package com.example.lingora_fe.admin.report.data.remote.dto

import com.example.lingora_fe.admin.report.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

// Date format for parsing API responses
private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

// Extension function to parse date string
private fun String.toDate(): Date {
    return try {
        dateFormat.parse(this) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

// DTO to Domain mappers
fun ReportUserDto.toDomain() = ReportUser(
    id = id,
    username = username,
    avatar = avatar,
    email = email
)

fun TargetInfoDto.toDomain() = TargetInfo(
    title = title,
    contentPreview = contentPreview,
    description = description
)

fun ReportDto.toDomain() = Report(
    id = id,
    targetType = TargetType.valueOf(targetType),
    targetId = targetId,
    reportType = ReportType.valueOf(reportType),
    reason = reason,
    status = ReportStatus.valueOf(status),
    createdAt = createdAt.toDate(),
    createdBy = createdBy.toDomain(),
    targetInfo = targetInfo?.toDomain()
)

// ReportHistoryItemDto to Report (for history list)
fun ReportHistoryItemDto.toDomain() = Report(
    id = id,
    targetType = TargetType.POST, // Default, not used in history display
    targetId = 0, // Default, not used in history display
    reportType = ReportType.valueOf(reportType),
    reason = reason,
    status = ReportStatus.valueOf(status),
    createdAt = createdAt.toDate(),
    createdBy = createdBy.toDomain(),
    targetInfo = null
)

// ReportDetailDto to ReportDetail
fun ReportDetailDto.toDomain() = ReportDetail(
    id = id,
    targetType = TargetType.valueOf(targetType),
    targetId = targetId,
    reportType = ReportType.valueOf(reportType),
    reason = reason,
    status = ReportStatus.valueOf(status),
    createdAt = createdAt.toDate(),
    createdBy = createdBy.toDomain(),
    reportHistory = reportHistory?.map { it.toDomain() }.orEmpty(),
    totalReports = totalReports
)

fun ReportListMetaData.toDomain() = ReportListMetadata(
    currentPage = currentPage,
    totalPages = totalPages,
    total = total,
    reports = reports.map { it.toDomain() }
)

// Domain to DTO mappers
fun CreateReportData.toDto() = CreateReportRequest(
    targetType = targetType.value,
    targetId = targetId,
    reportType = reportType.value,
    reason = reason
)

fun UpdateReportStatusData.toDto() = UpdateReportStatusRequest(
    status = status.value
)

fun ReportAction.toDto() = ReportActionDto(
    type = type.value,
    reason = reason,
    duration = duration
)


fun HandleReportData.toDto() = HandleReportRequest(
    status = status.value,
    actions = actions?.map { it.toDto() }
)

fun HandleReportResponse.toDomain(): HandleReportResult {
    return HandleReportResult(
        report = report?.toDomain(),
        autoResolvedCount = autoResolvedCount
    )
}
