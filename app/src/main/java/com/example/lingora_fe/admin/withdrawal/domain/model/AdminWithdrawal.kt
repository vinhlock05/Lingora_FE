package com.example.lingora_fe.admin.withdrawal.domain.model

import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import java.util.Date

/**
 * User info for admin withdrawal view
 */
data class WithdrawalUser(
    val id: Int,
    val username: String,
    val email: String
)

/**
 * Admin withdrawal model with user info
 */
data class AdminWithdrawal(
    val id: Int,
    val amount: Long,
    val bankName: String,
    val bankAccountNumber: String,
    val bankAccountName: String,
    val bankBranch: String?,
    val status: WithdrawalStatus,
    val rejectionReason: String?,
    val transactionReference: String?,
    val processedBy: Int?,
    val user: WithdrawalUser,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Admin withdrawal list metadata with pagination
 */
data class AdminWithdrawalListMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val withdrawals: List<AdminWithdrawal>
)

/**
 * Filter options for admin withdrawal list
 */
data class AdminWithdrawalFilterOptions(
    val page: Int = 1,
    val limit: Int = 10,
    val status: WithdrawalStatus? = null,
    val userId: Int? = null,
    val sort: String? = "-createdAt"
)
