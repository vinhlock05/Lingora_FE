package com.example.lingora_fe.user.forum.data.remote.dto

import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class PostDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("thumbnails")
    val thumbnails: List<String>? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("topic")
    val topic: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("createdBy")
    val createdBy: UserDto? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("likeCount")
    val likeCount: Int? = null,
    @SerializedName("commentCount")
    val commentCount: Int? = null,
    @SerializedName("isAlreadyLike")
    val isAlreadyLike: Boolean? = null
) {
    fun toDomain(): Post {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        return Post(
            id = id,
            title = title,
            content = content,
            thumbnails = thumbnails ?: emptyList(),
            tags = tags ?: emptyList(),
            topic = topic?.let { PostTopic.valueOf(it.uppercase()) },
            status = status?.let { PostStatus.valueOf(it.uppercase()) } ?: PostStatus.PUBLISHED,
            createdBy = createdBy?.toDomain(),
            createdAt = createdAt?.let { 
                try {
                    Date(dateFormat.parse(it)?.time ?: 0)
                } catch (e: Exception) {
                    null
                }
            },
            updatedAt = updatedAt?.let {
                try {
                    Date(dateFormat.parse(it)?.time ?: 0)
                } catch (e: Exception) {
                    null
                }
            },
            likeCount = likeCount ?: 0,
            commentCount = commentCount ?: 0,
            isAlreadyLike = isAlreadyLike ?: false
        )
    }
}

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null
) {
    fun toDomain(): com.example.lingora_fe.user.forum.domain.model.User {
        return com.example.lingora_fe.user.forum.domain.model.User(
            id = id,
            username = username,
            avatar = avatar
        )
    }
}

data class PostListMetaData(
    @SerializedName("currentPage")
    val currentPage: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("posts")
    val posts: List<PostDto>
)

data class CreatePostRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("thumbnails")
    val thumbnails: List<String>? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("topic")
    val topic: String? = null
)

data class UpdatePostRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("thumbnails")
    val thumbnails: List<String>? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("topic")
    val topic: String? = null,
    @SerializedName("status")
    val status: String? = null
)


