package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.local.VocabularyLocalDataSource
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.data.local.entity.CategoryEntity
import com.example.lingora_fe.user.vocabulary.domain.model.Category
import com.example.lingora_fe.user.vocabulary.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource,
    private val localDataSource: VocabularyLocalDataSource
) : CategoryRepository {

    override suspend fun getCategories(): Either<String, List<Category>> {
        return try {
            val response = remoteDataSource.getCategories()
            val categories = response.data.map { it.toDomain() }
            
            // Cache in local database
            val entities = response.data.map { dto ->
                CategoryEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    createdAt = java.sql.Timestamp.valueOf(dto.createdAt).time
                )
            }
            localDataSource.insertCategories(entities)
            
            Either.Right(categories)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localCategories = localDataSource.getCategories().map { it.toDomain() }
                Either.Right(localCategories)
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch categories")
            }
        }
    }

    override suspend fun getCategoryById(categoryId: Int): Either<String, Category> {
        return try {
            val response = remoteDataSource.getCategoryById(categoryId)
            val category = response.data.toDomain()
            
            // Cache in local database
            val entity = CategoryEntity(
                id = response.data.id,
                name = response.data.name,
                description = response.data.description,
                createdAt = java.sql.Timestamp.valueOf(response.data.createdAt).time
            )
            localDataSource.insertCategory(entity)
            
            Either.Right(category)
        } catch (e: Exception) {
            // Fallback to local data
            try {
                val localCategory = localDataSource.getCategoryById(categoryId)?.toDomain()
                if (localCategory != null) {
                    Either.Right(localCategory)
                } else {
                    Either.Left("Category not found")
                }
            } catch (localException: Exception) {
                Either.Left(e.message ?: "Failed to fetch category")
            }
        }
    }

    override fun observeCategories(): Flow<List<Category>> {
        return localDataSource.observeCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

