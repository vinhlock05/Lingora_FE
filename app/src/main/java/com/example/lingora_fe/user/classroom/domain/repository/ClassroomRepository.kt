package com.example.lingora_fe.user.classroom.domain.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.domain.model.QuizAttemptWithUser
import com.example.lingora_fe.user.classroom.domain.model.SubmitQuizAttemptResult

interface ClassroomRepository {

    // ── Classrooms ────────────────────────────────────────────────────────────

    suspend fun createClassroom(
        name: String,
        description: String? = null,
        coverImageUrl: String? = null,
        maxStudents: Int? = null,
        isPublic: Boolean? = null,
        status: String? = null
    ): Either<AppFailure, Classroom>

    suspend fun getAllClassrooms(
        page: Int = 1,
        limit: Int? = null,
        search: String? = null,
        status: String? = null,
        isPublic: Boolean? = null,
        teacherId: Int? = null,
        membership: String? = null,
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
        sortOrder: Int? = null,
        isPublished: Boolean? = null
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

    suspend fun getLessonDetail(classroomId: Int, lessonId: Int): Either<AppFailure, ClassroomLessonDetail>

    suspend fun createFlashcard(
        classroomId: Int,
        lessonId: Int,
        frontText: String,
        backText: String,
        example: String? = null,
        audioUrl: String? = null,
        imageUrl: String? = null
    ): Either<AppFailure, ClassroomFlashcard>

    suspend fun updateFlashcard(
        classroomId: Int,
        lessonId: Int,
        flashcardId: Int,
        frontText: String? = null,
        backText: String? = null,
        example: String? = null,
        audioUrl: String? = null,
        imageUrl: String? = null
    ): Either<AppFailure, ClassroomFlashcard>

    suspend fun deleteFlashcard(
        classroomId: Int,
        lessonId: Int,
        flashcardId: Int
    ): Either<AppFailure, Unit>

    suspend fun importFlashcardsFromStudySet(
        classroomId: Int,
        lessonId: Int,
        studySetId: Int
    ): Either<AppFailure, ClassroomLessonDetail>

    // ── Quizzes ───────────────────────────────────────────────────────────────

    suspend fun createQuiz(
        classroomId: Int,
        title: String,
        description: String? = null,
        lessonId: Int? = null,
        timeLimitSeconds: Int? = null,
        maxAttempts: Int? = null,
        passingScore: Double? = null,
        isPublished: Boolean? = null,
        opensAt: String? = null,
        closesAt: String? = null
    ): Either<AppFailure, ClassroomQuiz>

    suspend fun getQuizzes(classroomId: Int): Either<AppFailure, List<ClassroomQuiz>>

    suspend fun getQuizById(classroomId: Int, quizId: Int): Either<AppFailure, ClassroomQuiz>

    suspend fun getQuizDetail(classroomId: Int, quizId: Int): Either<AppFailure, ClassroomQuizDetail>

    suspend fun updateQuiz(
        classroomId: Int,
        quizId: Int,
        title: String? = null,
        description: String? = null,
        lessonId: Int? = null,
        timeLimitSeconds: Int? = null,
        maxAttempts: Int? = null,
        passingScore: Double? = null,
        isPublished: Boolean? = null,
        opensAt: String? = null,
        closesAt: String? = null
    ): Either<AppFailure, ClassroomQuiz>

    suspend fun deleteQuiz(classroomId: Int, quizId: Int): Either<AppFailure, Unit>

    suspend fun createQuizQuestion(
        classroomId: Int,
        quizId: Int,
        type: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String? = null
    ): Either<AppFailure, ClassroomQuizQuestion>

    suspend fun updateQuizQuestion(
        classroomId: Int,
        quizId: Int,
        questionId: Int,
        type: String? = null,
        question: String? = null,
        options: List<String>? = null,
        correctAnswer: String? = null,
        explanation: String? = null
    ): Either<AppFailure, ClassroomQuizQuestion>

    suspend fun deleteQuizQuestion(
        classroomId: Int,
        quizId: Int,
        questionId: Int
    ): Either<AppFailure, Unit>

    suspend fun importQuestionsFromStudySet(
        classroomId: Int,
        quizId: Int,
        studySetId: Int
    ): Either<AppFailure, ClassroomQuizDetail>

    suspend fun getQuizAttempts(
        classroomId: Int,
        quizId: Int
    ): Either<AppFailure, List<QuizAttemptWithUser>>

    suspend fun submitQuizAttempt(
        classroomId: Int,
        quizId: Int,
        answers: Map<String, String>
    ): Either<AppFailure, SubmitQuizAttemptResult>

    // ── Members ───────────────────────────────────────────────────────────────

    suspend fun getMembers(classroomId: Int, status: String? = null): Either<AppFailure, List<ClassroomMember>>

    suspend fun approveMember(classroomId: Int, memberId: Int): Either<AppFailure, ClassroomMember>

    suspend fun removeMember(classroomId: Int, memberId: Int): Either<AppFailure, Unit>

    // ── Chat ──────────────────────────────────────────────────────────────────

    suspend fun getChatHistory(
        classroomId: Int,
        limit: Int? = null,
        beforeId: Int? = null
    ): Either<AppFailure, List<ClassroomMessage>>

    // ── Join by Code ──────────────────────────────────────────────────────────

    suspend fun joinClassroomByCode(code: String): Either<AppFailure, Classroom>

    // ── Lesson Attachments ────────────────────────────────────────────────────

    suspend fun addAttachment(
        classroomId: Int,
        lessonId: Int,
        role: String,
        fileUrl: String,
        fileType: String,
        fileName: String,
        mimeType: String? = null,
        fileSizeBytes: Long? = null,
        durationSeconds: Int? = null,
        title: String? = null,
        sortOrder: Int? = null
    ): Either<AppFailure, com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment>

    suspend fun getAttachments(
        classroomId: Int,
        lessonId: Int
    ): Either<AppFailure, List<com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment>>

    suspend fun deleteAttachment(
        classroomId: Int,
        lessonId: Int,
        attachmentId: Int
    ): Either<AppFailure, Unit>
}
