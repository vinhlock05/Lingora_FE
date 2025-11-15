package com.example.lingora_fe.admin.studyset.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.studyset.domain.model.StudySet
import com.example.lingora_fe.user.studyset.domain.model.StudySetListMetadata

interface AdminStudySetRepository {
    suspend fun getAllStudySets(
        token: String,
        limit: Int = 10,
        page: Int = 1,
        search: String? = null,
        visibility: String? = null,
        status: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        sort: String? = null
    ): Either<AppFailure, StudySetListMetadata>

    suspend fun getPendingStudySets(
        token: String,
        limit: Int = 10,
        page: Int = 1,
        search: String? = null,
        sort: String? = null
    ): Either<AppFailure, StudySetListMetadata>

    suspend fun getStudySetById(
        token: String,
        studySetId: Int
    ): Either<AppFailure, StudySet>

    suspend fun approveStudySet(
        token: String,
        studySetId: Int
    ): Either<AppFailure, StudySet>

    suspend fun rejectStudySet(
        token: String,
        studySetId: Int,
        reason: String? = null
    ): Either<AppFailure, StudySet>
}

