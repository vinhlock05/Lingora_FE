package com.example.lingora_fe.admin.category.data.repository

import arrow.core.Either
import com.example.lingora_fe.admin.category.data.remote.api.CategoryApiService
import com.example.lingora_fe.admin.category.data.remote.dto.toDomain
import com.example.lingora_fe.admin.category.data.remote.dto.toDto
import com.example.lingora_fe.admin.category.domain.model.Category
import com.example.lingora_fe.admin.category.domain.model.CategoryFilterOptions
import com.example.lingora_fe.admin.category.domain.model.CategoryListMetadata
import com.example.lingora_fe.admin.category.domain.model.CreateCategoryData
import com.example.lingora_fe.admin.category.domain.model.UpdateCategoryData
import com.example.lingora_fe.admin.category.domain.repository.CategoryRepository
import com.example.lingora_fe.admin.topic.data.remote.dto.toDomain as topicToDomain
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val apiService: CategoryApiService
) : CategoryRepository {

    override suspend fun getAllCategories(
        token: String,
        filterOptions: CategoryFilterOptions
    ): Either<AppFailure, CategoryListMetadata> {
        return Either.catch {
            val response = apiService.getAllCategories(
                limit = filterOptions.limit,
                page = filterOptions.page,
                search = filterOptions.search,
                sort = filterOptions.sort
            )
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getCategoryById(
        token: String,
        categoryId: Int
    ): Either<AppFailure, Category> {
        return Either.catch {
            val response = apiService.getCategoryById(
                categoryId = categoryId,
                limit = 1, // Only need category info, not topics
                page = 1
            )
            // Extract only category info from CategoryWithTopicsDto
            val categoryWithTopics = response.metaData?.topicToDomain() 
                ?: throw Exception(response.message)
            
            Category(
                id = categoryWithTopics.id,
                name = categoryWithTopics.name,
                description = categoryWithTopics.description,
                totalTopics = categoryWithTopics.totalTopics
            )
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createCategory(
        token: String,
        categoryData: CreateCategoryData
    ): Either<AppFailure, Category> {
        return Either.catch {
            val response = apiService.createCategory(categoryData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateCategory(
        token: String,
        categoryId: Int,
        categoryData: UpdateCategoryData
    ): Either<AppFailure, Category> {
        return Either.catch {
            val response = apiService.updateCategory(categoryId, categoryData.toDto())
            response.metaData?.toDomain() ?: throw Exception(response.message)
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteCategory(
        token: String,
        categoryId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteCategory(categoryId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}

