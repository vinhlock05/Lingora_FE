package com.example.lingora_fe.user.forum.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.forum.data.remote.api.ForumApiService
import com.example.lingora_fe.user.forum.data.remote.dto.*
import com.example.lingora_fe.user.forum.domain.model.PostTopic
import com.example.lingora_fe.user.forum.domain.repository.ForumRepository
import com.example.lingora_fe.user.forum.domain.repository.PostListResult
import javax.inject.Inject

class ForumRepositoryImpl @Inject constructor(
    private val apiService: ForumApiService
) : ForumRepository {
    
    override suspend fun createPost(
        title: String,
        content: String,
        thumbnails: List<String>?,
        tags: List<String>?,
        topic: PostTopic?
    ): Either<AppFailure, com.example.lingora_fe.user.forum.domain.model.Post> {
        return Either.catch {
            val request = CreatePostRequest(
                title = title,
                content = content,
                thumbnails = thumbnails,
                tags = tags,
                topic = topic?.name?.lowercase()
            )
            val response = apiService.createPost(request)
            val postDto = response.metaData ?: throw Exception(response.message)
            postDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getAllPosts(
        page: Int,
        limit: Int?,
        sort: String?,
        search: String?,
        ownerId: Int?,
        topic: PostTopic?,
        tags: List<String>?,
        status: String?
    ): Either<AppFailure, PostListResult> {
        return Either.catch {
            val response = apiService.getAllPosts(
                page = page,
                limit = limit,
                sort = sort,
                search = search,
                ownerId = ownerId,
                topic = topic?.name?.lowercase(),
                tags = tags,
                status = status
            )
            val metaData = response.metaData ?: throw Exception(response.message)
            PostListResult(
                currentPage = metaData.currentPage,
                totalPages = metaData.totalPages,
                total = metaData.total,
                posts = metaData.posts.map { it.toDomain() }
            )
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getPostById(id: Int): Either<AppFailure, com.example.lingora_fe.user.forum.domain.model.Post> {
        return Either.catch {
            val response = apiService.getPostById(id)
            val postDto = response.metaData ?: throw Exception(response.message)
            postDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun updatePost(
        id: Int,
        title: String?,
        content: String?,
        thumbnails: List<String>?,
        tags: List<String>?,
        topic: PostTopic?,
        status: String?
    ): Either<AppFailure, com.example.lingora_fe.user.forum.domain.model.Post> {
        return Either.catch {
            val request = UpdatePostRequest(
                title = title,
                content = content,
                thumbnails = thumbnails,
                tags = tags,
                topic = topic?.name?.lowercase(),
                status = status
            )
            val response = apiService.updatePost(id, request)
            val postDto = response.metaData ?: throw Exception(response.message)
            postDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun deletePost(id: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deletePost(id)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun like(targetId: Int, targetType: String): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.like(targetId, targetType)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun unlike(targetId: Int, targetType: String): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.unlike(targetId, targetType)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun getChildComments(
        targetId: Int,
        parentId: Int?,
        targetType: String
    ): Either<AppFailure, List<com.example.lingora_fe.user.forum.domain.model.Comment>> {
        return Either.catch {
            val parentIdStr = parentId?.toString() ?: "null"
            val response = apiService.getChildComments(targetId, parentIdStr, targetType)
            val comments = response.metaData ?: throw Exception(response.message)
            comments.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun createComment(
        targetId: Int,
        content: String,
        parentId: Int?,
        targetType: String
    ): Either<AppFailure, com.example.lingora_fe.user.forum.domain.model.Comment> {
        return Either.catch {
            val request = CreateCommentRequest(content = content, parentId = parentId)
            val response = apiService.createComment(targetId, targetType, request)
            val commentDto = response.metaData ?: throw Exception(response.message)
            commentDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun updateComment(
        commentId: Int,
        targetId: Int,
        content: String,
        targetType: String
    ): Either<AppFailure, com.example.lingora_fe.user.forum.domain.model.Comment> {
        return Either.catch {
            val request = UpdateCommentRequest(content = content)
            val response = apiService.updateComment(commentId, targetId, targetType, request)
            val commentDto = response.metaData ?: throw Exception(response.message)
            commentDto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }
    
    override suspend fun deleteComment(commentId: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteComment(commentId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }
}


