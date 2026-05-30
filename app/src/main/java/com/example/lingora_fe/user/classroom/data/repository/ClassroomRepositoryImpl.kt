package com.example.lingora_fe.user.classroom.data.repository

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.core.error.toAppFailure
import com.example.lingora_fe.user.classroom.data.remote.api.ClassroomApiService
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateFlashcardRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizQuestionRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.CreateQuizRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.ImportStudySetRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.JoinClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateClassroomRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateFlashcardRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateLessonRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateQuizQuestionRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.UpdateQuizRequest
import com.example.lingora_fe.user.classroom.data.remote.dto.toDomain
import com.example.lingora_fe.user.classroom.data.remote.dto.toDetailDomain
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
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizAttempt
import com.example.lingora_fe.user.classroom.domain.model.QuizAttemptWithUser
import com.example.lingora_fe.user.classroom.domain.model.SubmitQuizAttemptResult
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.util.DateFormatHelper
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
        isPublic: Boolean?,
        status: String?
    ): Either<AppFailure, Classroom> {
        return Either.catch {
            val request = CreateClassroomRequest(
                name = name,
                description = description,
                coverImageUrl = coverImageUrl,
                maxStudents = maxStudents,
                isPublic = isPublic,
                status = status
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
        status: String?,
        isPublic: Boolean?,
        teacherId: Int?,
        membership: String?,
        sort: String?
    ): Either<AppFailure, ClassroomListResult> {
        return Either.catch {
            val response = apiService.getAllClassrooms(
                page = page,
                limit = limit,
                search = search,
                status = status,
                isPublic = isPublic,
                teacherId = teacherId,
                membership = membership,
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
        sortOrder: Int?,
        isPublished: Boolean?
    ): Either<AppFailure, ClassroomLesson> {
        return Either.catch {
            val request = CreateLessonRequest(
                title = title,
                description = description,
                lessonType = lessonType,
                content = content,
                sortOrder = sortOrder,
                isPublished = isPublished
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

    override suspend fun getLessonDetail(
        classroomId: Int,
        lessonId: Int
    ): Either<AppFailure, ClassroomLessonDetail> {
        return Either.catch {
            val response = apiService.getLessonById(classroomId, lessonId)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDetailDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createFlashcard(
        classroomId: Int,
        lessonId: Int,
        frontText: String,
        backText: String,
        example: String?,
        audioUrl: String?,
        imageUrl: String?
    ): Either<AppFailure, ClassroomFlashcard> {
        return Either.catch {
            val request = CreateFlashcardRequest(
                frontText = frontText,
                backText = backText,
                example = example,
                audioUrl = audioUrl,
                imageUrl = imageUrl
            )
            val response = apiService.createFlashcard(classroomId, lessonId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateFlashcard(
        classroomId: Int,
        lessonId: Int,
        flashcardId: Int,
        frontText: String?,
        backText: String?,
        example: String?,
        audioUrl: String?,
        imageUrl: String?
    ): Either<AppFailure, ClassroomFlashcard> {
        return Either.catch {
            val request = UpdateFlashcardRequest(
                frontText = frontText,
                backText = backText,
                example = example,
                audioUrl = audioUrl,
                imageUrl = imageUrl
            )
            val response = apiService.updateFlashcard(classroomId, lessonId, flashcardId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteFlashcard(
        classroomId: Int,
        lessonId: Int,
        flashcardId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteFlashcard(classroomId, lessonId, flashcardId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun importFlashcardsFromStudySet(
        classroomId: Int,
        lessonId: Int,
        studySetId: Int
    ): Either<AppFailure, ClassroomLessonDetail> {
        return Either.catch {
            val request = ImportStudySetRequest(studySetId = studySetId)
            val response = apiService.importFlashcardsFromStudySet(classroomId, lessonId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDetailDomain()
        }.mapLeft { it.toAppFailure() }
    }

    // ── Quizzes ───────────────────────────────────────────────────────────────

    override suspend fun createQuiz(
        classroomId: Int,
        title: String,
        description: String?,
        lessonId: Int?,
        timeLimitSeconds: Int?,
        maxAttempts: Int?,
        passingScore: Double?,
        isPublished: Boolean?,
        opensAt: String?,
        closesAt: String?
    ): Either<AppFailure, ClassroomQuiz> {
        return Either.catch {
            val request = CreateQuizRequest(
                title = title,
                description = description,
                lessonId = lessonId,
                timeLimitSeconds = timeLimitSeconds,
                maxAttempts = maxAttempts,
                passingScore = passingScore,
                isPublished = isPublished,
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

    override suspend fun getQuizDetail(
        classroomId: Int,
        quizId: Int
    ): Either<AppFailure, ClassroomQuizDetail> {
        return Either.catch {
            val response = apiService.getQuizById(classroomId, quizId)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDetailDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateQuiz(
        classroomId: Int,
        quizId: Int,
        title: String?,
        description: String?,
        lessonId: Int?,
        timeLimitSeconds: Int?,
        maxAttempts: Int?,
        passingScore: Double?,
        isPublished: Boolean?,
        opensAt: String?,
        closesAt: String?
    ): Either<AppFailure, ClassroomQuiz> {
        return Either.catch {
            val request = UpdateQuizRequest(
                title = title,
                description = description,
                lessonId = lessonId,
                timeLimitSeconds = timeLimitSeconds,
                maxAttempts = maxAttempts,
                passingScore = passingScore,
                isPublished = isPublished,
                opensAt = opensAt,
                closesAt = closesAt
            )
            val response = apiService.updateQuiz(classroomId, quizId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun createQuizQuestion(
        classroomId: Int,
        quizId: Int,
        type: String,
        question: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String?
    ): Either<AppFailure, ClassroomQuizQuestion> {
        return Either.catch {
            val request = CreateQuizQuestionRequest(
                type = type,
                question = question,
                options = options,
                correctAnswer = correctAnswer,
                explanation = explanation
            )
            val response = apiService.createQuizQuestion(classroomId, quizId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateQuizQuestion(
        classroomId: Int,
        quizId: Int,
        questionId: Int,
        type: String?,
        question: String?,
        options: List<String>?,
        correctAnswer: String?,
        explanation: String?
    ): Either<AppFailure, ClassroomQuizQuestion> {
        return Either.catch {
            val request = UpdateQuizQuestionRequest(
                type = type,
                question = question,
                options = options,
                correctAnswer = correctAnswer,
                explanation = explanation
            )
            val response = apiService.updateQuizQuestion(classroomId, quizId, questionId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteQuizQuestion(
        classroomId: Int,
        quizId: Int,
        questionId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteQuizQuestion(classroomId, quizId, questionId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun importQuestionsFromStudySet(
        classroomId: Int,
        quizId: Int,
        studySetId: Int
    ): Either<AppFailure, ClassroomQuizDetail> {
        return Either.catch {
            val request = ImportStudySetRequest(studySetId = studySetId)
            val response = apiService.importQuestionsFromStudySet(classroomId, quizId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDetailDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getQuizAttempts(
        classroomId: Int,
        quizId: Int
    ): Either<AppFailure, List<QuizAttemptWithUser>> {
        return Either.catch {
            val response = apiService.getQuizAttempts(classroomId, quizId)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun submitQuizAttempt(
        classroomId: Int,
        quizId: Int,
        answers: Map<String, String>
    ): Either<AppFailure, SubmitQuizAttemptResult> {
        return Either.catch {
            val request = com.example.lingora_fe.user.classroom.data.remote.dto.SubmitQuizAttemptRequest(answers)
            val response = apiService.submitQuizAttempt(classroomId, quizId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            SubmitQuizAttemptResult(
                attempt = dto.attempt.let { att -> 
                    ClassroomQuizAttempt(
                        id = att.id,
                        attemptNumber = att.attemptNumber,
                        score = att.score,
                        correctCount = att.correctCount,
                        answers = att.answers,
                        startedAt = DateFormatHelper.parseDate(att.startedAt),
                        submittedAt = att.submittedAt?.let { DateFormatHelper.parseDate(it) }
                    ) 
                },
                isPassing = dto.isPassing
            )
        }.mapLeft { it.toAppFailure() }
    }

    // ── Members ──────────────────────────────────────────────────────────────

    override suspend fun getMembers(classroomId: Int, status: String?): Either<AppFailure, List<ClassroomMember>> {
        return Either.catch {
            val response = apiService.getMembers(classroomId, status)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun approveMember(classroomId: Int, memberId: Int): Either<AppFailure, ClassroomMember> {
        return Either.catch {
            val response = apiService.approveMember(classroomId, memberId)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun removeMember(classroomId: Int, memberId: Int): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.removeMember(classroomId, memberId)
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

    override suspend fun joinClassroomByCode(code: String): Either<AppFailure, Classroom> {
        return Either.catch {
            val request = JoinClassroomRequest(code = code)
            val response = apiService.joinClassroomByCode(request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    // ── Lesson Attachments ──────────────────────────────────────────────────

    override suspend fun addAttachment(
        classroomId: Int,
        lessonId: Int,
        role: String,
        fileUrl: String,
        fileType: String,
        fileName: String,
        mimeType: String?,
        fileSizeBytes: Long?,
        durationSeconds: Int?,
        title: String?,
        sortOrder: Int?,
        subtitlesJson: String?
    ): Either<AppFailure, com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment> {
        return Either.catch {
            val request = com.example.lingora_fe.user.classroom.data.remote.dto.AddAttachmentRequest(
                role = role,
                fileUrl = fileUrl,
                fileType = fileType,
                fileName = fileName,
                mimeType = mimeType,
                fileSizeBytes = fileSizeBytes,
                durationSeconds = durationSeconds,
                title = title,
                sortOrder = sortOrder,
                subtitlesJson = subtitlesJson
            )
            val response = apiService.addAttachment(classroomId, lessonId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun getAttachments(
        classroomId: Int,
        lessonId: Int
    ): Either<AppFailure, List<com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment>> {
        return Either.catch {
            val response = apiService.getAttachments(classroomId, lessonId)
            val dtos = response.metaData ?: throw Exception(response.message)
            dtos.map { it.toDomain() }
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun updateSubtitles(
        classroomId: Int,
        lessonId: Int,
        attachmentId: Int,
        subtitlesJson: String?
    ): Either<AppFailure, com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment> {
        return Either.catch {
            val request = com.example.lingora_fe.user.classroom.data.remote.dto.UpdateAttachmentRequest(
                subtitlesJson = subtitlesJson
            )
            val response = apiService.updateAttachment(classroomId, lessonId, attachmentId, request)
            val dto = response.metaData ?: throw Exception(response.message)
            dto.toDomain()
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun deleteAttachment(
        classroomId: Int,
        lessonId: Int,
        attachmentId: Int
    ): Either<AppFailure, Unit> {
        return Either.catch {
            apiService.deleteAttachment(classroomId, lessonId, attachmentId)
            Unit
        }.mapLeft { it.toAppFailure() }
    }

    override suspend fun transcribeAttachment(
        classroomId: Int,
        lessonId: Int,
        mediaUrl: String
    ): Either<AppFailure, List<com.example.lingora_fe.user.classroom.presentation.components.SubtitleCue>> {
        return Either.catch {
            val request = com.example.lingora_fe.user.classroom.data.remote.api.TranscribeAttachmentRequest(mediaUrl = mediaUrl)
            val response = apiService.transcribeAttachment(classroomId, lessonId, request)
            val data = response.metaData ?: throw Exception(response.message)
            data.subtitles
        }.mapLeft { it.toAppFailure() }
    }
}
