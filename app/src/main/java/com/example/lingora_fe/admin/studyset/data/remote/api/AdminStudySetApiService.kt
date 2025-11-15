package com.example.lingora_fe.admin.studyset.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.studyset.data.remote.dto.StudySetDto
import com.example.lingora_fe.user.studyset.data.remote.dto.StudySetListMetaData
import retrofit2.http.*

interface AdminStudySetApiService {

    @GET("admin/studysets")
    suspend fun getAllStudySets(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("visibility") visibility: String? = null,
        @Query("status") status: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<StudySetListMetaData>

    @GET("admin/studysets/pending")
    suspend fun getPendingStudySets(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<StudySetListMetaData>

    @GET("admin/studysets/{id}")
    suspend fun getStudySetById(
        @Path("id") studySetId: Int
    ): ApiResponse<StudySetDto>

    @PATCH("admin/studysets/{id}/approve")
    suspend fun approveStudySet(
        @Path("id") studySetId: Int
    ): ApiResponse<StudySetDto>

    @PATCH("admin/studysets/{id}/reject")
    suspend fun rejectStudySet(
        @Path("id") studySetId: Int,
        @Body request: RejectStudySetRequest
    ): ApiResponse<StudySetDto>
}

data class RejectStudySetRequest(
    val reason: String? = null
)

