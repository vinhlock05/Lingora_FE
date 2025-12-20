package com.example.lingora_fe.admin.withdrawal.data.remote.api

import com.example.lingora_fe.admin.withdrawal.data.remote.dto.AdminWithdrawalDto
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.AdminWithdrawalListMetaDataDto
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.CompleteWithdrawalRequest
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.FailWithdrawalRequest
import com.example.lingora_fe.admin.withdrawal.data.remote.dto.RejectWithdrawalRequest
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

/**
 * Retrofit API interface for Admin Withdrawal endpoints
 */
interface AdminWithdrawalApiService {

    /**
     * Get all withdrawal requests (Admin)
     * GET /withdrawals/admin/all
     */
    @GET("withdrawals/admin/all")
    suspend fun getAllWithdrawals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("userId") userId: Int? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<AdminWithdrawalListMetaDataDto>

    /**
     * Get a specific withdrawal by ID (Admin)
     * GET /withdrawals/admin/{id}
     */
    @GET("withdrawals/admin/{id}")
    suspend fun getWithdrawalById(
        @Path("id") id: Int
    ): ApiResponse<AdminWithdrawalDto>

    /**
     * Approve a withdrawal request
     * PUT /withdrawals/admin/{id}/approve
     */
    @PUT("withdrawals/admin/{id}/approve")
    suspend fun approveWithdrawal(
        @Path("id") id: Int
    ): ApiResponse<AdminWithdrawalDto>

    /**
     * Reject a withdrawal request
     * PUT /withdrawals/admin/{id}/reject
     */
    @PUT("withdrawals/admin/{id}/reject")
    suspend fun rejectWithdrawal(
        @Path("id") id: Int,
        @Body request: RejectWithdrawalRequest
    ): ApiResponse<AdminWithdrawalDto>

    /**
     * Mark withdrawal as completed
     * PUT /withdrawals/admin/{id}/complete
     */
    @PUT("withdrawals/admin/{id}/complete")
    suspend fun completeWithdrawal(
        @Path("id") id: Int,
        @Body request: CompleteWithdrawalRequest
    ): ApiResponse<AdminWithdrawalDto>

    /**
     * Mark withdrawal as failed
     * PUT /withdrawals/admin/{id}/fail
     */
    @PUT("withdrawals/admin/{id}/fail")
    suspend fun failWithdrawal(
        @Path("id") id: Int,
        @Body request: FailWithdrawalRequest
    ): ApiResponse<AdminWithdrawalDto>
}
