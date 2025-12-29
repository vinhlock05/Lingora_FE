package com.example.lingora_fe.admin.dashboard.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.lingora_fe.admin.dashboard.domain.model.*

// ==================== Overview DTOs ====================

data class OverviewDto(
    @SerializedName("users")
    val users: UsersOverviewDto,
    
    @SerializedName("studySets")
    val studySets: StudySetsOverviewDto,
    
    @SerializedName("revenue")
    val revenue: RevenueOverviewDto,
    
    @SerializedName("exams")
    val exams: ExamsOverviewDto
)

data class UsersOverviewDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("active")
    val active: Int,
    
    @SerializedName("new")
    val new: Int,
    
    @SerializedName("growth")
    val growth: Int
)

data class StudySetsOverviewDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("published")
    val published: Int,
    
    @SerializedName("totalPurchases")
    val totalPurchases: Int
)

data class RevenueOverviewDto(
    @SerializedName("total")
    val total: Long,
    
    @SerializedName("thisPeriod")
    val thisPeriod: Long,
    
    @SerializedName("lastPeriod")
    val lastPeriod: Long,
    
    @SerializedName("growth")
    val growth: Int
)

data class ExamsOverviewDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("published")
    val published: Int,
    
    @SerializedName("totalAttempts")
    val totalAttempts: Int,
    
    @SerializedName("completedAttempts")
    val completedAttempts: Int
)

// ==================== User Analytics DTOs ====================

data class UserAnalyticsDto(
    @SerializedName("growth")
    val growth: List<GrowthDto>,
    
    @SerializedName("byProficiency")
    val byProficiency: List<ByProficiencyDto>,
    
    @SerializedName("byStatus")
    val byStatus: List<ByStatusDto>,
    
    @SerializedName("activeUsers")
    val activeUsers: ActiveUsersDto? = null
)

data class ActiveUsersDto(
    @SerializedName("daily")
    val daily: Int,
    
    @SerializedName("weekly")
    val weekly: Int,
    
    @SerializedName("monthly")
    val monthly: Int
)

data class GrowthDto(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("count")
    val count: Int
)

data class ByProficiencyDto(
    @SerializedName("proficiency")
    val proficiency: String,
    
    @SerializedName("count")
    val count: Int
)

data class ByStatusDto(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("count")
    val count: Int
)

// ==================== Learning Analytics DTOs ====================

data class LearningAnalyticsDto(
    @SerializedName("categories")
    val categories: CategoriesAnalyticsDto,
    
    @SerializedName("topics")
    val topics: TopicsAnalyticsDto,
    
    @SerializedName("words")
    val words: WordsAnalyticsDto,
    
    @SerializedName("learningTrend")
    val learningTrend: List<LearningTrendDto>? = null
)

data class LearningTrendDto(
    @SerializedName("date")
    val date: String?,
    
    @SerializedName("wordsLearned")
    val wordsLearned: Int,
    
    @SerializedName("topicsCompleted")
    val topicsCompleted: Int
)

data class CategoriesAnalyticsDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("completedByUsers")
    val completedByUsers: Int,
    
    @SerializedName("avgProgress")
    val avgProgress: String,
    
    @SerializedName("popular")
    val popular: List<PopularCategoryDto>
)

data class PopularCategoryDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("usersCount")
    val usersCount: Int,
    
    @SerializedName("avgProgress")
    val avgProgress: String
)

data class TopicsAnalyticsDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("completedByUsers")
    val completedByUsers: Int,
    
    @SerializedName("popular")
    val popular: List<PopularTopicDto>
)

data class PopularTopicDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("usersCount")
    val usersCount: Int
)

data class WordsAnalyticsDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("learnedByUsers")
    val learnedByUsers: Int,
    
    @SerializedName("avgPerUser")
    val avgPerUser: Int
)

// ==================== Revenue Analytics DTOs ====================

data class RevenueAnalyticsDto(
    @SerializedName("trend")
    val trend: List<RevenueTrendDto>,
    
    @SerializedName("transactions")
    val transactions: TransactionsDto,
    
    @SerializedName("topSelling")
    val topSelling: List<TopSellingDto>
)

data class RevenueTrendDto(
    @SerializedName("month")
    val month: String,
    
    @SerializedName("revenue")
    val revenue: Long,
    
    @SerializedName("transactions")
    val transactions: Int
)

data class TransactionsDto(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("success")
    val success: Int,
    
    @SerializedName("pending")
    val pending: Int,
    
    @SerializedName("failed")
    val failed: Int,
    
    @SerializedName("successRate")
    val successRate: Int
)

data class TopSellingDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("price")
    val price: Long,
    
    @SerializedName("ownerUsername")
    val ownerUsername: String,
    
    @SerializedName("sales")
    val sales: Int,
    
    @SerializedName("revenue")
    val revenue: Long
)

// ==================== Exam Analytics DTOs ====================

data class ExamAnalyticsDto(
    @SerializedName("overview")
    val overview: ExamOverviewAnalyticsDto,
    
    @SerializedName("trend")
    val trend: List<ExamTrendDto>,
    
    @SerializedName("examPerformance")
    val examPerformance: List<ExamPerformanceDto>,
    
    @SerializedName("scoreDistribution")
    val scoreDistribution: List<ScoreDistributionDto>? = null,
    
    @SerializedName("averageScore")
    val averageScore: Double? = null,
    
    @SerializedName("averageTimeMinutes")
    val averageTimeMinutes: Int? = null
)

