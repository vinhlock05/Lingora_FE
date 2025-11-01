package com.example.lingora_fe.user.vocabulary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lingora_fe.user.vocabulary.domain.model.UserTopicProgress

@Entity(tableName = "user_topic_progress")
data class UserTopicProgressEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val topicId: Int,
    val totalWords: Int,
    val completed: Boolean
) {
    fun toDomain(): UserTopicProgress {
        return UserTopicProgress(
            id = id,
            userId = userId,
            topicId = topicId,
            totalWords = totalWords,
            completed = completed
        )
    }

    companion object {
        fun fromDomain(progress: UserTopicProgress): UserTopicProgressEntity {
            return UserTopicProgressEntity(
                id = progress.id,
                userId = progress.userId,
                topicId = progress.topicId,
                totalWords = progress.totalWords,
                completed = progress.completed
            )
        }
    }
}

