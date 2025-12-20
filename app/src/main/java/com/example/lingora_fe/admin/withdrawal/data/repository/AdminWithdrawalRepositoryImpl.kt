package com.example.lingora_fe.admin.withdrawal.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.withdrawal.data.remote.api.AdminWithdrawalApiService
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.CompleteWithdrawalRequest
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.FailWithdrawalRequest
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.RejectWithdrawalRequest
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.toDomain
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawal
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalFilterOptions
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalListMetadata
import com.example.lingora_fe.admin.withdrawal.domain.repository.AdminWithdrawalRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

/**
 * Repository implementation for Admin Withdrawal operations
 */
class AdminWithdrawalRepositoryImpl @Inject constructor(
    private val apiService: AdminWithdrawalApiService
) : AdminWithdrawalRepository {

    override suspend fun getAllWithdrawals(
        filterOptions: AdminWithdrawalFilterOptions
    ): Either<AppFailure, AdminWithdrawalListMetadata> {
        return Either.catch {
            val response = apiService.getAllWithdrawals(
                page = filterOptions.page,
                limit = filterOptions.limit,
                status = filterOptions.status?.value,
                userId = filterOptions.userId,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getWithdrawalById(id: Int): Either<AppFailure, AdminWithdrawal> {
        return Either.catch {
            val response = apiService.getWithdrawalById(id)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun approveWithdrawal(id: Int): Either<AppFailure, AdminWithdrawal> {
        return Either.catch {
            val response = apiService.approveWithdrawal(id)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun rejectWithdrawal(
        id: Int,
        reason: String?
    ): Either<AppFailure, AdminWithdrawal> {
        return Either.catch {
            val request = RejectWithdrawalRequest(rejectionReason = reason)
            val response = apiService.rejectWithdrawal(id, request)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun completeWithdrawal(
        id: Int,
        transactionReference: String?
    ): Either<AppFailure, AdminWithdrawal> {
        return Either.catch {
            val request = CompleteWithdrawalRequest(transactionReference = transactionReference)
            val response = apiService.completeWithdrawal(id, request)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun failWithdrawal(
        id: Int,
        reason: String?
    ): Either<AppFailure, AdminWithdrawal> {
        return Either.catch {
            val request = FailWithdrawalRequest(reason = reason)
            val response = apiService.failWithdrawal(id, request)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
}
