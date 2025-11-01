package com.example.lingora_fe.user.vocabulary.domain.repository

import arrow.core.Either
import com.example.lingora_fe.user.vocabulary.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun getCategories(): Either<String, List<Category>>
    suspend fun getCategoryById(categoryId: Int): Either<String, Category>
    fun observeCategories(): Flow<List<Category>>
}

