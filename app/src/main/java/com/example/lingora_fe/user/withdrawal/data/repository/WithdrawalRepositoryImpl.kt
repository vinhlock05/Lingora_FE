package com.example.lingora_fe.user.withdrawal.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.withdrawal.data.remote.api.WithdrawalApiService
import com.example.lingora_fe.user.withdrawal.data.remote.dto.CreateWithdrawalRequest
import com.example.lingora_fe.user.withdrawal.data.remote.dto.toDomain
import com.example.lingora_fe.user.withdrawal.domain.model.Balance
import com.example.lingora_fe.user.withdrawal.domain.model.CreateWithdrawalData
import com.example.lingora_fe.user.withdrawal.domain.model.Withdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalFilterOptions
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalListMetadata
import com.example.lingora_fe.user.withdrawal.domain.repository.WithdrawalRepository
import javax.inject.Inject

/**
 * Repository implementation for User Withdrawal operations
 */
class WithdrawalRepositoryImpl @Inject constructor(
    private val apiService: WithdrawalApiService
) : WithdrawalRepository {

    override suspend fun createWithdrawal(
        data: CreateWithdrawalData
    ): Either<AppFailure, Withdrawal> {
        return Either.catch {
            val request = CreateWithdrawalRequest(
                amount = data.amount,
                bankName = data.bankName,
                bankAccountNumber = data.bankAccountNumber,
                bankAccountName = data.bankAccountName,
                bankBranch = data.bankBranch
            )
            val response = apiService.createWithdrawal(request)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getBalance(): Either<AppFailure, Balance> {
        return Either.catch {
            val response = apiService.getBalance()
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getMyWithdrawals(
        filterOptions: WithdrawalFilterOptions
    ): Either<AppFailure, WithdrawalListMetadata> {
        return Either.catch {
            val response = apiService.getMyWithdrawals(
                page = filterOptions.page,
                limit = filterOptions.limit,
                status = filterOptions.status?.value,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getWithdrawalById(id: Int): Either<AppFailure, Withdrawal> {
        return Either.catch {
            val response = apiService.getWithdrawalById(id)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
}
