package com.example.lingora_fe.user.forum.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.forum.data.remote.dto.*
import retrofit2.http.*

interface ForumApiService {
    
    // Posts
    @POST("posts")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): ApiResponse<PostDto>
    
    @GET("posts")
    suspend fun getAllPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("search") search: String? = null,
        @Query("ownerId") ownerId: Int? = null,
        @Query("topic") topic: String? = null,
        @Query("tags") tags: List<String>? = null,
        @Query("status") status: String? = null
    ): ApiResponse<PostListMetaData>
    
    @GET("posts/{id}")
    suspend fun getPostById(
        @Path("id") id: Int
    ): ApiResponse<PostDto>
    
    @PATCH("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body request: UpdatePostRequest
    ): ApiResponse<PostDto>
    
    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Path("id") id: Int
    ): ApiResponse<Any>
    
    // Likes
    @POST("likes/{targetId}")
    suspend fun like(
        @Path("targetId") targetId: Int,
        @Query("targetType") targetType: String
    ): ApiResponse<LikeDto>
    
    @DELETE("likes/{targetId}")
    suspend fun unlike(
        @Path("targetId") targetId: Int,
        @Query("targetType") targetType: String
    ): ApiResponse<Any>
    
    // Comments
    @GET("comments/target/{targetId}/parent/{parentId}")
    suspend fun getChildComments(
        @Path("targetId") targetId: Int,
        @Path("parentId") parentId: String, // Can be "null" or number
        @Query("targetType") targetType: String? = "POST"
    ): ApiResponse<List<CommentDto>>
    
    @POST("comments/target/{targetId}")
    suspend fun createComment(
        @Path("targetId") targetId: Int,
        @Query("targetType") targetType: String? = "POST",
        @Body request: CreateCommentRequest
    ): ApiResponse<CommentDto>
    
    @PATCH("comments/{commentId}/target/{targetId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Int,
        @Path("targetId") targetId: Int,
        @Query("targetType") targetType: String? = "POST",
        @Body request: UpdateCommentRequest
    ): ApiResponse<CommentDto>
    
    @DELETE("comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Int
    ): ApiResponse<Any>
}


