package com.example.lingora_fe.user.classroom.data.remote.dto

import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMemberRole
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
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
