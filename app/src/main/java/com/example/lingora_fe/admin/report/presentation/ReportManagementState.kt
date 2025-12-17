package com.example.lingora_fe.admin.report.presentation

import com.example.lingora_fe.admin.report.domain.model.*

// Main UI State
data class ReportManagementState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalReports: Int = 0,
    
    // Filters
    val selectedStatus: ReportStatus? = null,
    val selectedTargetType: TargetType? = null,
    val selectedReportType: ReportType? = null,
    val createdByFilter: Int? = null,
    val searchQuery: String = "",
    val selectedSort: ReportSortOption? = null,
    
    // Selected report for details
    val selectedReport: ReportDetail? = null,
    val isReportDetailsLoading: Boolean = false,
    
    // Target content (fetched separately)
    val targetContent: TargetContent? = null,
    val isTargetContentLoading: Boolean = false,
    val targetContentError: String? = null,
    
    // Action states
    val isUpdatingStatus: Boolean = false,
    val isHandlingReport: Boolean = false,
    val isDeleting: Boolean = false,
    
    val actionSuccess: String? = null,
    val actionError: String? = null,
    
    // Handle report dialog state
    val showHandleDialog: Boolean = false,
    val handleDialogReportId: Int? = null
)

// Handle Report Dialog State
data class HandleReportDialogState(
    val selectedStatus: ReportStatus = ReportStatus.ACCEPTED,
    
    // Content action (toggle)
    val deleteContent: Boolean = true, // Default ON
    
    // User action (radio - pick ONE)
    val selectedUserAction: ReportActionType = ReportActionType.WARN_USER, // Default WARN
    
    // Shared reason field
    val actionReason: String = "",
    
    // Duration for SUSPEND_USER only
    val suspensionDuration: String = "",
    
    // Validation errors
    val durationError: String? = null,
    
    val isValid: Boolean = true // Default values are valid (DELETE_CONTENT=ON, WARN_USER selected)
) {
    fun validate(): HandleReportDialogState {
        val durationErr = if (selectedUserAction == ReportActionType.SUSPEND_USER) {
            when {
                suspensionDuration.isBlank() -> "Duration is required for suspension"
                suspensionDuration.toIntOrNull() == null -> "Duration must be a number"
                suspensionDuration.toInt() !in 1..365 -> "Duration must be between 1-365 days"
                else -> null
            }
        } else null
        
        val valid = when {
            selectedStatus == ReportStatus.REJECTED -> true // No actions needed for rejection
            selectedUserAction == ReportActionType.SUSPEND_USER && durationErr != null -> false
            else -> true
        }
        
        return copy(
            durationError = durationErr,
            isValid = valid
        )
    }
}

// Create Report Dialog State (for users)
data class CreateReportDialogState(
    val selectedReportType: ReportType? = null,
    val reason: String = "",
    
    // Validation errors
    val reportTypeError: String? = null,
    val reasonError: String? = null,
    
    val isValid: Boolean = false
) {
    fun validate(): CreateReportDialogState {
        val reportTypeErr = if (selectedReportType == null) {
            "Please select a report type"
        } else null
        
        val reasonErr = when {
            selectedReportType == ReportType.OTHER && reason.isBlank() -> 
                "Reason is required for 'Other' violations"
            reason.length > 500 -> 
                "Reason cannot exceed 500 characters"
            else -> null
        }
        
        return copy(
            reportTypeError = reportTypeErr,
            reasonError = reasonErr,
            isValid = reportTypeErr == null && reasonErr == null
        )
    }
}
