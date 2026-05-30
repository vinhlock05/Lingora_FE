package com.example.lingora_fe.user.classroom.data.remote.api

import com.example.lingora_fe.core.network.ApiResponse
import com.example.lingora_fe.user.classroom.data.remote.dto.AddAttachmentRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomFlashcardDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomLessonAttachmentDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomLessonDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomListMetaData
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomMemberDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomMessageDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomQuizDto
import com.example.lingora_fe.user.classroom.data.remote.dto.ClassroomQuizQuestionDto
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateFlashcardRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizQuestionRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.ImportStudySetRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.JoinClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateAttachmentRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateFlashcardRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateQuizQuestionRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.QuizAttemptWithUserDto
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateQuizRequest
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
        @Query("status") status: String? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("teacherId") teacherId: Int? = null,
        @Query("membership") membership: String? = null,
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

    @POST("classrooms/{id}/lessons/{lessonId}/flashcards")
    suspend fun createFlashcard(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Body request: CreateFlashcardRequest
    ): ApiResponse<ClassroomFlashcardDto>

    @PATCH("classrooms/{id}/lessons/{lessonId}/flashcards/{flashcardId}")
    suspend fun updateFlashcard(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Path("flashcardId") flashcardId: Int,
        @Body request: UpdateFlashcardRequest
    ): ApiResponse<ClassroomFlashcardDto>

    @DELETE("classrooms/{id}/lessons/{lessonId}/flashcards/{flashcardId}")
    suspend fun deleteFlashcard(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Path("flashcardId") flashcardId: Int
    ): ApiResponse<Any>

    @POST("classrooms/{id}/lessons/{lessonId}/import-studyset")
    suspend fun importFlashcardsFromStudySet(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Body request: ImportStudySetRequest
    ): ApiResponse<ClassroomLessonDto>

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

    @POST("classrooms/{id}/quizzes/{quizId}/attempt")
    suspend fun submitQuizAttempt(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Body request: com.example.lingora_fe.user.classroom.data.remote.dto.SubmitQuizAttemptRequest
    ): ApiResponse<com.example.lingora_fe.user.classroom.data.remote.dto.SubmitQuizAttemptResponseDto>

    @GET("classrooms/{id}/quizzes/{quizId}/attempts")
    suspend fun getQuizAttempts(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int
    ): ApiResponse<List<QuizAttemptWithUserDto>>

    @DELETE("classrooms/{id}/quizzes/{quizId}")
    suspend fun deleteQuiz(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int
    ): ApiResponse<Any>

    @PATCH("classrooms/{id}/quizzes/{quizId}")
    suspend fun updateQuiz(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Body request: UpdateQuizRequest
    ): ApiResponse<ClassroomQuizDto>

    @POST("classrooms/{id}/quizzes/{quizId}/questions")
    suspend fun createQuizQuestion(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Body request: CreateQuizQuestionRequest
    ): ApiResponse<ClassroomQuizQuestionDto>

    @PATCH("classrooms/{id}/quizzes/{quizId}/questions/{questionId}")
    suspend fun updateQuizQuestion(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Path("questionId") questionId: Int,
        @Body request: UpdateQuizQuestionRequest
    ): ApiResponse<ClassroomQuizQuestionDto>

    @DELETE("classrooms/{id}/quizzes/{quizId}/questions/{questionId}")
    suspend fun deleteQuizQuestion(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Path("questionId") questionId: Int
    ): ApiResponse<Any>

    @POST("classrooms/{id}/quizzes/{quizId}/import-studyset")
    suspend fun importQuestionsFromStudySet(
        @Path("id") classroomId: Int,
        @Path("quizId") quizId: Int,
        @Body request: ImportStudySetRequest
    ): ApiResponse<ClassroomQuizDto>

    // ── Members ───────────────────────────────────────────────────────────

    @GET("classrooms/{id}/members")
    suspend fun getMembers(
        @Path("id") classroomId: Int,
        @Query("status") status: String? = null
    ): ApiResponse<List<ClassroomMemberDto>>

    @PATCH("classrooms/{id}/members/{memberId}/approve")
    suspend fun approveMember(
        @Path("id") classroomId: Int,
        @Path("memberId") memberId: Int
    ): ApiResponse<ClassroomMemberDto>

    @DELETE("classrooms/{id}/members/{memberId}")
    suspend fun removeMember(
        @Path("id") classroomId: Int,
        @Path("memberId") memberId: Int
    ): ApiResponse<Any>

    // ── Chat ──────────────────────────────────────────────────────────────────

    @GET("classrooms/{id}/messages")
    suspend fun getChatHistory(
        @Path("id") classroomId: Int,
        @Query("limit") limit: Int? = null,
        @Query("beforeId") beforeId: Int? = null
    ): ApiResponse<List<ClassroomMessageDto>>

    // ── Join by Code ───────────────────────────────────────────────────────────

    @POST("classrooms/join")
    suspend fun joinClassroomByCode(
        @Body request: JoinClassroomRequest
    ): ApiResponse<ClassroomDto>

    // ── Lesson Attachments ────────────────────────────────────────────────────

    @POST("classrooms/{id}/lessons/{lessonId}/attachments")
    suspend fun addAttachment(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Body request: AddAttachmentRequest
    ): ApiResponse<ClassroomLessonAttachmentDto>

    @GET("classrooms/{id}/lessons/{lessonId}/attachments")
    suspend fun getAttachments(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int
    ): ApiResponse<List<ClassroomLessonAttachmentDto>>

    @PATCH("classrooms/{id}/lessons/{lessonId}/attachments/{attachmentId}")
    suspend fun updateAttachment(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Path("attachmentId") attachmentId: Int,
        @Body request: UpdateAttachmentRequest
    ): ApiResponse<ClassroomLessonAttachmentDto>

    @DELETE("classrooms/{id}/lessons/{lessonId}/attachments/{attachmentId}")
    suspend fun deleteAttachment(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Path("attachmentId") attachmentId: Int
    ): ApiResponse<Any>

    @POST("classrooms/{id}/lessons/{lessonId}/attachments/transcribe")
    suspend fun transcribeAttachment(
        @Path("id") classroomId: Int,
        @Path("lessonId") lessonId: Int,
        @Body request: TranscribeAttachmentRequest
    ): ApiResponse<TranscribeAttachmentResponse>
}

data class TranscribeAttachmentRequest(
    @com.google.gson.annotations.SerializedName("mediaUrl") val mediaUrl: String
)

data class TranscribeAttachmentResponse(
    @com.google.gson.annotations.SerializedName("subtitles") val subtitles: List<com.example.lingora_fe.user.classroom.presentation.components.SubtitleCue>
)
