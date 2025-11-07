package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.CategoryProgress

interface CategoryRepository {
    // Categories with progress
    suspend fun getCategoriesWithProgress(
        limit: Int = 20,
        page: Int = 1,
        search: String? = null
    ): Either<String, Pair<List<CategoryProgress>, PaginationMeta>>
}

data class PaginationMeta(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int
)