data class ScoreDistributionDto(
    @SerializedName("range")
    val range: String,
    
    @SerializedName("count")
    val count: Int
)

data class ExamOverviewAnalyticsDto(
    @SerializedName("totalExams")
    val totalExams: Int,
    
    @SerializedName("publishedExams")
    val publishedExams: Int,
    
    @SerializedName("totalAttempts")
    val totalAttempts: Int,
    
    @SerializedName("completedAttempts")
    val completedAttempts: Int,
    
    @SerializedName("completionRate")
    val completionRate: Int
)

data class ExamTrendDto(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("attempts")
    val attempts: Int
)

data class ExamPerformanceDto(
    @SerializedName("examId")
    val examId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("attempts")
    val attempts: Int,
    
    @SerializedName("completed")
    val completed: Int,
    
    @SerializedName("completionRate")
    val completionRate: Int
)

// ==================== Activities DTOs ====================

data class ActivityDto(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("user")
    val user: ActivityUserDto,
    
    @SerializedName("action")
    val action: String,
    
    @SerializedName("details")
    val details: ActivityDetailsDto? = null
)

data class ActivityUserDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("avatar")
    val avatar: String?
)

data class ActivityDetailsDto(
    @SerializedName("amount")
    val amount: Long? = null,
    
    @SerializedName("scoreSummary")
    val scoreSummary: Any? = null
)

// ==================== Extension Functions - toDomain() ====================

fun OverviewDto.toDomain() = DashboardOverview(
    users = UsersOverview(
        total = users.total,
        active = users.active,
        new = users.new,
        growth = users.growth
    ),
    studySets = StudySetsOverview(
        total = studySets.total,
        published = studySets.published,
        totalPurchases = studySets.totalPurchases
    ),
    revenue = RevenueOverview(
        total = revenue.total,
        thisMonth = revenue.thisPeriod,
        lastMonth = revenue.lastPeriod,
        growth = revenue.growth
    ),
    exams = ExamsOverview(
        total = exams.total,
        published = exams.published,
        totalAttempts = exams.totalAttempts,
        completedAttempts = exams.completedAttempts
    )
)

fun UserAnalyticsDto.toDomain() = UserAnalytics(
    growth = growth.map { UserGrowth(date = it.date, count = it.count) },
    byProficiency = byProficiency.map { UserByProficiency(proficiency = it.proficiency, count = it.count) },
    byStatus = byStatus.map { UserByStatus(status = it.status, count = it.count) },
    activeUsers = activeUsers?.let { ActiveUsers(daily = it.daily, weekly = it.weekly, monthly = it.monthly) }
)

fun LearningAnalyticsDto.toDomain() = LearningAnalytics(
    categories = CategoriesAnalytics(
        total = categories.total,
        completedByUsers = categories.completedByUsers,
        avgProgress = categories.avgProgress.toFloatOrNull() ?: 0f,
        popular = categories.popular.map { 
            PopularCategory(id = it.id, name = it.name, usersCount = it.usersCount, avgProgress = it.avgProgress.toFloatOrNull() ?: 0f) 
        }
    ),
    topics = TopicsAnalytics(
        total = topics.total,
        completedByUsers = topics.completedByUsers,
        popular = topics.popular.map { PopularTopic(id = it.id, name = it.name ?: "", usersCount = it.usersCount) }
    ),
    words = WordsAnalytics(
        total = words.total,
        learnedByUsers = words.learnedByUsers,
        avgPerUser = words.avgPerUser
    ),
    learningTrend = learningTrend?.filter { it.date != null }?.map { LearningTrend(date = it.date!!, wordsLearned = it.wordsLearned, topicsCompleted = it.topicsCompleted) }
)

fun RevenueAnalyticsDto.toDomain() = RevenueAnalytics(
    trend = trend.map { RevenueTrend(month = it.month, revenue = it.revenue, transactions = it.transactions) },
    transactions = Transactions(
        total = transactions.total,
        success = transactions.success,
        pending = transactions.pending,
        failed = transactions.failed,
        successRate = transactions.successRate
    ),
    topSelling = topSelling.map { 
        TopSelling(
            id = it.id, 
            title = it.title, 
            price = it.price, 
            ownerUsername = it.ownerUsername, 
            sales = it.sales, 
            revenue = it.revenue
        ) 
    }
)

fun ExamAnalyticsDto.toDomain() = ExamAnalytics(
    overview = ExamOverviewAnalytics(
        totalExams = overview.totalExams,
        publishedExams = overview.publishedExams,
        totalAttempts = overview.totalAttempts,
        completedAttempts = overview.completedAttempts,
        completionRate = overview.completionRate
    ),
    trend = trend.map { ExamTrend(date = it.date, attempts = it.attempts) },
    examPerformance = examPerformance.map { 
        ExamPerformance(
            examId = it.examId, 
            title = it.title, 
            code = it.code, 
            attempts = it.attempts, 
            completed = it.completed, 
            completionRate = it.completionRate
        ) 
    },
    scoreDistribution = scoreDistribution?.map { ScoreDistribution(range = it.range, count = it.count) },
    averageScore = averageScore,
    averageTimeMinutes = averageTimeMinutes
)

fun ActivityDto.toDomain() = Activity(
    type = ActivityType.fromValue(type),
    timestamp = timestamp,
    user = ActivityUser(id = user.id, username = user.username, avatar = user.avatar),
    action = action,
    details = details?.let { ActivityDetails(amount = it.amount) }
)
