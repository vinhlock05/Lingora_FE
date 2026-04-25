package com.example.lingora_fe.user.ranking.domain.model

/**
 * Mirrors backend `RankingPeriod` enum.
 * Wire format must match the lower-cased values the backend accepts as query
 * params — keep in sync with `Lingora-BE/src/enums/rankingPeriod.enum.ts`.
 */
enum class RankingPeriod(val value: String, val labelVi: String) {
    WEEKLY("weekly", "Tuần này"),
    MONTHLY("monthly", "Tháng này"),
    ALLTIME("alltime", "Toàn thời gian")
}

/** Mirrors backend `RankingScope` enum. */
enum class RankingScope(val value: String) {
    GLOBAL("global"),
    CLASSROOM("classroom")
}

/** Mirrors backend `XpActionType` — used in XP history filtering / display. */
enum class XpActionType(val value: String, val labelVi: String) {
    FLASHCARD_LEARNED("flashcard_learned", "Học flashcard"),
    WORD_MASTERED("word_mastered", "Thành thạo từ"),
    QUIZ_COMPLETED("quiz_completed", "Hoàn thành trắc nghiệm"),
    EXAM_COMPLETED("exam_completed", "Hoàn thành bài kiểm tra"),
    LESSON_COMPLETED("lesson_completed", "Hoàn thành bài giảng"),
    CLASSROOM_QUIZ("classroom_quiz", "Quiz lớp học"),
    CLASSROOM_CHAT("classroom_chat", "Tin nhắn lớp học"),
    CONVERSATION_ENDED("conversation_ended", "Kết thúc hội thoại AI"),
    DAILY_LOGIN("daily_login", "Điểm danh hàng ngày"),
    STREAK_BONUS("streak_bonus", "Thưởng streak"),
    POST_CREATED("post_created", "Đăng bài"),
    ADMIN_ADJUSTMENT("admin_adjustment", "Admin điều chỉnh"),
    UNKNOWN("unknown", "Khác");

    companion object {
        fun fromValue(value: String?): XpActionType =
            values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}

/** Per-level progress towards the next level. */
data class LevelProgress(
    val current: Int,
    val needed: Int,
    val progress: Float
)

/** Caller's global ranking snapshot — backs the "My rank" card. */
data class MyRankingStats(
    val userId: Int,
    val level: Int,
    val totalXp: Int,
    val weeklyXp: Int,
    val monthlyXp: Int,
    val streak: Int,
    val activityScore: Float,
    val rankWeekly: Int?,
    val rankMonthly: Int?,
    val rankAlltime: Int?,
    val xpPerLevel: Int,
    val levelProgress: LevelProgress,
    val updatedAt: String?
) {
    fun rankFor(period: RankingPeriod): Int? = when (period) {
        RankingPeriod.WEEKLY -> rankWeekly
        RankingPeriod.MONTHLY -> rankMonthly
        RankingPeriod.ALLTIME -> rankAlltime
    }

    fun xpFor(period: RankingPeriod): Int = when (period) {
        RankingPeriod.WEEKLY -> weeklyXp
        RankingPeriod.MONTHLY -> monthlyXp
        RankingPeriod.ALLTIME -> totalXp
    }
}

/** Caller's classroom-scoped snapshot. */
data class MyClassroomRankingStats(
    val userId: Int,
    val classroomId: Int,
    val totalXp: Int,
    val weeklyXp: Int,
    val monthlyXp: Int,
    val rankWeekly: Int?,
    val rankMonthly: Int?,
    val rankAlltime: Int?,
    val updatedAt: String?
) {
    fun rankFor(period: RankingPeriod): Int? = when (period) {
        RankingPeriod.WEEKLY -> rankWeekly
        RankingPeriod.MONTHLY -> rankMonthly
        RankingPeriod.ALLTIME -> rankAlltime
    }

    fun xpFor(period: RankingPeriod): Int = when (period) {
        RankingPeriod.WEEKLY -> weeklyXp
        RankingPeriod.MONTHLY -> monthlyXp
        RankingPeriod.ALLTIME -> totalXp
    }
}

/**
 * One row of the global leaderboard. `level` and `streak` are only present
 * for global entries; `xp` is the period-relevant XP that drove the ordering.
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val username: String,
    val avatar: String?,
    val level: Int?,
    val streak: Int?,
    val totalXp: Int,
    val weeklyXp: Int,
    val monthlyXp: Int,
    val xp: Int
)

data class GlobalLeaderboardPage(
    val items: List<LeaderboardEntry>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val period: RankingPeriod
)

/** Light classroom descriptor returned alongside a classroom leaderboard. */
data class LeaderboardClassroomInfo(
    val id: Int,
    val name: String,
    val code: String?
)

data class ClassroomLeaderboardPage(
    val items: List<LeaderboardEntry>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val period: RankingPeriod,
    val classroom: LeaderboardClassroomInfo?
)

/** Single XP transaction entry for the audit log. */
data class XpHistoryEntry(
    val id: Int,
    val xpAmount: Int,
    val actionType: XpActionType,
    val rawActionType: String,
    val referencedId: Int?,
    val referenceType: String?,
    val description: String?,
    val classroomId: Int?,
    val classroomName: String?,
    val createdAt: String
)

data class XpHistoryPage(
    val items: List<XpHistoryEntry>,
    val page: Int,
    val limit: Int,
    val total: Int
)

/** Trimmed entry returned for "above / self / below" neighbors of `getMyRank`. */
data class RankNeighbor(
    val rank: Int,
    val userId: Int,
    val username: String,
    val avatar: String?,
    val xp: Int
)

data class MyRankWithNeighbors(
    val scope: RankingScope,
    val period: RankingPeriod,
    val classroomId: Int?,
    val rank: Int?,
    val xp: Int,
    val total: Int,
    val above: RankNeighbor?,
    val self: RankNeighbor?,
    val below: RankNeighbor?
)
