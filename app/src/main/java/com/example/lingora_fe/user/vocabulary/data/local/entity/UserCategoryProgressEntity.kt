package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.UserCategoryProgress

@Entity(tableName = "user_category_progress")
data class UserCategoryProgressEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val categoryId: Int,
    val totalTopics: Int,
    val progressPercent: Float,
    val completed: Boolean
) {
    fun toDomain(): UserCategoryProgress {
        return UserCategoryProgress(
            id = id,
            userId = userId,
            categoryId = categoryId,
            totalTopics = totalTopics,
            progressPercent = progressPercent,
            completed = completed
        )
    }

    companion object {
        fun fromDomain(progress: UserCategoryProgress): UserCategoryProgressEntity {
            return UserCategoryProgressEntity(
                id = progress.id,
                userId = progress.userId,
                categoryId = progress.categoryId,
                totalTopics = progress.totalTopics,
                progressPercent = progress.progressPercent,
                completed = progress.completed
            )
        }
    }
}

