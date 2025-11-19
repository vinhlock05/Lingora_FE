package com.example.lingora_fe.user.forum.domain.model

import java.util.Date

enum class PostTopic {
    GENERAL,
    VOCABULARY,
    GRAMMAR,
    LISTENING,
    SPEAKING,
    READING,
    WRITING
}

enum class PostStatus {
    PUBLISHED,
    ARCHIVED,
    DELETED
}

data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val thumbnails: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val topic: PostTopic? = null,
    val status: PostStatus = PostStatus.PUBLISHED,
    val createdBy: User? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isAlreadyLike: Boolean = false
)

data class User(
    val id: Int,
    val username: String? = null,
    val avatar: String? = null
)