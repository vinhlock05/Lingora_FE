package com.example.lingora_fe.user.ranking.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.ranking.domain.model.ClassroomLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.GlobalLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.MyClassroomRankingStats
import com.example.lingora_fe.user.ranking.domain.model.MyRankWithNeighbors
import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod
import com.example.lingora_fe.user.ranking.domain.model.RankingScope
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryPage

interface RankingRepository {

    suspend fun getMyStats(): Either<AppFailure, MyRankingStats>

    suspend fun getMyRank(
        period: RankingPeriod,
        scope: RankingScope,
        classroomId: Int? = null
    ): Either<AppFailure, MyRankWithNeighbors>

    suspend fun getMyXpHistory(
        page: Int = 1,
        limit: Int = 20,
        actionType: String? = null,
        classroomId: Int? = null
    ): Either<AppFailure, XpHistoryPage>

    suspend fun getGlobalLeaderboard(
        period: RankingPeriod,
        page: Int = 1,
        limit: Int = 20
    ): Either<AppFailure, GlobalLeaderboardPage>

    suspend fun getClassroomLeaderboard(
        classroomId: Int,
        period: RankingPeriod,
        page: Int = 1,
        limit: Int = 20
    ): Either<AppFailure, ClassroomLeaderboardPage>

    suspend fun getMyClassroomStats(
        classroomId: Int
    ): Either<AppFailure, MyClassroomRankingStats>
}
