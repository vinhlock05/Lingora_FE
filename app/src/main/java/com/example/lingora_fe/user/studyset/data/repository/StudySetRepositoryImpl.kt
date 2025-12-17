package com.example.lingora_fe.user.studyset.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.studyset.data.remote.api.StudySetApiService
import com.example.lingora_fe.user.studyset.data.remote.dto.toDomain
import com.example.lingora_fe.user.studyset.data.remote.dto.toDto
import com.example.lingora_fe.user.studyset.domain.model.BuyStudySetResponse
import com.example.lingora_fe.user.studyset.domain.model.CreateStudySetData
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetFilterOptions
import com.example.lingora_fe.user.studyset.domain.model.StudySetListMetadata
import com.example.lingora_fe.user.studyset.domain.model.UpdateStudySetData
import com.example.lingora_fe.user.studyset.domain.repository.StudySetRepository
import javax.inject.Inject

class StudySetRepositoryImpl @Inject constructor(
    private val apiService: StudySetApiService
) : StudySetRepository {

    override suspend fun createStudySet(
        token: String,
        studySetData: CreateStudySetData
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.createStudySet(studySetData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAllStudySets(
        token: String,
        filterOptions: StudySetFilterOptions
    ): Either<AppFailure, StudySetListMetadata> {
        return Either.catch {
            val response = apiService.getAllStudySets(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                visibility = filterOptions.visibility?.value,
                status = filterOptions.status?.value,
                minPrice = filterOptions.minPrice,
                maxPrice = filterOptions.maxPrice,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getOwnStudySets(
        token: String,
        filterOptions: StudySetFilterOptions
    ): Either<AppFailure, StudySetListMetadata> {
        return Either.catch {
            val response = apiService.getOwnStudySets(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                visibility = filterOptions.visibility?.value,
                status = filterOptions.status?.value,
                minPrice = filterOptions.minPrice,
                maxPrice = filterOptions.maxPrice,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getStudySetById(
        studySetId: Int
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.getStudySetById(studySetId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateStudySet(
        token: String,
        studySetId: Int,
        studySetData: UpdateStudySetData
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.updateStudySet(studySetId, studySetData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteStudySet(studySetId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun buyStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, BuyStudySetResponse> {
        return Either.catch {
            val response = apiService.buyStudySet(studySetId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun verifyVNPayPayment(
        token: String,
        vnpParams: Map<String, String>
    ): Either<AppFailure, com.example.lingora_fe.user.studyset.domain.repository.VNPayVerifyResponse> {
        return Either.catch {
            val response = apiService.verifyVNPayPayment(vnpParams)
            
            // Extract from metaData
            val metaData = response.metaData
            val success = metaData?.success ?: false
            val message = metaData?.message ?: response.message ?: "Unknown error"
            
            com.example.lingora_fe.user.studyset.domain.repository.VNPayVerifyResponse(
                success = success,
                message = message
            )
        }.mapLeft { it.toAppFailure() }
    }
}

