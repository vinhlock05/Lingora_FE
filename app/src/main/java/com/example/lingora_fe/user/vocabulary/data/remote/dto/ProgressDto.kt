package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.UserCategoryProgress
import com.example.lingora_fe.user.vocabulary.domain.model.UserTopicProgress
import com.google.gson.annotations.SerializedName

data class UserCategoryProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("total_topics")
    val totalTopics: Int,
    @SerializedName("progress_percent")
    val progressPercent: Float,
    @SerializedName("completed")
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
}

data class UserTopicProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("total_words")
    val totalWords: Int,
    @SerializedName("completed")
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
}

data class CategoryProgressResponse(
    @SerializedName("data")
    val data: UserCategoryProgressDto
)

data class CategoryProgressListResponse(
    @SerializedName("data")
    val data: List<UserCategoryProgressDto>
)

data class TopicProgressResponse(
    @SerializedName("data")
    val data: UserTopicProgressDto
)

data class UpdateProgressRequest(
    @SerializedName("completed")
    val completed: Boolean? = null,
    @SerializedName("progress_percent")
    val progressPercent: Float? = null
)

