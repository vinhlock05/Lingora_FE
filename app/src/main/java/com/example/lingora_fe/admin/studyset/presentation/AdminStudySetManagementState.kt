package com.example.lingora_fe.admin.studyset.presentation

import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetStatus

data class AdminStudySetManagementState(
    val studySets: List<StudySet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    
    // Filters
    val searchQuery: String = "",
    val filterStatus: StudySetFilter? = null,
    
    // Selected study set for details
    val selectedStudySet: StudySet? = null,
    val isStudySetDetailsLoading: Boolean = false,
    
    // Action states
    val isApproving: Boolean = false,
    val isRejecting: Boolean = false,
    
    val actionSuccess: String? = null,
    val actionError: String? = null
)

enum class StudySetFilter(val displayName: String, val status: StudySetStatus?) {
    ALL("Tất cả", null),
    PENDING_APPROVAL("Chờ duyệt", StudySetStatus.PENDING_APPROVAL),
    PUBLISHED("Đã duyệt", StudySetStatus.PUBLISHED)
}

sealed class AdminStudySetManagementEvent {
    // List events
    data class LoadStudySets(val page: Int = 1) : AdminStudySetManagementEvent()
    data class SearchStudySets(val query: String) : AdminStudySetManagementEvent()
    data class FilterByStatus(val filter: StudySetFilter?) : AdminStudySetManagementEvent()
    object RefreshStudySets : AdminStudySetManagementEvent()
    
    // Study set details
    data class LoadStudySetDetails(val studySetId: Int) : AdminStudySetManagementEvent()
    object ClearSelectedStudySet : AdminStudySetManagementEvent()
    
    // Approval actions
    data class ApproveStudySet(val studySetId: Int) : AdminStudySetManagementEvent()
    data class RejectStudySet(val studySetId: Int, val reason: String? = null) : AdminStudySetManagementEvent()
    
    // UI actions
    object ClearError : AdminStudySetManagementEvent()
    object ClearActionMessages : AdminStudySetManagementEvent()
}

