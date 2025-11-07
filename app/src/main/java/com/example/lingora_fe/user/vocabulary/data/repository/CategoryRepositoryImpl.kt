package com.example.lingora_fe.user.vocabulary.data.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.data.datasource.remote.VocabularyRemoteDataSource
import com.example.lingora_fe.user.vocabulary.domain.repository.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) : CategoryRepository {

    override suspend fun getCategoriesWithProgress(
        limit: Int,
        page: Int,
        search: String?
    ): Either<String, Pair<List<com.example.lingora_fe.user.vocabulary.domain.model.CategoryProgress>, com.example.lingora_fe.user.vocabulary.domain.repository.PaginationMeta>> {
        return try {
            val response = remoteDataSource.getCategoriesWithProgress(limit, page, search)
            val categories = response.metaData?.categories?.map { dto ->
                com.example.lingora_fe.user.vocabulary.domain.model.CategoryProgress(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    totalTopics = dto.totalTopics,
                    completedTopics = dto.completedTopics,
                    progressPercent = dto.progressPercent,
                    completed = dto.completed
                )
            } ?: emptyList()
            val meta = response.metaData?.let {
                com.example.lingora_fe.user.vocabulary.domain.repository.PaginationMeta(
                    currentPage = it.currentPage,
                    totalPages = it.totalPages,
                    total = it.total
                )
            } ?: com.example.lingora_fe.user.vocabulary.domain.repository.PaginationMeta(
                currentPage = page,
                totalPages = 1,
                total = categories.size
            )
            Either.Right(Pair(categories, meta))
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to fetch categories with progress")
        }
    }
}

