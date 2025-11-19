package com.example.lingora_fe.user.forum.data.remote.dto

import com.example.lingora_fe.user.forum.domain.model.Comment
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class CommentDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("createdBy")
    val createdBy: UserDto? = null,
    @SerializedName("parentComment")
    val parentComment: ParentCommentDto? = null,
    @SerializedName("targetId")
    val targetId: Int? = null,
    @SerializedName("targetType")
    val targetType: String? = null,
    @SerializedName("likeCount")
    val likeCount: Int? = null,
    @SerializedName("isAlreadyLike")
    val isAlreadyLike: Boolean? = null
) {
    fun toDomain(): Comment {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        return Comment(
            id = id,
            content = content,
            createdAt = createdAt?.let {
                try {
                    Date(dateFormat.parse(it)?.time ?: 0)
                } catch (e: Exception) {
                    null
                }
            },
            createdBy = createdBy?.toDomain(),
            parentCommentId = parentComment?.id,
            parentCommentOwnerId = parentComment?.createdBy?.id,
            likeCount = likeCount ?: 0,
            isAlreadyLike = isAlreadyLike ?: false
        )
    }
}

data class ParentCommentDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("createdBy")
    val createdBy: UserDto? = null
)

data class CreateCommentRequest(
    @SerializedName("content")
    val content: String,
    @SerializedName("parentId")
    val parentId: Int? = null
)

data class UpdateCommentRequest(
    @SerializedName("content")
    val content: String
)




