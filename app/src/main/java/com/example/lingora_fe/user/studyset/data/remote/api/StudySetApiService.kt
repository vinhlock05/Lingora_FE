package com.example.lingora_fe.user.studyset.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.studyset.data.remote.dto.BuyStudySetResponseDto
import com.example.lingora_fe.user.studyset.data.remote.dto.CreateStudySetRequest
import com.example.lingora_fe.user.studyset.data.remote.dto.StudySetDto
import com.example.lingora_fe.user.studyset.data.remote.dto.StudySetListMetaData
import com.example.lingora_fe.user.studyset.data.remote.dto.UpdateStudySetRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StudySetApiService {

    @POST("studysets")
    suspend fun createStudySet(
        @Body request: CreateStudySetRequest
    ): ApiResponse<StudySetDto>

    @GET("studysets")
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

    @GET("studysets/own")
    suspend fun getOwnStudySets(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("visibility") visibility: String? = null,
        @Query("status") status: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<StudySetListMetaData>

    @GET("studysets/{id}")
    suspend fun getStudySetById(
        @Path("id") studySetId: Int
    ): ApiResponse<StudySetDto>

    @PATCH("studysets/{id}")
    suspend fun updateStudySet(
        @Path("id") studySetId: Int,
        @Body request: UpdateStudySetRequest
    ): ApiResponse<StudySetDto>

    @DELETE("studysets/{id}")
    suspend fun deleteStudySet(
        @Path("id") studySetId: Int
    ): ApiResponse<Any>

    @POST("studysets/{id}/buy")
    suspend fun buyStudySet(
        @Path("id") studySetId: Int
    ): ApiResponse<BuyStudySetResponseDto>
}

