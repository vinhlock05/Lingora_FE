package com.example.lingora_fe.user.ranking.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /rankings/me */
data class MyRankingStatsDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("weeklyXp") val weeklyXp: Int,
    @SerializedName("monthlyXp") val monthlyXp: Int,
    @SerializedName("streak") val streak: Int,
    @SerializedName("activityScore") val activityScore: Float,
    @SerializedName("rankWeekly") val rankWeekly: Int?,
    @SerializedName("rankMonthly") val rankMonthly: Int?,
    @SerializedName("rankAlltime") val rankAlltime: Int?,
    @SerializedName("xpPerLevel") val xpPerLevel: Int,
    @SerializedName("levelProgress") val levelProgress: LevelProgressDto,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class LevelProgressDto(
    @SerializedName("current") val current: Int,
    @SerializedName("needed") val needed: Int,
    @SerializedName("progress") val progress: Float
)

/** GET /rankings/classrooms/:classroomId/me */
data class MyClassroomRankingStatsDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("classroomId") val classroomId: Int,
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("weeklyXp") val weeklyXp: Int,
    @SerializedName("monthlyXp") val monthlyXp: Int,
    @SerializedName("rankWeekly") val rankWeekly: Int?,
    @SerializedName("rankMonthly") val rankMonthly: Int?,
    @SerializedName("rankAlltime") val rankAlltime: Int?,
    @SerializedName("updatedAt") val updatedAt: String?
)

/** Single leaderboard row. Some fields are only present for global vs classroom. */
data class LeaderboardEntryDto(
    @SerializedName("rank") val rank: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("level") val level: Int?,
    @SerializedName("streak") val streak: Int?,
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("weeklyXp") val weeklyXp: Int,
    @SerializedName("monthlyXp") val monthlyXp: Int,
    @SerializedName("xp") val xp: Int
)

/** GET /rankings/global */
data class GlobalLeaderboardPageDto(
    @SerializedName("items") val items: List<LeaderboardEntryDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("period") val period: String
)

data class LeaderboardClassroomInfoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?
)

/** GET /rankings/classrooms/:classroomId */
data class ClassroomLeaderboardPageDto(
    @SerializedName("items") val items: List<LeaderboardEntryDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("period") val period: String,
    @SerializedName("classroom") val classroom: LeaderboardClassroomInfoDto?
)

/** GET /rankings/me/history */
data class XpHistoryEntryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("xpAmount") val xpAmount: Int,
    @SerializedName("actionType") val actionType: String,
    @SerializedName("referencedId") val referencedId: Int?,
    @SerializedName("referenceType") val referenceType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("classroomId") val classroomId: Int?,
    @SerializedName("classroomName") val classroomName: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class XpHistoryPageDto(
    @SerializedName("items") val items: List<XpHistoryEntryDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int
)

/** GET /rankings/me/rank */
data class MyRankWithNeighborsDto(
    @SerializedName("scope") val scope: String,
    @SerializedName("period") val period: String,
    @SerializedName("classroomId") val classroomId: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("xp") val xp: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("neighbors") val neighbors: NeighborsDto
)

data class NeighborsDto(
    @SerializedName("above") val above: RankNeighborDto?,
    @SerializedName("self") val self: RankNeighborDto?,
    @SerializedName("below") val below: RankNeighborDto?
)

data class RankNeighborDto(
    @SerializedName("rank") val rank: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("xp") val xp: Int
)
