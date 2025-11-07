package com.example.lingora_fe.admin.category.data.remote.api

import com.example.lingora_fe.admin.category.data.remote.dto.*
import com.example.lingora_fe.admin.topic.data.remote.dto.CategoryWithTopicsDto
import com.example.lingora_fe.core.network.ApiResponse
import retrofit2.http.*

interface CategoryApiService {

    @GET("categories")
    suspend fun getAllCategories(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<CategoryListMetaData>

    // Get topics in a specific category
    @GET("categories/{id}/topics")
    suspend fun getCategoryById(
        @Path("id") categoryId: Int,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<CategoryWithTopicsDto>

    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): ApiResponse<CategoryDto>

    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") categoryId: Int,
        @Body request: UpdateCategoryRequest
    ): ApiResponse<CategoryDto>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") categoryId: Int
    ): ApiResponse<Any>
}