package com.example.lingora_fe.user.forum.presentation

import com.example.lingora_fe.user.forum.domain.model.Comment
import com.example.lingora_fe.user.forum.domain.model.Post
import com.example.lingora_fe.user.forum.domain.model.PostStatus
import com.example.lingora_fe.user.forum.domain.model.PostTopic

data class ForumState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<Post> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val selectedTopic: PostTopic? = null, // null means "Tất cả"
    val searchInput: String = "",
    val searchTags: List<String> = emptyList(),
    val appliedTags: List<String> = emptyList(),
    val searchText: String = "",
    val currentUserId: Int? = null,
    val showMyPosts: Boolean = false,
    val myPostsStatus: PostStatus = PostStatus.PUBLISHED
)

data class CreatePostState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val content: String = "",
    val thumbnails: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val selectedTopic: PostTopic? = PostTopic.GENERAL,
    val titleCharCount: Int = 0,
    val contentCharCount: Int = 0,
    val isSuccess: Boolean = false
)

data class PostDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val post: Post? = null,
    val commentThreads: List<CommentThread> = emptyList(),
    val commentText: String = "",
    val commentCharCount: Int = 0,
    val isSubmittingComment: Boolean = false,
    val isLiking: Boolean = false,
    val replyContext: ReplyContext? = null,
    val expandedParentIds: Set<Int> = emptySet(),
    val currentUserId: Int? = null,
    val editingCommentId: Int? = null,
    val editingCommentText: String = "",
    val isUpdatingComment: Boolean = false
)

data class CommentThread(
    val parent: Comment,
    val replies: List<Comment> = emptyList(),
    val areRepliesVisible: Boolean = false
)

data class ReplyContext(
    val parentCommentId: Int,
    val targetUsername: String
)


