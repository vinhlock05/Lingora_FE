package com.example.lingora_fe.admin.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.report.domain.model.CreateReportData
import com.example.lingora_fe.admin.report.domain.model.HandleReportData
import com.example.lingora_fe.admin.report.domain.model.ReportFilterOptions
import com.example.lingora_fe.admin.report.domain.model.ReportSortOption
import com.example.lingora_fe.admin.report.domain.model.ReportStatus
import com.example.lingora_fe.admin.report.domain.model.ReportType
import com.example.lingora_fe.admin.report.domain.model.TargetContent
import com.example.lingora_fe.admin.report.domain.model.TargetType
import com.example.lingora_fe.admin.report.domain.model.UpdateReportStatusData
import com.example.lingora_fe.admin.report.domain.repository.ReportRepository
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportManagementViewModel @Inject constructor(
    private val repository: ReportRepository,
    private val forumRepository: ForumRepository,
    private val studySetRepository: StudySetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportManagementState())
    val state: StateFlow<ReportManagementState> = _state.asStateFlow()

    private val _handleDialogState = MutableStateFlow(HandleReportDialogState())
    val handleDialogState: StateFlow<HandleReportDialogState> = _handleDialogState.asStateFlow()

    private val _createReportDialogState = MutableStateFlow(CreateReportDialogState())
    val createReportDialogState: StateFlow<CreateReportDialogState> = _createReportDialogState.asStateFlow()

    init {
        loadReports()
    }

    // Load reports with filters
    fun loadReports(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val filterOptions = ReportFilterOptions(
                page = page,
                limit = 10,
                sort = _state.value.selectedSort?.apiValue,
                status = _state.value.selectedStatus,
                targetType = _state.value.selectedTargetType,
                reportType = _state.value.selectedReportType,
                createdBy = _state.value.createdByFilter,
                search = _state.value.searchQuery.takeIf { it.isNotBlank() }
            )

            repository.getAllReports(filterOptions)
                .onRight { metadata ->
                    _state.value = _state.value.copy(
                        reports = metadata.reports,
                        currentPage = metadata.currentPage,
                        totalPages = metadata.totalPages,
                        totalReports = metadata.total,
                        isLoading = false,
                        error = null
                    )
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = failure.message
                    )
                }
        }
    }


    // Filter functions
    fun filterByStatus(status: ReportStatus?) {
        _state.value = _state.value.copy(
            selectedStatus = status,
            currentPage = 1
        )
    }

    fun filterByTargetType(targetType: TargetType?) {
        _state.value = _state.value.copy(
            selectedTargetType = targetType,
            currentPage = 1
        )
    }

    fun filterByReportType(reportType: ReportType?) {
        _state.value = _state.value.copy(
            selectedReportType = reportType,
            currentPage = 1
        )
    }

    fun sortBy(sortOption: ReportSortOption?) {
        _state.value = _state.value.copy(
            selectedSort = sortOption,
            currentPage = 1
        )
    }

    fun clearFilters() {
        _state.value = _state.value.copy(
            selectedStatus = null,
            selectedTargetType = null,
            selectedReportType = null,
            createdByFilter = null,
            selectedSort = null,
            searchQuery = "",
            currentPage = 1
        )
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun performSearch() {
        loadReports(page = 1)
    }

    // Update report status only
    fun updateReportStatus(reportId: Int, status: ReportStatus) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdatingStatus = true, actionError = null)

            val statusData = UpdateReportStatusData(status = status)

            repository.updateReportStatus(reportId, statusData)
                .onRight {
                    _state.value = _state.value.copy(
                        isUpdatingStatus = false,
                        actionSuccess = "Report status updated successfully"
                    )
                    loadReports(_state.value.currentPage)
                    if (_state.value.selectedReport?.id == reportId) {
                        loadReportDetail(reportId)
                    }
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isUpdatingStatus = false,
                        actionError = failure.message
                    )
                }
        }
    }

    // Handle report with action
    fun handleReport(reportId: Int, handleData: HandleReportData) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isHandlingReport = true, actionError = null)
            

            repository.handleReport(reportId, handleData)
                .onRight { result ->
                    val successMessage = if (result.autoResolvedCount > 0) {
                        "Report handled successfully. ${result.autoResolvedCount} related report(s) auto-resolved."
                    } else {
                        "Report handled successfully"
                    }
                    
                    _state.value = _state.value.copy(
                        isHandlingReport = false,
                        actionSuccess = successMessage,
                        showHandleDialog = false
                    )
                    resetHandleDialogState()
                    loadReports(_state.value.currentPage)
                    // Reload detail view to show updated status
                    loadReportDetail(reportId)
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isHandlingReport = false,
                        actionError = failure.message
                    )
                }
        }
    }

    // Delete report
    fun deleteReport(reportId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, actionError = null)

            repository.deleteReport(reportId)
                .onRight {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionSuccess = "Report deleted successfully"
                    )
                    loadReports(_state.value.currentPage)
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        actionError = failure.message
                    )
                }
        }
    }

    // Create report (for users)
    fun createReport(reportData: CreateReportData) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, actionError = null)

            repository.createReport(reportData)
                .onRight {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        actionSuccess = "Report submitted successfully"
                    )
                    resetCreateReportDialogState()
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        actionError = failure.message
                    )
                }
        }
    }

    // Dialog management
    fun showHandleDialog(reportId: Int) {
        _state.value = _state.value.copy(
            showHandleDialog = true,
            handleDialogReportId = reportId
        )
    }

    fun hideHandleDialog() {
        _state.value = _state.value.copy(
            showHandleDialog = false,
            handleDialogReportId = null
        )
        resetHandleDialogState()
    }

    // Handle dialog state management
    fun updateHandleDialogState(dialogState: HandleReportDialogState) {
        _handleDialogState.value = dialogState.validate()
    }

    fun resetHandleDialogState() {
        _handleDialogState.value = HandleReportDialogState()
    }

    // Create report dialog state management
    fun updateCreateReportDialogState(dialogState: CreateReportDialogState) {
        _createReportDialogState.value = dialogState.validate()
    }

    fun resetCreateReportDialogState() {
        _createReportDialogState.value = CreateReportDialogState()
    }

    // Clear messages
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearActionMessages() {
        _state.value = _state.value.copy(
            actionSuccess = null,
            actionError = null
        )
    }
    
    // Load report detail by ID
    fun loadReportDetail(reportId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isReportDetailsLoading = true)

            repository.getReportById(reportId)
                .onRight { reportDetail ->
                    _state.value = _state.value.copy(
                        selectedReport = reportDetail,
                        isReportDetailsLoading = false
                    )
                    // Fetch target content after loading report
                    fetchTargetContent(reportDetail.targetType, reportDetail.targetId)
                }
                .onLeft { failure ->
                    _state.value = _state.value.copy(
                        isReportDetailsLoading = false,
                        error = failure.message
                    )
                }
        }
    }
    
    // Fetch target content based on type
    private fun fetchTargetContent(targetType: TargetType, targetId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isTargetContentLoading = true,
                targetContentError = null
            )

            when (targetType) {
                TargetType.POST -> {
                    forumRepository.getPostById(targetId)
                        .onRight { post ->
                            _state.value = _state.value.copy(
                                targetContent = TargetContent.PostContent(post),
                                isTargetContentLoading = false
                            )
                        }
                        .onLeft { failure ->
                            _state.value = _state.value.copy(
                                isTargetContentLoading = false,
                                targetContentError = failure.message
                            )
                        }
                }
                TargetType.STUDY_SET -> {
                    studySetRepository.getStudySetById(targetId)
                        .onRight { studySet ->
                            _state.value = _state.value.copy(
                                targetContent = TargetContent.StudySetContent(studySet),
                                isTargetContentLoading = false
                            )
                        }
                        .onLeft { failure ->
                            _state.value = _state.value.copy(
                                isTargetContentLoading = false,
                                targetContentError = failure.message
                            )
                        }
                }
                TargetType.COMMENT -> {
                    forumRepository.getCommentById(targetId)
                        .onRight { comment ->
                            _state.value = _state.value.copy(
                                targetContent = TargetContent.CommentContent(comment),
                                isTargetContentLoading = false
                            )
                        }
                        .onLeft { failure ->
                            _state.value = _state.value.copy(
                                isTargetContentLoading = false,
                                targetContentError = failure.message
                            )
                        }
                }
            }
        }
    }

    fun refreshReports() {
        loadReports(_state.value.currentPage)
    }
}
