package com.example.lingora_fe.user.classroom.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz

interface ClassroomRepository {

    // ── Classrooms ────────────────────────────────────────────────────────────

    suspend fun createClassroom(
        name: String,
        description: String? = null,
        coverImageUrl: String? = null,
        maxStudents: Int? = null,
        isPublic: Boolean? = null
    ): Either<AppFailure, Classroom>

    suspend fun getAllClassrooms(
        page: Int = 1,
        limit: Int? = null,
        search: String? = null,
        isPublic: Boolean? = null,
        sort: String? = null
    ): Either<AppFailure, ClassroomListResult>

    suspend fun getClassroomById(id: Int): Either<AppFailure, Classroom>

    suspend fun updateClassroom(
        id: Int,
        name: String? = null,
        description: String? = null,
        coverImageUrl: String? = null,
        maxStudents: Int? = null,
        isPublic: Boolean? = null,
        status: String? = null
    ): Either<AppFailure, Classroom>

    suspend fun deleteClassroom(id: Int): Either<AppFailure, Unit>

    // ── Lessons ───────────────────────────────────────────────────────────────

    suspend fun createLesson(
        classroomId: Int,
        title: String,
        description: String? = null,
        lessonType: String? = null,
        content: String? = null,
        sortOrder: Int? = null
    ): Either<AppFailure, ClassroomLesson>

    suspend fun getLessons(classroomId: Int): Either<AppFailure, List<ClassroomLesson>>

    suspend fun getLessonById(classroomId: Int, lessonId: Int): Either<AppFailure, ClassroomLesson>

    suspend fun updateLesson(
        classroomId: Int,
        lessonId: Int,
        title: String? = null,
        description: String? = null,
        lessonType: String? = null,
        content: String? = null,
        sortOrder: Int? = null,
        isPublished: Boolean? = null,
        scheduledAt: String? = null
    ): Either<AppFailure, ClassroomLesson>

    suspend fun deleteLesson(classroomId: Int, lessonId: Int): Either<AppFailure, Unit>

    // ── Quizzes ───────────────────────────────────────────────────────────────

    suspend fun createQuiz(
        classroomId: Int,
        title: String,
        description: String? = null,
        timeLimitSeconds: Int? = null,
        maxAttempts: Int? = null,
        passingScore: Double? = null,
        opensAt: String? = null,
        closesAt: String? = null
    ): Either<AppFailure, ClassroomQuiz>

    suspend fun getQuizzes(classroomId: Int): Either<AppFailure, List<ClassroomQuiz>>

    suspend fun getQuizById(classroomId: Int, quizId: Int): Either<AppFailure, ClassroomQuiz>

    suspend fun deleteQuiz(classroomId: Int, quizId: Int): Either<AppFailure, Unit>

    // ── Chat ──────────────────────────────────────────────────────────────────

    suspend fun getChatHistory(
        classroomId: Int,
        limit: Int? = null,
        beforeId: Int? = null
    ): Either<AppFailure, List<ClassroomMessage>>
}
