package com.example.lingora_fe.user.classroom.presentation

import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz

data class ClassroomListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val classrooms: List<Classroom> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val searchQuery: String = "",
    val selectedTab: Int = 0  // 0: Khám phá (public), 1: Của tôi (all)
)

data class ClassroomDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val classroom: Classroom? = null,
    val lessons: List<ClassroomLesson> = emptyList(),
    val quizzes: List<ClassroomQuiz> = emptyList(),
    val chatMessages: List<ClassroomMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val chatInput: String = "",
    val isSendingMessage: Boolean = false,
    val selectedTab: Int = 0  // 0: Bài học, 1: Bài kiểm tra, 2: Thảo luận
)

data class CreateClassroomState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val maxStudents: Int? = null,
    val isSuccess: Boolean = false,
    val createdClassroomId: Int? = null
)
