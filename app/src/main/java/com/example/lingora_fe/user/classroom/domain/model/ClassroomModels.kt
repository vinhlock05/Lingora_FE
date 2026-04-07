package com.example.lingora_fe.user.classroom.domain.model

import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMemberRole
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
import com.example.lingora_fe.user.classroom.util.QuizType
import java.util.Date

data class ClassroomUser(
    val id: Int,
    val username: String?,
    val avatar: String?
)

data class Classroom(
    val id: Int,
    val code: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val maxStudents: Int,
    val status: ClassroomStatus,
    val isPublic: Boolean,
    val settings: Map<String, Any>,
    val teacher: ClassroomUser?,
    val totalMembers: Int,
    val createdAt: Date?,
    val updatedAt: Date?
)

data class ClassroomLesson(
    val id: Int,
    val title: String,
    val description: String?,
    val lessonType: ClassroomLessonType,
    val content: String?,
    val sortOrder: Int,
    val isPublished: Boolean,
    val scheduledAt: Date?,
    val createdAt: Date?,
    val updatedAt: Date?
)

data class ClassroomMember(
    val id: Int,
    val user: ClassroomUser,
    val role: ClassroomMemberRole,
    val status: ClassroomMemberStatus,
    val joinedAt: Date?,
    val removedAt: Date?
)

data class ClassroomQuiz(
    val id: Int,
    val title: String,
    val description: String?,
    val timeLimitSeconds: Int?,
    val maxAttempts: Int,
    val passingScore: Double,
    val isPublished: Boolean,
    val opensAt: Date?,
    val closesAt: Date?,
    val createdAt: Date?,
    val updatedAt: Date?
)

data class ClassroomMessage(
    val id: Int,
    val sender: ClassroomUser,
    val type: ClassroomMessageType,
    val content: String,
    val attachmentUrl: String?,
    val repliedTo: ClassroomMessage?,
    val createdAt: Date?
)

data class ClassroomListResult(
    val currentPage: Int,
    val totalPages: Int,
    val total: Int,
    val classrooms: List<Classroom>
)

data class ClassroomFlashcard(
    val id: Int,
    val frontText: String,
    val backText: String,
    val example: String?,
    val audioUrl: String?,
    val imageUrl: String?
)

data class ClassroomQuizQuestion(
    val id: Int,
    val type: QuizType,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String?
)

data class ClassroomLessonDetail(
    val id: Int,
    val title: String,
    val description: String?,
    val lessonType: ClassroomLessonType,
    val content: String?,
    val sortOrder: Int,
    val isPublished: Boolean,
    val scheduledAt: Date?,
    val flashcards: List<ClassroomFlashcard>,
    val createdAt: Date?,
    val updatedAt: Date?
)

data class ClassroomQuizDetail(
    val id: Int,
    val title: String,
    val description: String?,
    val lessonId: Int?,
    val timeLimitSeconds: Int?,
    val maxAttempts: Int,
    val passingScore: Double,
    val isPublished: Boolean,
    val opensAt: Date?,
    val closesAt: Date?,
    val questions: List<ClassroomQuizQuestion>,
    val createdAt: Date?,
    val updatedAt: Date?
)
