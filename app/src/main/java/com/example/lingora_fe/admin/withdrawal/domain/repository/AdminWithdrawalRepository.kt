package com.example.lingora_fe.admin.withdrawal.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawal
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalFilterOptions
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalListMetadata
import com.example.lingora_fe.core.error.AppFailure

/**
 * Repository interface for Admin Withdrawal operations
 */
interface AdminWithdrawalRepository {

    /**
     * Get all withdrawal requests with filters
     */
    suspend fun getAllWithdrawals(
        filterOptions: AdminWithdrawalFilterOptions
    ): Either<AppFailure, AdminWithdrawalListMetadata>

    /**
     * Get a specific withdrawal by ID
     */
    suspend fun getWithdrawalById(
        id: Int
    ): Either<AppFailure, AdminWithdrawal>

    /**
     * Approve a withdrawal request
     */
    suspend fun approveWithdrawal(
        id: Int
    ): Either<AppFailure, AdminWithdrawal>

    /**
     * Reject a withdrawal request
     */
    suspend fun rejectWithdrawal(
        id: Int,
        reason: String?
    ): Either<AppFailure, AdminWithdrawal>

    /**
     * Mark withdrawal as completed
     */
    suspend fun completeWithdrawal(
        id: Int,
        transactionReference: String?
    ): Either<AppFailure, AdminWithdrawal>

    /**
     * Mark withdrawal as failed
     */
    suspend fun failWithdrawal(
        id: Int,
        reason: String?
    ): Either<AppFailure, AdminWithdrawal>
}
