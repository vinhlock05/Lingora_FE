package com.example.lingora_fe.user.studyset.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.studyset.domain.model.BuyStudySetResponse
import com.example.lingora_fe.user.studyset.domain.model.CreateStudySetData
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetFilterOptions
import com.example.lingora_fe.user.studyset.domain.model.StudySetListMetadata
import com.example.lingora_fe.user.studyset.domain.model.UpdateStudySetData

interface StudySetRepository {

    /**
     * Create a new study set
     */
    suspend fun createStudySet(
        token: String,
        studySetData: CreateStudySetData
    ): Either<AppFailure, StudySet>

    /**
     * Get list of study sets (Public & Published)
     */
    suspend fun getAllStudySets(
        token: String,
        filterOptions: StudySetFilterOptions
    ): Either<AppFailure, StudySetListMetadata>

    /**
     * Get study sets owned by current user
     */
    suspend fun getOwnStudySets(
        token: String,
        filterOptions: StudySetFilterOptions
    ): Either<AppFailure, StudySetListMetadata>

    /**
     * Get study set by ID
     */
    suspend fun getStudySetById(
        token: String,
        studySetId: Int
    ): Either<AppFailure, StudySet>

    /**
     * Update study set
     */
    suspend fun updateStudySet(
        token: String,
        studySetId: Int,
        studySetData: UpdateStudySetData
    ): Either<AppFailure, StudySet>

    /**
     * Delete study set
     */
    suspend fun deleteStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, Unit>

    /**
     * Buy study set
     */
    suspend fun buyStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, BuyStudySetResponse>
    
    /**
     * Verify VNPay payment return
     */
    suspend fun verifyVNPayPayment(
        token: String,
        vnpParams: Map<String, String>
    ): Either<AppFailure, VNPayVerifyResponse>
}

data class VNPayVerifyResponse(
    val success: Boolean,
    val message: String?
)

