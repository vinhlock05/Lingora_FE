package com.example.lingora_fe.user.ranking.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.ranking.data.remote.dto.ClassroomLeaderboardPageDto
import com.example.lingora_fe.user.ranking.data.remote.dto.GlobalLeaderboardPageDto
import com.example.lingora_fe.user.ranking.data.remote.dto.MyClassroomRankingStatsDto
import com.example.lingora_fe.user.ranking.data.remote.dto.MyRankWithNeighborsDto
import com.example.lingora_fe.user.ranking.data.remote.dto.MyRankingStatsDto
import com.example.lingora_fe.user.ranking.data.remote.dto.XpHistoryPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RankingApiService {

    @GET("rankings/me")
    suspend fun getMyStats(): ApiResponse<MyRankingStatsDto>

    @GET("rankings/me/rank")
    suspend fun getMyRank(
        @Query("period") period: String,
        @Query("scope") scope: String,
        @Query("classroomId") classroomId: Int? = null
    ): ApiResponse<MyRankWithNeighborsDto>

    @GET("rankings/me/history")
    suspend fun getMyXpHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("actionType") actionType: String? = null,
        @Query("classroomId") classroomId: Int? = null
    ): ApiResponse<XpHistoryPageDto>

    @GET("rankings/global")
    suspend fun getGlobalLeaderboard(
        @Query("period") period: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<GlobalLeaderboardPageDto>

    @GET("rankings/classrooms/{classroomId}")
    suspend fun getClassroomLeaderboard(
        @Path("classroomId") classroomId: Int,
        @Query("period") period: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<ClassroomLeaderboardPageDto>

    @GET("rankings/classrooms/{classroomId}/me")
    suspend fun getMyClassroomRank(
        @Path("classroomId") classroomId: Int
    ): ApiResponse<MyClassroomRankingStatsDto>
}
