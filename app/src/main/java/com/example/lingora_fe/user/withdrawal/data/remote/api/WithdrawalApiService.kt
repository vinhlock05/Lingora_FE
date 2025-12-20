package com.example.lingora_fe.user.withdrawal.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.withdrawal.data.remote.dto.BalanceDto
import com.example.lingora_fe.user.withdrawal.data.remote.dto.CreateWithdrawalRequest
import com.example.lingora_fe.user.withdrawal.data.remote.dto.WithdrawalDto
import com.example.lingora_fe.user.withdrawal.data.remote.dto.WithdrawalListMetaDataDto
import retrofit2.http.*

/**
 * Retrofit API interface for User Withdrawal endpoints
 */
interface WithdrawalApiService {

    /**
     * Create a new withdrawal request
     * POST /withdrawals
     */
    @POST("withdrawals")
    suspend fun createWithdrawal(
        @Body request: CreateWithdrawalRequest
    ): ApiResponse<WithdrawalDto>

    /**
     * Get user's balance information
     * GET /withdrawals/balance
     */
    @GET("withdrawals/balance")
    suspend fun getBalance(): ApiResponse<BalanceDto>

    /**
     * Get user's withdrawal history with pagination and filters
     * GET /withdrawals/me
     */
    @GET("withdrawals/me")
    suspend fun getMyWithdrawals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<WithdrawalListMetaDataDto>

    /**
     * Get a specific withdrawal by ID
     * GET /withdrawals/{id}
     */
    @GET("withdrawals/{id}")
    suspend fun getWithdrawalById(
        @Path("id") id: Int
    ): ApiResponse<WithdrawalDto>
}
