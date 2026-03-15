package com.example.lingora_fe.user.classroom.domain.model

data class Classroom(
    val id: String,
    val name: String,
    val code: String,       // Added from ERD
    val creatorName: String,
    val studentCount: Int,
    val maxStudents: Int,   // Added from ERD
    val description: String,
    val coverImageUrl: String? = null,
    val status: String,     // Added from ERD (e.g., ACTIVE, CLOSED)
    val isPublic: Boolean   // Added from ERD
)

data class ClassroomLesson(
    val id: String,
    val title: String,
    val description: String,
    val isLocked: Boolean,
    val isCompleted: Boolean,
    val isPublished: Boolean, // Added from ERD
    val type: String // "VIDEO", "QUIZ", "READING"
)

data class ClassroomMember(
    val id: String,
    val name: String,
    val role: String, // "TEACHER", "STUDENT"
    val avatarUrl: String? = null,
    val joinedAt: String? = null // Added from ERD
)

data class ClassroomDiscussionPost(
    val id: String,
    val authorName: String,
    val content: String,
    val messageType: String, // Added from ERD (TEXT, FILE, etc)
    val attachmentUrl: String? = null, // Added from ERD
    val timeAgo: String,
    val replyCount: Int,
    val avatarUrl: String? = null
)
