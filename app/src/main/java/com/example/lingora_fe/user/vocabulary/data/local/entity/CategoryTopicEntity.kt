package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.CategoryTopic

@Entity(tableName = "category_topics")
data class CategoryTopicEntity(
    @PrimaryKey
    val id: Int,
    val categoryId: Int,
    val topicId: Int,
    val orderIndex: Int
) {
    fun toDomain(): CategoryTopic {
        return CategoryTopic(
            id = id,
            categoryId = categoryId,
            topicId = topicId,
            orderIndex = orderIndex
        )
    }

    companion object {
        fun fromDomain(categoryTopic: CategoryTopic): CategoryTopicEntity {
            return CategoryTopicEntity(
                id = categoryTopic.id,
                categoryId = categoryTopic.categoryId,
                topicId = categoryTopic.topicId,
                orderIndex = categoryTopic.orderIndex
            )
        }
    }
}

