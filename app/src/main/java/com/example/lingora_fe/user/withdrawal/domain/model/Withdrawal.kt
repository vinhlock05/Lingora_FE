package com.example.lingora_fe.user.withdrawal.domain.model

import java.util.Date

/**
 * Withdrawal status enum with display names
 * Must match backend enum exactly:
 * PENDING -> PROCESSING -> COMPLETED
 * PENDING -> REJECTED
 * PROCESSING -> FAILED
 */
enum class WithdrawalStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Chờ xử lý"),
    PROCESSING("PROCESSING", "Đang xử lý"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    REJECTED("REJECTED", "Từ chối"),
    FAILED("FAILED", "Thất bại");

    companion object {
        fun fromValue(value: String): WithdrawalStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

/**
 * Main withdrawal domain model
 */
data class Withdrawal(
    val id: Int,
    val amount: Long,
    val bankName: String,
    val bankAccountNumber: String,
    val bankAccountName: String,
    val bankBranch: String?,
    val status: WithdrawalStatus,
    val rejectionReason: String?,
    val transactionReference: String?,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Balance information
 */
data class Balance(
    val totalEarnings: Long,
    val pendingWithdrawal: Long,
    val withdrawnAmount: Long,
    val availableBalance: Long
)

/**
 * Withdrawal list metadata with pagination
 */
data class WithdrawalListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val withdrawals: List<Withdrawal>
)

/**
 * Create withdrawal request data
 */
data class CreateWithdrawalData(
    val amount: Long,
    val bankName: String,
    val bankAccountNumber: String,
    val bankAccountName: String,
    val bankBranch: String? = null
)

/**
 * Filter options for withdrawal list
 */
data class WithdrawalFilterOptions(
    val page: Int = 1,
    val limit: Int = 10,
    val status: WithdrawalStatus? = null,
    val sort: String? = "-createdAt"
)
