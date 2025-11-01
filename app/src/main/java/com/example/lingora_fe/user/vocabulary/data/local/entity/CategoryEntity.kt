package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.Category
import java.sql.Timestamp

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: Long
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description,
            createdAt = Timestamp(createdAt)
        )
    }

    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id,
                name = category.name,
                description = category.description,
                createdAt = category.createdAt.time
            )
        }
    }
}

