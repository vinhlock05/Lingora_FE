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

            val overviewDeferred = async { repository.getOverview() }
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
            repository.getUserAnalytics().fold(
                { error -> _state.update { it.copy(isLoadingUsers = false, error = error.message) } },
                { data -> _state.update { it.copy(userAnalytics = data, isLoadingUsers = false) } }
            )
        }
    }

    fun loadLearningAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLearning = true) }
            repository.getLearningAnalytics().fold(
                { error -> _state.update { it.copy(isLoadingLearning = false, error = error.message) } },
                { data -> _state.update { it.copy(learningAnalytics = data, isLoadingLearning = false) } }
            )
        }
    }

    fun loadRevenueAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingRevenue = true) }
            repository.getRevenueAnalytics().fold(
                { error -> _state.update { it.copy(isLoadingRevenue = false, error = error.message) } },
                { data -> _state.update { it.copy(revenueAnalytics = data, isLoadingRevenue = false) } }
            )
        }
    }

    fun loadExamAnalytics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingExams = true) }
            repository.getExamAnalytics().fold(
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
}
