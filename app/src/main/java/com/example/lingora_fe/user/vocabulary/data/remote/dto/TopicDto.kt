package com.example.lingora_fe.user.vocabulary.data.remote.dto

import com.example.lingora_fe.user.vocabulary.domain.model.Topic
import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class TopicDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    fun toDomain(): Topic {
        return Topic(
            id = id,
            name = name,
            description = description,
            createdAt = Timestamp.valueOf(createdAt)
        )
    }
}

data class TopicResponse(
    @SerializedName("data")
    val data: TopicDto
)

data class TopicsResponse(
    @SerializedName("data")
    val data: List<TopicDto>
)

