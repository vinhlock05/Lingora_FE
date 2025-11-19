package com.example.lingora_fe.user.forum.domain.model

import java.util.Date

data class Comment(
    val id: Int,
    val content: String,
    val createdAt: Date? = null,
    val createdBy: User? = null,
    val parentCommentId: Int? = null,
    val parentCommentOwnerId: Int? = null,
    val likeCount: Int = 0,
    val isAlreadyLike: Boolean = false
)


