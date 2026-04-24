package com.example.lingora_fe.user.ranking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod
import com.example.lingora_fe.user.ranking.domain.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

private const val DEFAULT_PAGE_SIZE = 20
private const val HISTORY_PAGE_SIZE = 30

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val rankingRepository: RankingRepository,
    private val classroomRepository: ClassroomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RankingState())
    val state: StateFlow<RankingState> = _state.asStateFlow()

    init {
        loadMyStats()
        loadGlobalLeaderboard(page = 1)

        // When an XP event is broadcast (e.g. user just finished an exam)
        // silently refresh stats + visible leaderboards so the screen stays
        // in sync without user-visible spinners.
        XpEventBus.events
            .onEach { refreshAfterXpAwarded() }
            .launchIn(viewModelScope)
    }

    /**
     * Lifecycle-aware refresh hook used by `RankingScreen` / `RankingSummaryCard`
     * when the hosting composable resumes. Cheap: only re-fetches the header
     * and the currently visible tab.
     */
    fun onScreenResumed() {
        loadMyStats()
        loadGlobalLeaderboard(page = 1)
        _state.value.selectedClassroomId?.let { loadClassroomLeaderboard(it, page = 1) }
    }

    private fun refreshAfterXpAwarded() {
        loadMyStats()
        // Period-scoped XP gets stale even for non-visible tabs, but re-loading
        // just the visible surface keeps things snappy.
        loadGlobalLeaderboard(page = 1)
        _state.value.selectedClassroomId?.let { id ->
            loadClassroomLeaderboard(id, page = 1)
            loadMyClassroomStats(id)
        }
    }

    // ─── Period ──────────────────────────────────────────────────────────

    fun setPeriod(period: RankingPeriod) {
        if (_state.value.period == period) return
        _state.value = _state.value.copy(period = period)
        // Refresh both global + active classroom leaderboard so visible data
        // matches the chip the user just tapped.
        loadGlobalLeaderboard(page = 1)
        _state.value.selectedClassroomId?.let { loadClassroomLeaderboard(it, page = 1) }
    }

    // ─── My stats header ─────────────────────────────────────────────────

    fun loadMyStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMyStats = true, myStatsError = null)
            rankingRepository.getMyStats().fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(
                        isLoadingMyStats = false,
                        myStatsError = failure.message
                    )
                },
                ifRight = { stats ->
                    _state.value = _state.value.copy(
                        isLoadingMyStats = false,
                        myStats = stats,
                        myStatsError = null
                    )
                }
            )
        }
    }

    // ─── Global leaderboard ──────────────────────────────────────────────

    fun loadGlobalLeaderboard(page: Int = 1) {
        val period = _state.value.period
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingGlobal = true, globalError = null)
            rankingRepository.getGlobalLeaderboard(
                period = period,
                page = page,
                limit = DEFAULT_PAGE_SIZE
            ).fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(
                        isLoadingGlobal = false,
                        globalError = failure.message
                    )
                },
                ifRight = { result ->
                    val totalPages = totalPagesOf(result.total, result.limit)
                    val merged = if (page == 1) result.items
                    else (_state.value.globalEntries + result.items).distinctBy { it.userId }
                    _state.value = _state.value.copy(
                        isLoadingGlobal = false,
                        globalError = null,
                        globalEntries = merged,
                        globalPage = result.page,
                        globalTotalPages = totalPages,
                        globalTotal = result.total
                    )
                }
            )
        }
    }

    fun loadMoreGlobal() {
        val s = _state.value
        if (s.isLoadingGlobal) return
        if (s.globalPage < s.globalTotalPages) {
            loadGlobalLeaderboard(page = s.globalPage + 1)
        }
    }

    fun refreshGlobal() {
        loadMyStats()
        loadGlobalLeaderboard(page = 1)
    }

    // ─── Classroom leaderboard ───────────────────────────────────────────

    fun loadJoinedClassrooms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingJoinedClassrooms = true,
                joinedClassroomsError = null
            )
            classroomRepository.getAllClassrooms(
                page = 1,
                limit = 100,
                membership = "ALL"
            ).fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(
                        isLoadingJoinedClassrooms = false,
                        joinedClassroomsError = failure.message
                    )
                },
                ifRight = { result ->
                    val classrooms = result.classrooms
                    val firstId = classrooms.firstOrNull()?.id
                    val newSelected = _state.value.selectedClassroomId
                        ?.takeIf { id -> classrooms.any { it.id == id } }
                        ?: firstId
                    _state.value = _state.value.copy(
                        isLoadingJoinedClassrooms = false,
                        joinedClassrooms = classrooms,
                        selectedClassroomId = newSelected
                    )
                    if (newSelected != null) {
                        loadClassroomLeaderboard(newSelected, page = 1)
                        loadMyClassroomStats(newSelected)
                    } else {
                        _state.value = _state.value.copy(
                            classroomBoard = emptyList(),
                            classroomTotal = 0,
                            classroomTotalPages = 1,
                            classroomPage = 1,
                            myClassroomStats = null,
                            classroomInfo = null
                        )
                    }
                }
            )
        }
    }

    fun selectClassroom(classroomId: Int) {
        if (_state.value.selectedClassroomId == classroomId) return
        _state.value = _state.value.copy(
            selectedClassroomId = classroomId,
            classroomBoard = emptyList(),
            classroomPage = 1,
            classroomTotal = 0,
            classroomTotalPages = 1,
            myClassroomStats = null,
            classroomInfo = null
        )
        loadClassroomLeaderboard(classroomId, page = 1)
        loadMyClassroomStats(classroomId)
    }

    fun loadClassroomLeaderboard(classroomId: Int, page: Int = 1) {
        val period = _state.value.period
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingClassroomBoard = true,
                classroomBoardError = null
            )
            rankingRepository.getClassroomLeaderboard(
                classroomId = classroomId,
                period = period,
                page = page,
                limit = DEFAULT_PAGE_SIZE
            ).fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(
                        isLoadingClassroomBoard = false,
                        classroomBoardError = failure.message
                    )
                },
                ifRight = { result ->
                    val totalPages = totalPagesOf(result.total, result.limit)
                    val merged = if (page == 1) result.items
                    else (_state.value.classroomBoard + result.items).distinctBy { it.userId }
                    _state.value = _state.value.copy(
                        isLoadingClassroomBoard = false,
                        classroomBoardError = null,
                        classroomBoard = merged,
                        classroomPage = result.page,
                        classroomTotalPages = totalPages,
                        classroomTotal = result.total,
                        classroomInfo = result.classroom
                    )
                }
            )
        }
    }

    fun loadMoreClassroom() {
        val s = _state.value
        val classroomId = s.selectedClassroomId ?: return
        if (s.isLoadingClassroomBoard) return
        if (s.classroomPage < s.classroomTotalPages) {
            loadClassroomLeaderboard(classroomId, page = s.classroomPage + 1)
        }
    }

    fun refreshClassroom() {
        val classroomId = _state.value.selectedClassroomId ?: run {
            loadJoinedClassrooms()
            return
        }
        loadMyStats()
        loadClassroomLeaderboard(classroomId, page = 1)
        loadMyClassroomStats(classroomId)
    }

    private fun loadMyClassroomStats(classroomId: Int) {
        viewModelScope.launch {
            rankingRepository.getMyClassroomStats(classroomId).fold(
                ifLeft = { _state.value = _state.value.copy(myClassroomStats = null) },
                ifRight = { stats ->
                    _state.value = _state.value.copy(myClassroomStats = stats)
                }
            )
        }
    }

    // ─── XP history ──────────────────────────────────────────────────────

    fun loadXpHistory(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingHistory = true, historyError = null)
            rankingRepository.getMyXpHistory(
                page = page,
                limit = HISTORY_PAGE_SIZE
            ).fold(
                ifLeft = { failure ->
                    _state.value = _state.value.copy(
                        isLoadingHistory = false,
                        historyError = failure.message
                    )
                },
                ifRight = { result ->
                    val totalPages = totalPagesOf(result.total, result.limit)
                    val merged = if (page == 1) result.items
                    else (_state.value.historyEntries + result.items).distinctBy { it.id }
                    _state.value = _state.value.copy(
                        isLoadingHistory = false,
                        historyError = null,
                        historyEntries = merged,
                        historyPage = result.page,
                        historyTotalPages = totalPages,
                        historyTotal = result.total
                    )
                }
            )
        }
    }

    fun loadMoreHistory() {
        val s = _state.value
        if (s.isLoadingHistory) return
        if (s.historyPage < s.historyTotalPages) {
            loadXpHistory(page = s.historyPage + 1)
        }
    }

    fun refreshHistory() {
        loadXpHistory(page = 1)
    }

    private fun totalPagesOf(total: Int, limit: Int): Int {
        if (limit <= 0) return 1
        return max(1, (total + limit - 1) / limit)
    }
}
