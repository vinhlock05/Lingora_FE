package com.example.lingora_fe.admin.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.admin.dashboard.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    /**
     * Called when user switches to a tab - always load data for that tab
     */
    fun onTabSelected(tabIndex: Int) {
        when (tabIndex) {
            0 -> loadOverviewData()
            1 -> loadUserAnalytics()
            2 -> loadLearningAnalytics()
            3 -> loadRevenueAnalytics()
            4 -> loadExamAnalytics()
        }
    }

    private fun loadOverviewData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val startDateStr = formatDateParam(_state.value.startDate)
            val endDateStr = formatDateParam(_state.value.endDate)

            val overviewDeferred = async { repository.getOverview(startDateStr, endDateStr) }
            val activitiesDeferred = async { repository.getRecentActivities(10) }

            val overviewResult = overviewDeferred.await()
            val activitiesResult = activitiesDeferred.await()

            _state.update { currentState ->
                var newState = currentState.copy(isLoading = false)
                
                overviewResult.fold(
                    { error -> newState = newState.copy(error = error.message) },
                    { data -> newState = newState.copy(overview = data) }
                )
                
                activitiesResult.fold(
                    { /* Ignore */ },
                    { data -> newState = newState.copy(activities = data) }
                )
                
                newState
            }
        }
    }

    fun loadUserAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingUsers = true) }
            val startDateStr = formatDateParam(_state.value.startDate)
            val endDateStr = formatDateParam(_state.value.endDate)
            repository.getUserAnalytics(startDateStr, endDateStr).fold(
                { error -> _state.update { it.copy(isLoadingUsers = false, error = error.message) } },
                { data -> _state.update { it.copy(userAnalytics = data, isLoadingUsers = false) } }
            )
        }
    }

    fun loadLearningAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLearning = true) }
            val startDateStr = formatDateParam(_state.value.startDate)
            val endDateStr = formatDateParam(_state.value.endDate)
            repository.getLearningAnalytics(startDateStr, endDateStr).fold(
                { error -> _state.update { it.copy(isLoadingLearning = false, error = error.message) } },
                { data -> _state.update { it.copy(learningAnalytics = data, isLoadingLearning = false) } }
            )
        }
    }

    fun loadRevenueAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingRevenue = true) }
            val startDateStr = formatDateParam(_state.value.startDate)
            val endDateStr = formatDateParam(_state.value.endDate)
            repository.getRevenueAnalytics(startDateStr, endDateStr).fold(
                { error -> _state.update { it.copy(isLoadingRevenue = false, error = error.message) } },
                { data -> _state.update { it.copy(revenueAnalytics = data, isLoadingRevenue = false) } }
            )
        }
    }

    fun loadExamAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingExams = true) }
            val startDateStr = formatDateParam(_state.value.startDate)
            val endDateStr = formatDateParam(_state.value.endDate)
            repository.getExamAnalytics(startDateStr, endDateStr).fold(
                { error -> _state.update { it.copy(isLoadingExams = false, error = error.message) } },
                { data -> _state.update { it.copy(examAnalytics = data, isLoadingExams = false) } }
            )
        }
    }

    fun refresh() {
        _state.update { 
            DashboardState(isRefreshing = true)
        }
        loadOverviewData()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun onDateRangeSelected(start: Long?, end: Long?) {
         _state.update { it.copy(startDate = start, endDate = end) }
         // Reload all data with new filter
         loadOverviewData()
         loadUserAnalytics()
         loadLearningAnalytics()
         loadRevenueAnalytics()
         loadExamAnalytics()
    }

    private fun formatDateParam(timestamp: Long?): String? {
        return timestamp?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it))
        }
    }
}
