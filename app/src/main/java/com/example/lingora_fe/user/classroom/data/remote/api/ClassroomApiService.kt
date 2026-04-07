package com.example.lingora_fe.user.classroom.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomLessonDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomListMetaData
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomMessageDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomQuizDto
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateLessonRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClassroomApiService {

    // ── Classrooms ────────────────────────────────────────────────────────────

    @POST("classrooms")
    suspend fun createClassroom(
        @Body request: CreateClassroomRequest
    ): ApiResponse<ClassroomDto>

    @GET("classrooms")
    suspend fun getAllClassrooms(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<ClassroomListMetaData>

    @GET("classrooms/{id}")
    suspend fun getClassroomById(
        @Path("id") id: Int
    ): ApiResponse<ClassroomDto>

    @PATCH("classrooms/{id}")
    suspend fun updateClassroom(
        @Path("id") id: Int,
        @Body request: UpdateClassroomRequest
    ): ApiResponse<ClassroomDto>

    @DELETE("classrooms/{id}")
    suspend fun deleteClassroom(
        @Path("id") id: Int
    ): ApiResponse<Any>

    // ── Lessons ───────────────────────────────────────────────────────────────

    @POST("classrooms/{id}/lessons")
    suspend fun createLesson(
        @Path("id") classroomId: Int,
        @Body request: CreateLessonRequest
    ): ApiResponse<ClassroomLessonDto>

    @GET("classrooms/{id}/lessons")
    suspend fun getLessons(
        @Path("id") classroomId: Int
    ): ApiResponse<List<ClassroomLessonDto>>

    @GET("classrooms/{id}/lessons/{lessonId}")
    suspend fun getLessonById(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int
    ): ApiResponse<ClassroomLessonDto>

    @PATCH("classrooms/{id}/lessons/{lessonId}")
    suspend fun updateLesson(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Body request: UpdateLessonRequest
    ): ApiResponse<ClassroomLessonDto>

    @DELETE("classrooms/{id}/lessons/{lessonId}")
    suspend fun deleteLesson(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int
    ): ApiResponse<Any>

    // ── Quizzes ───────────────────────────────────────────────────────────────

    @POST("classrooms/{id}/quizzes")
    suspend fun createQuiz(
        @Path("id") classroomId: Int,
        @Body request: CreateQuizRequest
    ): ApiResponse<ClassroomQuizDto>

    @GET("classrooms/{id}/quizzes")
    suspend fun getQuizzes(
        @Path("id") classroomId: Int
    ): ApiResponse<List<ClassroomQuizDto>>

    @GET("classrooms/{id}/quizzes/{quizId}")
    suspend fun getQuizById(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int
    ): ApiResponse<ClassroomQuizDto>

    @DELETE("classrooms/{id}/quizzes/{quizId}")
    suspend fun deleteQuiz(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int
    ): ApiResponse<Any>

    // ── Chat ──────────────────────────────────────────────────────────────────

    @GET("classrooms/{id}/messages")
    suspend fun getChatHistory(
        @Path("id") classroomId: Int,
        @Query("limit") limit: Int? = null,
        @Query("beforeId") beforeId: Int? = null
    ): ApiResponse<List<ClassroomMessageDto>>
}
