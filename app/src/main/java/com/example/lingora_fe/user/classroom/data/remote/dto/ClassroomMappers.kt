package com.example.lingora_fe.user.classroom.data.remote.dto

import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonAttachment
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.domain.model.QuizAttemptWithUser
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMemberRole
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
import com.example.lingora_fe.user.classroom.util.LessonAttachmentRole
import com.example.lingora_fe.user.classroom.util.LessonAttachmentType
import com.example.lingora_fe.user.classroom.util.QuizType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun parseDate(value: String?): Date? {
    if (value == null) return null
    return try {
        isoFormat.parse(value)
    } catch (e: Exception) {
        null
    }
}

fun ClassroomUserDto.toDomain(): ClassroomUser = ClassroomUser(
    id = id,
    username = username,
    avatar = avatar
)

fun ClassroomDto.toDomain(): Classroom = Classroom(
    id = id,
    code = code,
    name = name,
    description = description,
    coverImageUrl = coverImageUrl,
    maxStudents = maxStudents,
    status = ClassroomStatus.fromValue(status),
    isPublic = isPublic,
    settings = settings ?: emptyMap(),
    teacher = teacher?.toDomain(),
    totalMembers = totalMembers ?: 0,
    myStatus = myStatus?.let { ClassroomMemberStatus.fromValue(it) },
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt)
)

fun ClassroomListMetaData.toDomain(): ClassroomListResult = ClassroomListResult(
    currentPage = currentPage,
    totalPages = totalPages,
    total = total,
    classrooms = classrooms.map { it.toDomain() }
)

fun ClassroomLessonDto.toDomain(): ClassroomLesson = ClassroomLesson(
    id = id,
    title = title,
    description = description,
    lessonType = ClassroomLessonType.fromValue(lessonType),
    content = content,
    sortOrder = sortOrder ?: 0,
    isPublished = isPublished ?: false,
    scheduledAt = parseDate(scheduledAt),
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt)
)

fun ClassroomMemberDto.toDomain(): ClassroomMember = ClassroomMember(
    id = id,
    user = user.toDomain(),
    role = ClassroomMemberRole.fromValue(role),
    status = ClassroomMemberStatus.fromValue(status),
    joinedAt = parseDate(joinedAt),
    removedAt = parseDate(removedAt)
)

fun ClassroomQuizDto.toDomain(): ClassroomQuiz = ClassroomQuiz(
    id = id,
    title = title,
    description = description,
    timeLimitSeconds = timeLimitSeconds,
    maxAttempts = maxAttempts,
    passingScore = passingScore,
    isPublished = isPublished,
    opensAt = parseDate(opensAt),
    closesAt = parseDate(closesAt),
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt)
)

fun ClassroomMessageDto.toDomain(): ClassroomMessage = ClassroomMessage(
    id = id,
    sender = sender.toDomain(),
    type = ClassroomMessageType.fromValue(type),
    content = content,
    attachmentUrl = attachmentUrl,
    repliedTo = repliedTo?.toDomain(),
    createdAt = parseDate(createdAt)
)

fun ClassroomFlashcardDto.toDomain(): ClassroomFlashcard = ClassroomFlashcard(
    id = id,
    frontText = frontText,
    backText = backText,
    example = example,
    audioUrl = audioUrl,
    imageUrl = imageUrl
)

fun ClassroomQuizQuestionDto.toDomain(): ClassroomQuizQuestion = ClassroomQuizQuestion(
    id = id,
    type = QuizType.fromValue(type),
    question = question,
    options = options,
    correctAnswer = correctAnswer,
    explanation = explanation
)

fun ClassroomLessonDto.toDetailDomain(): ClassroomLessonDetail = ClassroomLessonDetail(
    id = id,
    title = title,
    description = description,
    lessonType = ClassroomLessonType.fromValue(lessonType),
    content = content,
    sortOrder = sortOrder ?: 0,
    isPublished = isPublished ?: false,
    scheduledAt = parseDate(scheduledAt),
    flashcards = flashcards?.map { it.toDomain() } ?: emptyList(),
    attachments = attachments?.map { it.toDomain() } ?: emptyList(),
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt)
)

fun ClassroomLessonAttachmentDto.toDomain(): ClassroomLessonAttachment = ClassroomLessonAttachment(
    id = id,
    role = LessonAttachmentRole.fromValue(role),
    fileUrl = fileUrl,
    fileType = LessonAttachmentType.fromValue(fileType),
    fileName = fileName,
    mimeType = mimeType,
    fileSizeBytes = fileSizeBytes,
    durationSeconds = durationSeconds,
    title = title,
    sortOrder = sortOrder,
    subtitlesJson = subtitlesJson,
    createdAt = parseDate(createdAt)
)

fun ClassroomQuizDto.toDetailDomain(): ClassroomQuizDetail = ClassroomQuizDetail(
    id = id,
    title = title,
    description = description,
    lessonId = lessonId,
    timeLimitSeconds = timeLimitSeconds,
    maxAttempts = maxAttempts,
    passingScore = passingScore,
    isPublished = isPublished,
    opensAt = parseDate(opensAt),
    closesAt = parseDate(closesAt),
    questions = questions?.map { it.toDomain() } ?: emptyList(),
    userAttempts = userAttempts,
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt)
)

fun ClassroomQuizAttemptDto.toDomain(): com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizAttempt = com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizAttempt(
    id = id,
    attemptNumber = attemptNumber,
    score = score,
    correctCount = correctCount,
    answers = answers,
    startedAt = parseDate(startedAt),
    submittedAt = parseDate(submittedAt)
)

fun SubmitQuizAttemptResponseDto.toDomain(): com.example.lingora_fe.user.classroom.domain.model.SubmitQuizAttemptResult = com.example.lingora_fe.user.classroom.domain.model.SubmitQuizAttemptResult(
    attempt = attempt.toDomain(),
    isPassing = isPassing
)

fun QuizAttemptWithUserDto.toDomain(): QuizAttemptWithUser = QuizAttemptWithUser(
    id = id,
    attemptNumber = attemptNumber,
    score = score,
    answers = answers,
    startedAt = parseDate(startedAt),
    submittedAt = parseDate(submittedAt),
    user = user?.toDomain()
)
