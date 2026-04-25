package com.example.lingora_fe.user.ranking.data.remote.dto

import com.example.lingora_fe.user.ranking.domain.model.ClassroomLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.GlobalLeaderboardPage
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardClassroomInfo
import com.example.lingora_fe.user.ranking.domain.model.LeaderboardEntry
import com.example.lingora_fe.user.ranking.domain.model.LevelProgress
import com.example.lingora_fe.user.ranking.domain.model.MyClassroomRankingStats
import com.example.lingora_fe.user.ranking.domain.model.MyRankWithNeighbors
import com.example.lingora_fe.user.ranking.domain.model.MyRankingStats
import com.example.lingora_fe.user.ranking.domain.model.RankNeighbor
import com.example.lingora_fe.user.ranking.domain.model.RankingPeriod
import com.example.lingora_fe.user.ranking.domain.model.RankingScope
import com.example.lingora_fe.user.ranking.domain.model.XpActionType
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryEntry
import com.example.lingora_fe.user.ranking.domain.model.XpHistoryPage

private fun parsePeriod(raw: String?, fallback: RankingPeriod = RankingPeriod.WEEKLY): RankingPeriod =
    RankingPeriod.values().firstOrNull { it.value.equals(raw, ignoreCase = true) } ?: fallback

private fun parseScope(raw: String?, fallback: RankingScope = RankingScope.GLOBAL): RankingScope =
    RankingScope.values().firstOrNull { it.value.equals(raw, ignoreCase = true) } ?: fallback

fun LevelProgressDto.toDomain(): LevelProgress = LevelProgress(
    current = current,
    needed = needed,
    progress = progress.coerceIn(0f, 1f)
)

fun MyRankingStatsDto.toDomain(): MyRankingStats = MyRankingStats(
    userId = userId,
    level = level,
    totalXp = totalXp,
    weeklyXp = weeklyXp,
    monthlyXp = monthlyXp,
    streak = streak,
    activityScore = activityScore,
    rankWeekly = rankWeekly,
    rankMonthly = rankMonthly,
    rankAlltime = rankAlltime,
    xpPerLevel = xpPerLevel,
    levelProgress = levelProgress.toDomain(),
    updatedAt = updatedAt
)

fun MyClassroomRankingStatsDto.toDomain(): MyClassroomRankingStats = MyClassroomRankingStats(
    userId = userId,
    classroomId = classroomId,
    totalXp = totalXp,
    weeklyXp = weeklyXp,
    monthlyXp = monthlyXp,
    rankWeekly = rankWeekly,
    rankMonthly = rankMonthly,
    rankAlltime = rankAlltime,
    updatedAt = updatedAt
)

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
    rank = rank,
    userId = userId,
    username = username,
    avatar = avatar,
    level = level,
    streak = streak,
    totalXp = totalXp,
    weeklyXp = weeklyXp,
    monthlyXp = monthlyXp,
    xp = xp
)

fun GlobalLeaderboardPageDto.toDomain(): GlobalLeaderboardPage = GlobalLeaderboardPage(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    total = total,
    period = parsePeriod(period)
)

fun LeaderboardClassroomInfoDto.toDomain(): LeaderboardClassroomInfo = LeaderboardClassroomInfo(
    id = id,
    name = name,
    code = code
)

fun ClassroomLeaderboardPageDto.toDomain(): ClassroomLeaderboardPage = ClassroomLeaderboardPage(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    total = total,
    period = parsePeriod(period),
    classroom = classroom?.toDomain()
)

fun XpHistoryEntryDto.toDomain(): XpHistoryEntry = XpHistoryEntry(
    id = id,
    xpAmount = xpAmount,
    actionType = XpActionType.fromValue(actionType),
    rawActionType = actionType,
    referencedId = referencedId,
    referenceType = referenceType,
    description = description,
    classroomId = classroomId,
    classroomName = classroomName,
    createdAt = createdAt
)

fun XpHistoryPageDto.toDomain(): XpHistoryPage = XpHistoryPage(
    items = items.map { it.toDomain() },
    page = page,
    limit = limit,
    total = total
)

fun RankNeighborDto.toDomain(): RankNeighbor = RankNeighbor(
    rank = rank,
    userId = userId,
    username = username,
    avatar = avatar,
    xp = xp
)

fun MyRankWithNeighborsDto.toDomain(): MyRankWithNeighbors = MyRankWithNeighbors(
    scope = parseScope(scope),
    period = parsePeriod(period),
    classroomId = classroomId,
    rank = rank,
    xp = xp,
    total = total,
    above = neighbors.above?.toDomain(),
    self = neighbors.self?.toDomain(),
    below = neighbors.below?.toDomain()
)
