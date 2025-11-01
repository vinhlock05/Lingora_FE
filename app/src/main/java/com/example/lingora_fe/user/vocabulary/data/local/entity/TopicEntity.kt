package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.Topic
import java.sql.Timestamp

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: Long
) {
    fun toDomain(): Topic {
        return Topic(
            id = id,
            name = name,
            description = description,
            createdAt = Timestamp(createdAt)
        )
    }

    companion object {
        fun fromDomain(topic: Topic): TopicEntity {
            return TopicEntity(
                id = topic.id,
                name = topic.name,
                description = topic.description,
                createdAt = topic.createdAt.time
            )
        }
    }
}

