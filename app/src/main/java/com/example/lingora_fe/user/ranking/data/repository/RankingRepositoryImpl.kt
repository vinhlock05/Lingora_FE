package com.example.lingora_fe.user.ranking.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.ranking.data.remote.api.RankingApiService
import com.example.lingora_fe.user.ranking.data.remote.dto.toDomain
import com.example.lingora_fe.user.ranking.domain.model.ClassroomLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.GlobalLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.MyClassroomRankingStats
import com.example.lingora_fe.user.ranking.domain.model.MyRankWithNeighbors
import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod
import com.example.lingora_fe.user.ranking.domain.model.RankingScope
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryPage
import com.example.lingora_fe.user.ranking.domain.repository.RankingRepository
import javax.inject.Inject

class RankingRepositoryImpl @Inject constructor(
    private val apiService: RankingApiService
) : RankingRepository {

    override suspend fun getMyStats(): Either<AppFailure, MyRankingStats> = Either.catch {
        val response = apiService.getMyStats()
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }

    override suspend fun getMyRank(
        period: RankingPeriod,
        scope: RankingScope,
        classroomId: Int?
    ): Either<AppFailure, MyRankWithNeighbors> = Either.catch {
        val response = apiService.getMyRank(
            period = period.value,
            scope = scope.value,
            classroomId = classroomId
        )
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }

    override suspend fun getMyXpHistory(
        page: Int,
        limit: Int,
        actionType: String?,
        classroomId: Int?
    ): Either<AppFailure, XpHistoryPage> = Either.catch {
        val response = apiService.getMyXpHistory(
            page = page,
            limit = limit,
            actionType = actionType,
            classroomId = classroomId
        )
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }

    override suspend fun getGlobalLeaderboard(
        period: RankingPeriod,
        page: Int,
        limit: Int
    ): Either<AppFailure, GlobalLeaderboardPage> = Either.catch {
        val response = apiService.getGlobalLeaderboard(
            period = period.value,
            page = page,
            limit = limit
        )
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }

    override suspend fun getClassroomLeaderboard(
        classroomId: Int,
        period: RankingPeriod,
        page: Int,
        limit: Int
    ): Either<AppFailure, ClassroomLeaderboardPage> = Either.catch {
        val response = apiService.getClassroomLeaderboard(
            classroomId = classroomId,
            period = period.value,
            page = page,
            limit = limit
        )
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }

    override suspend fun getMyClassroomStats(
        classroomId: Int
    ): Either<AppFailure, MyClassroomRankingStats> = Either.catch {
        val response = apiService.getMyClassroomRank(classroomId)
        response.metaData?.toDomain() ?: throw IllegalStateException(response.message)
    }.mapLeft { it.toAppFailure() }
}
