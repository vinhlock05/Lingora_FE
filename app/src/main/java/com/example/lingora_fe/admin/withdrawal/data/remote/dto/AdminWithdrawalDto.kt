package com.example.lingora_fe.admin.withdrawal.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawal
import com.example.lingora_fe.admin.withdrawal.domain.model.AdminWithdrawalListMetadata
import com.example.lingora_fe.admin.withdrawal.domain.model.WithdrawalUser
import com.example.lingora_fe.user.withdrawal.domain.model.WithdrawalStatus
import java.util.Date

/**
 * DTO for user info in admin withdrawal response
 */
data class WithdrawalUserDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String
)

/**
 * DTO for admin withdrawal response (includes user info)
 */
data class AdminWithdrawalDto(
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
    
    @SerializedName("user")
    val user: WithdrawalUserDto,
    
    @SerializedName("createdAt")
    val createdAt: Date,
    
    @SerializedName("updatedAt")
    val updatedAt: Date
)

/**
 * DTO for admin withdrawal list metadata response
 */
data class AdminWithdrawalListMetaDataDto(
    @SerializedName("currentPage")
    val currentPage: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("withdrawals")
    val withdrawals: List<AdminWithdrawalDto>? = null
)

/**
 * Request body for rejecting withdrawal
 */
data class RejectWithdrawalRequest(
    @SerializedName("rejectionReason")
    val rejectionReason: String? = null
)

/**
 * Request body for completing withdrawal
 */
data class CompleteWithdrawalRequest(
    @SerializedName("transactionReference")
    val transactionReference: String? = null
)

/**
 * Request body for marking withdrawal as failed
 */
data class FailWithdrawalRequest(
    @SerializedName("reason")
    val reason: String? = null
)

// Extension functions to convert DTOs to domain models

fun WithdrawalUserDto.toDomain(): WithdrawalUser {
    return WithdrawalUser(
        id = id,
        username = username,
        email = email
    )
}

fun AdminWithdrawalDto.toDomain(): AdminWithdrawal {
    return AdminWithdrawal(
        id = id,
        amount = amount,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        bankAccountName = bankAccountName,
        bankBranch = bankBranch,
        status = WithdrawalStatus.fromValue(status),
        rejectionReason = rejectionReason,
        transactionReference = transactionReference,
        processedBy = processedBy,
        user = user.toDomain(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AdminWithdrawalListMetaDataDto.toDomain(): AdminWithdrawalListMetadata {
    return AdminWithdrawalListMetadata(
        currentPage = currentPage,
        totalPages = totalPages,
        total = total,
        withdrawals = withdrawals?.map { it.toDomain() } ?: emptyList()
    )
}
