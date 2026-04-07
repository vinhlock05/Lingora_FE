package com.example.lingora_fe.user.classroom.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.classroom.data.remote.api.ClassroomApiService
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.toDomain
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import javax.inject.Inject

class ClassroomRepositoryImpl @Inject constructor(
    private val apiService: ClassroomApiService
) : ClassroomRepository {

    // ── Classrooms ────────────────────────────────────────────────────────────

    override suspend fun createClassroom(
        name: String,
        description: String?,
        coverImageUrl: String?,
        maxStudents: Int?,
        isPublic: Boolean?
    ): Either<AppFailure, Classroom> {
        return Either.catch {
            val request = CreateClassroomRequest(
                name = name,
                description = description,
                coverImageUrl = coverImageUrl,
                maxStudents = maxStudents,
                isPublic = isPublic
            )
            val response = apiService.createClassroom(request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAllClassrooms(
        page: Int,
        limit: Int?,
        search: String?,
        isPublic: Boolean?,
        sort: String?
    ): Either<AppFailure, ClassroomListResult> {
        return Either.catch {
            val response = apiService.getAllClassrooms(
                page = page,
                limit = limit,
                search = search,
                isPublic = isPublic,
                sort = sort
            )
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getClassroomById(id: Int): Either<AppFailure, Classroom> {
        return Either.catch {
            val response = apiService.getClassroomById(id)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateClassroom(
        id: Int,
        name: String?,
        description: String?,
        coverImageUrl: String?,
        maxStudents: Int?,
        isPublic: Boolean?,
        status: String?
    ): Either<AppFailure, Classroom> {
        return Either.catch {
            val request = UpdateClassroomRequest(
                name = name,
                description = description,
                coverImageUrl = coverImageUrl,
                maxStudents = maxStudents,
                isPublic = isPublic,
                status = status
            )
            val response = apiService.updateClassroom(id, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteClassroom(id: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteClassroom(id)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    // ── Lessons ───────────────────────────────────────────────────────────────

    override suspend fun createLesson(
        classroomId: Int,
        title: String,
        description: String?,
        lessonType: String?,
        content: String?,
        sortOrder: Int?
    ): Either<AppFailure, ClassroomLesson> {
        return Either.catch {
            val request = CreateLessonRequest(
                title = title,
                description = description,
                lessonType = lessonType,
                content = content,
                sortOrder = sortOrder
            )
            val response = apiService.createLesson(classroomId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getLessons(classroomId: Int): Either<AppFailure, List<ClassroomLesson>> {
        return Either.catch {
            val response = apiService.getLessons(classroomId)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getLessonById(
        classroomId: Int,
        lessonId: Int
    ): Either<AppFailure, ClassroomLesson> {
        return Either.catch {
            val response = apiService.getLessonById(classroomId, lessonId)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateLesson(
        classroomId: Int,
        lessonId: Int,
        title: String?,
        description: String?,
        lessonType: String?,
        content: String?,
        sortOrder: Int?,
        isPublished: Boolean?,
        scheduledAt: String?
    ): Either<AppFailure, ClassroomLesson> {
        return Either.catch {
            val request = UpdateLessonRequest(
                title = title,
                description = description,
                lessonType = lessonType,
                content = content,
                sortOrder = sortOrder,
                isPublished = isPublished,
                scheduledAt = scheduledAt
            )
            val response = apiService.updateLesson(classroomId, lessonId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteLesson(classroomId: Int, lessonId: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteLesson(classroomId, lessonId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    // ── Quizzes ───────────────────────────────────────────────────────────────

    override suspend fun createQuiz(
        classroomId: Int,
        title: String,
        description: String?,
        timeLimitSeconds: Int?,
        maxAttempts: Int?,
        passingScore: Double?,
        opensAt: String?,
        closesAt: String?
    ): Either<AppFailure, ClassroomQuiz> {
        return Either.catch {
            val request = CreateQuizRequest(
                title = title,
                description = description,
                timeLimitSeconds = timeLimitSeconds,
                maxAttempts = maxAttempts,
                passingScore = passingScore,
                opensAt = opensAt,
                closesAt = closesAt
            )
            val response = apiService.createQuiz(classroomId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getQuizzes(classroomId: Int): Either<AppFailure, List<ClassroomQuiz>> {
        return Either.catch {
            val response = apiService.getQuizzes(classroomId)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getQuizById(
        classroomId: Int,
        quizId: Int
    ): Either<AppFailure, ClassroomQuiz> {
        return Either.catch {
            val response = apiService.getQuizById(classroomId, quizId)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteQuiz(classroomId: Int, quizId: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteQuiz(classroomId, quizId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    override suspend fun getChatHistory(
        classroomId: Int,
        limit: Int?,
        beforeId: Int?
    ): Either<AppFailure, List<ClassroomMessage>> {
        return Either.catch {
            val response = apiService.getChatHistory(classroomId, limit, beforeId)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }
}
