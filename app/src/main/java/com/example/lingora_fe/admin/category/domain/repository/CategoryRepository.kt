package com.example.lingora_fe.admin.category.domain.repository

import arrow.core.Either
import com.example.lingora_fe.admin.category.domain.model.Category
import com.example.lingora_fe.admin.category.domain.model.CategoryFilterOptions
import com.example.lingora_fe.admin.category.domain.model.CategoryListMetadata
import com.example.lingora_fe.admin.category.domain.model.CreateCategoryData
import com.example.lingora_fe.admin.category.domain.model.UpdateCategoryData
import com.example.lingora_fe.core.error.AppFailure

interface CategoryRepository {

    /**
     * Get list of categories with filtering and pagination
     */
    suspend fun getAllCategories(
        token: String,
        filterOptions: CategoryFilterOptions
    ): Either<AppFailure, CategoryListMetadata>

    /**
     * Get category details by ID
     */
    suspend fun getCategoryById(
        token: String,
        categoryId: Int
    ): Either<AppFailure, Category>

    /**
     * Create a new category
     */
    suspend fun createCategory(
        token: String,
        categoryData: CreateCategoryData
    ): Either<AppFailure, Category>

    /**
     * Update existing category
     */
    suspend fun updateCategory(
        token: String,
        categoryId: Int,
        categoryData: UpdateCategoryData
    ): Either<AppFailure, Category>

    /**
     * Delete a category
     */
    suspend fun deleteCategory(
        token: String,
        categoryId: Int
    ): Either<AppFailure, Unit>
}

