package com.example.lingora_fe.user.withdrawal.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.withdrawal.domain.model.Balance
import com.example.lingora_fe.user.withdrawal.domain.model.CreateWithdrawalData
import com.example.lingora_fe.user.withdrawal.domain.model.Withdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalFilterOptions
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalListMetadata

/**
 * Repository interface for User Withdrawal operations
 */
interface WithdrawalRepository {

    /**
     * Create a new withdrawal request
     */
    suspend fun createWithdrawal(
        data: CreateWithdrawalData
    ): Either<AppFailure, Withdrawal>

    /**
     * Get user's balance information
     */
    suspend fun getBalance(): Either<AppFailure, Balance>

    /**
     * Get user's withdrawal history with filters
     */
    suspend fun getMyWithdrawals(
        filterOptions: WithdrawalFilterOptions
    ): Either<AppFailure, WithdrawalListMetadata>

    /**
     * Get a specific withdrawal by ID
     */
    suspend fun getWithdrawalById(
        id: Int
    ): Either<AppFailure, Withdrawal>
}
