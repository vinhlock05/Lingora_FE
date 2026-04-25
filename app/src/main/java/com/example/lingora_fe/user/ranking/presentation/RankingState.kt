package com.example.lingora_fe.user.ranking.presentation

import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardClassroomInfo
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardEntry
import com.example.lingora_fe.user.ranking.domain.model.MyClassroomRankingStats
import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryEntry

/**
 * Single source of truth for the Ranking screen. Each tab has its own loading
 * / paging / error fields so loading one tab never wipes out data already
 * fetched for another tab. The `myStats` header and the `period` chips are
 * shared across the Global and Classroom tabs (the History tab ignores both).
 */
data class RankingState(

    // ─── Shared header ────────────────────────────────────────────────────
    val isLoadingMyStats: Boolean = false,
    val myStats: MyRankingStats? = null,
    val myStatsError: String? = null,

    // Period chips shared across Global + Classroom.
    val period: RankingPeriod = RankingPeriod.WEEKLY,

    // ─── Global leaderboard tab ───────────────────────────────────────────
    val isLoadingGlobal: Boolean = false,
    val globalEntries: List<LeaderboardEntry> = emptyList(),
    val globalPage: Int = 1,
    val globalTotalPages: Int = 1,
    val globalTotal: Int = 0,
    val globalError: String? = null,

    // ─── Classroom leaderboard tab ────────────────────────────────────────
    val isLoadingJoinedClassrooms: Boolean = false,
    val joinedClassrooms: List<Classroom> = emptyList(),
    val selectedClassroomId: Int? = null,
    val joinedClassroomsError: String? = null,

    val isLoadingClassroomBoard: Boolean = false,
    val classroomBoard: List<LeaderboardEntry> = emptyList(),
    val classroomPage: Int = 1,
    val classroomTotalPages: Int = 1,
    val classroomTotal: Int = 0,
    val classroomInfo: LeaderboardClassroomInfo? = null,
    val myClassroomStats: MyClassroomRankingStats? = null,
    val classroomBoardError: String? = null,

    // ─── XP history tab ───────────────────────────────────────────────────
    val isLoadingHistory: Boolean = false,
    val historyEntries: List<XpHistoryEntry> = emptyList(),
    val historyPage: Int = 1,
    val historyTotalPages: Int = 1,
    val historyTotal: Int = 0,
    val historyError: String? = null
)
