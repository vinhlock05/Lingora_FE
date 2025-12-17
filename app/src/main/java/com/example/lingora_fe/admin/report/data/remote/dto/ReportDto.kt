package com.example.lingora_fe.admin.report.data.remote.dto

import com.google.gson.annotations.SerializedName

// User DTO in reports
data class ReportUserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName(value = "username")
    val username: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("email")
    val email: String? = null
)

// Target info DTO
data class TargetInfoDto(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("contentPreview")
    val contentPreview: String? = null,
    @SerializedName("description")
    val description: String? = null
)

// Basic report DTO
data class ReportDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("targetType")
    val targetType: String,
    @SerializedName("targetId")
    val targetId: Int,
    @SerializedName("reportType")
    val reportType: String,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: ReportUserDto,
    @SerializedName("targetInfo")
    val targetInfo: TargetInfoDto? = null
)

// Report history item DTO (simplified version for history list)
data class ReportHistoryItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("reportType")
    val reportType: String,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: ReportUserDto
)

// Detailed report DTO
data class ReportDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("targetType")
    val targetType: String,
    @SerializedName("targetId")
    val targetId: Int,
    @SerializedName("reportType")
    val reportType: String,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("createdBy")
    val createdBy: ReportUserDto,
    @SerializedName("reportHistory")
    val reportHistory: List<ReportHistoryItemDto>? = null,  // Backend may return null
    @SerializedName("totalReports")
    val totalReports: Int
)

// Report list metadata DTO
data class ReportListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("reports")
    val reports: List<ReportDto>
)

// Request DTOs
data class CreateReportRequest(
    @SerializedName("targetType")
    val targetType: String,
    @SerializedName("targetId")
    val targetId: Int,
    @SerializedName("reportType")
    val reportType: String,
    @SerializedName("reason")
    val reason: String? = null
)

data class UpdateReportStatusRequest(
    @SerializedName("status")
    val status: String
)

data class ReportActionDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("duration")
    val duration: Int? = null
)

data class HandleReportRequest(
    @SerializedName("status")
    val status: String,
    @SerializedName("actions")
    val actions: List<ReportActionDto>? = null
)

// Response for handle report with auto-resolved count
data class HandleReportResponse(
    @SerializedName("report")
    val report: ReportDetailDto?,
    @SerializedName("actions")
    val actions: List<Any>?,
    @SerializedName("autoResolvedCount")
    val autoResolvedCount: Int
)
