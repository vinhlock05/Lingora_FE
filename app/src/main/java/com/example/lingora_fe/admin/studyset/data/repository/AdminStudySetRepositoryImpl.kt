package com.example.lingora_fe.admin.studyset.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.studyset.data.remote.api.AdminStudySetApiService
import com.example.lingora_fe.admin.studyset.data.remote.api.RejectStudySetRequest
import com.example.lingora_fe.admin.studyset.domain.repository.AdminStudySetRepository
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.studyset.data.remote.dto.toDomain
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetListMetadata
import javax.inject.Inject

class AdminStudySetRepositoryImpl @Inject constructor(
    private val apiService: AdminStudySetApiService
) : AdminStudySetRepository {

    override suspend fun getAllStudySets(
        token: String,
        limit: Int,
        page: Int,
        search: String?,
        visibility: String?,
        status: String?,
        minPrice: Int?,
        maxPrice: Int?,
        sort: String?
    ): Either<AppFailure, StudySetListMetadata> {
        return Either.catch {
            val response = apiService.getAllStudySets(
                limit = limit,
                page = page,
                search = search,
                visibility = visibility,
                status = status,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sort = sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getPendingStudySets(
        token: String,
        limit: Int,
        page: Int,
        search: String?,
        sort: String?
    ): Either<AppFailure, StudySetListMetadata> {
        return Either.catch {
            val response = apiService.getPendingStudySets(
                limit = limit,
                page = page,
                search = search,
                sort = sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getStudySetById(
        token: String,
        studySetId: Int
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.getStudySetById(studySetId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun approveStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.approveStudySet(studySetId)
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun rejectStudySet(
        token: String,
        studySetId: Int,
        reason: String?
    ): Either<AppFailure, StudySet> {
        return Either.catch {
            val response = apiService.rejectStudySet(studySetId, RejectStudySetRequest(reason))
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }
}

