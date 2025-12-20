package com.example.lingora_fe.user.withdrawal.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.lingora_fe.user.withdrawal.domain.model.Balance
import com.example.lingora_fe.user.withdrawal.domain.model.Withdrawal
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalListMetadata
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import java.util.Date

/**
 * DTO for single withdrawal response
 */
data class WithdrawalDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("bankName")
    val bankName: String,
    
    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String,
    
    @SerializedName("bankAccountName")
    val bankAccountName: String,
    
    @SerializedName("bankBranch")
    val bankBranch: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("rejectionReason")
    val rejectionReason: String?,
    
    @SerializedName("transactionReference")
    val transactionReference: String?,
    
    @SerializedName("processedBy")
    val processedBy: Int?,
    
    @SerializedName("createdAt")
    val createdAt: Date,
    
    @SerializedName("updatedAt")
    val updatedAt: Date
)

/**
 * DTO for balance response
 */
data class BalanceDto(
    @SerializedName("totalEarnings")
    val totalEarnings: Long,
    
    @SerializedName("pendingWithdrawal")
    val pendingWithdrawal: Long,
    
    @SerializedName("withdrawnAmount")
    val withdrawnAmount: Long,
    
    @SerializedName("availableBalance")
    val availableBalance: Long
)

/**
 * DTO for withdrawal list metadata response
 */
data class WithdrawalListMetaDataDto(
    @SerializedName("currentPage")
    val currentPage: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("withdrawals")
    val withdrawals: List<WithdrawalDto>? = null
)

/**
 * Request body for creating withdrawal
 */
data class CreateWithdrawalRequest(
    @SerializedName("amount")
    val amount: Long,
    
    @SerializedName("bankName")
    val bankName: String,
    
    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String,
    
    @SerializedName("bankAccountName")
    val bankAccountName: String,
    
    @SerializedName("bankBranch")
    val bankBranch: String? = null
)

// Extension functions to convert DTOs to domain models

fun WithdrawalDto.toDomain(): Withdrawal {
    return Withdrawal(
        id = id,
        amount = amount,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        bankAccountName = bankAccountName,
        bankBranch = bankBranch,
        status = WithdrawalStatus.fromValue(status),
        rejectionReason = rejectionReason,
        transactionReference = transactionReference,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun BalanceDto.toDomain(): Balance {
    return Balance(
        totalEarnings = totalEarnings,
        pendingWithdrawal = pendingWithdrawal,
        withdrawnAmount = withdrawnAmount,
        availableBalance = availableBalance
    )
}

fun WithdrawalListMetaDataDto.toDomain(): WithdrawalListMetadata {
    return WithdrawalListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        withdrawals = withdrawals?.map { it.toDomain() } ?: emptyList()
    )
}
