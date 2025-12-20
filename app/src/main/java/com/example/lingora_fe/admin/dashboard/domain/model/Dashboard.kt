package com.example.lingora_fe.admin.dashboard.domain.model

/**
 * Dashboard Overview - 4 KPI Cards
 */
data class DashboardOverview(
    val users: UsersOverview,
    val studySets: StudySetsOverview,
    val revenue: RevenueOverview,
    val exams: ExamsOverview
)

data class UsersOverview(
    val total: Int,
    val active: Int,
    val new: Int,
    val growth: Int
)

data class StudySetsOverview(
    val total: Int,
    val published: Int,
    val totalPurchases: Int
)

data class RevenueOverview(
    val total: Long,
    val thisMonth: Long,
    val lastMonth: Long,
    val growth: Int
)

data class ExamsOverview(
    val total: Int,
    val published: Int,
    val totalAttempts: Int,
    val completedAttempts: Int
)

/**
 * User Analytics
 */
data class UserAnalytics(
    val growth: List<UserGrowth>,
    val byProficiency: List<UserByProficiency>,
    val byStatus: List<UserByStatus>,
    val activeUsers: ActiveUsers? = null
)

data class ActiveUsers(
    val daily: Int,
    val weekly: Int,
    val monthly: Int
)

data class UserGrowth(
    val date: String,
    val count: Int
)

data class UserByProficiency(
    val proficiency: String,
    val count: Int
)

data class UserByStatus(
    val status: String,
    val count: Int
)

/**
 * Learning Analytics
 */
data class LearningAnalytics(
    val categories: CategoriesAnalytics,
    val topics: TopicsAnalytics,
    val words: WordsAnalytics,
    val learningTrend: List<LearningTrend>? = null
)

data class LearningTrend(
    val date: String,
    val wordsLearned: Int,
    val topicsCompleted: Int
)

data class CategoriesAnalytics(
    val total: Int,
    val completedByUsers: Int,
    val avgProgress: Float,
    val popular: List<PopularCategory>
)

data class PopularCategory(
    val id: Int,
    val name: String,
    val usersCount: Int,
    val avgProgress: Float
)

data class TopicsAnalytics(
    val total: Int,
    val completedByUsers: Int,
    val popular: List<PopularTopic>
)

data class PopularTopic(
    val id: Int,
    val name: String,
    val usersCount: Int
)

data class WordsAnalytics(
    val total: Int,
    val learnedByUsers: Int,
    val avgPerUser: Int
)

/**
 * Revenue Analytics
 */
data class RevenueAnalytics(
    val trend: List<RevenueTrend>,
    val transactions: Transactions,
    val topSelling: List<TopSelling>
)

data class RevenueTrend(
    val month: String,
    val revenue: Long,
    val transactions: Int
)

data class Transactions(
    val total: Int,
    val success: Int,
    val pending: Int,
    val failed: Int,
    val successRate: Int
)

data class TopSelling(
    val id: Int,
    val title: String,
    val price: Long,
    val ownerUsername: String,
    val sales: Int,
    val revenue: Long
)

/**
 * Exam Analytics
 */
data class ExamAnalytics(
    val overview: ExamOverviewAnalytics,
    val trend: List<ExamTrend>,
    val examPerformance: List<ExamPerformance>,
    val scoreDistribution: List<ScoreDistribution>? = null,
    val averageScore: Double? = null,
    val averageTimeMinutes: Int? = null
)

data class ScoreDistribution(
    val range: String,
    val count: Int
)

data class ExamOverviewAnalytics(
    val totalExams: Int,
    val publishedExams: Int,
    val totalAttempts: Int,
    val completedAttempts: Int,
    val completionRate: Int
)

data class ExamTrend(
    val date: String,
    val attempts: Int
)

data class ExamPerformance(
    val examId: Int,
    val title: String,
    val code: String,
    val attempts: Int,
    val completed: Int,
    val completionRate: Int
)

/**
 * Recent Activities
 */
data class Activity(
    val type: ActivityType,
    val timestamp: String,
    val user: ActivityUser,
    val action: String,
    val details: ActivityDetails?
)

enum class ActivityType(val value: String) {
    USER_REGISTER("USER_REGISTER"),
    PURCHASE("PURCHASE"),
    EXAM_COMPLETED("EXAM_COMPLETED"),
    UNKNOWN("UNKNOWN");
    
    companion object {
        fun fromValue(value: String): ActivityType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

data class ActivityUser(
    val id: Int,
    val username: String,
    val avatar: String?
)

data class ActivityDetails(
    val amount: Long?
)
