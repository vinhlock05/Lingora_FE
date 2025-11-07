package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.UserCategoryProgress
import com.example.lingora_fe.user.vocabulary.domain.model.UserTopicProgress
import com.google.gson.annotations.SerializedName

data class UserCategoryProgressDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("totalTopics")
    val totalTopics: Int,
    @SerializedName("progressPercent")
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
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("totalWords")
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

data class UserCategoryProgressListResponse(
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
    @SerializedName("progressPercent")
    val progressPercent: Float? = null
)

