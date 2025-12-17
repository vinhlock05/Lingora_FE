package com.example.lingora_fe.user.forum.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.forum.domain.model.Comment
import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.forum.domain.model.PostTopic

interface ForumRepository {
    suspend fun createPost(
        title: String,
        content: String,
        thumbnails: List<String>?,
        tags: List<String>?,
        topic: PostTopic?
    ): Either<AppFailure, Post>
    
    suspend fun getAllPosts(
        page: Int = 1,
        limit: Int? = null,
        sort: String? = null,
        search: String? = null,
        ownerId: Int? = null,
        topic: PostTopic? = null,
        tags: List<String>? = null,
        status: String? = null
    ): Either<AppFailure, PostListResult>
    
    suspend fun getPostById(id: Int): Either<AppFailure, Post>
    
    suspend fun updatePost(
        id: Int,
        title: String? = null,
        content: String? = null,
        thumbnails: List<String>? = null,
        tags: List<String>? = null,
        topic: PostTopic? = null,
        status: String? = null
    ): Either<AppFailure, Post>
    
    suspend fun deletePost(id: Int): Either<AppFailure, Unit>
    
    suspend fun like(targetId: Int, targetType: String): Either<AppFailure, Unit>
    
    suspend fun unlike(targetId: Int, targetType: String): Either<AppFailure, Unit>
    
    suspend fun getChildComments(
        targetId: Int,
        parentId: Int?,
        targetType: String = "POST"
    ): Either<AppFailure, List<Comment>>
    
    suspend fun getCommentById(
        commentId: Int
    ): Either<AppFailure, Comment>
    
    suspend fun createComment(
        targetId: Int,
        content: String,
        parentId: Int? = null,
        targetType: String = "POST"
    ): Either<AppFailure, Comment>
    
    suspend fun updateComment(
        commentId: Int,
        targetId: Int,
        content: String,
        targetType: String = "POST"
    ): Either<AppFailure, Comment>
    
    suspend fun deleteComment(commentId: Int): Either<AppFailure, Unit>
}

data class PostListResult(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val posts: List<Post>
)



