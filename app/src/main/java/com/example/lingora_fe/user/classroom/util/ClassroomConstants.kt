package com.example.lingora_fe.user.classroom.util

enum class ClassroomStatus(val value: String) {
    DRAFT("DRAFT"),
    ACTIVE("ACTIVE"),
    ARCHIVED("ARCHIVED");

    companion object {
        fun fromValue(value: String): ClassroomStatus =
            entries.find { it.value == value } ?: DRAFT
    }
}

enum class ClassroomMemberRole(val value: String, val displayName: String) {
    STUDENT("STUDENT", "Học sinh"),
    ASSISTANT("ASSISTANT", "Trợ giáo"),
    OBSERVER("OBSERVER", "Quan sát viên");

    companion object {
        fun fromValue(value: String): ClassroomMemberRole =
            entries.find { it.value == value } ?: STUDENT
    }
}

enum class ClassroomMemberStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Chờ duyệt"),
    ACTIVE("ACTIVE", "Hoạt động"),
    REMOVED("REMOVED", "Bị xóa"),
    LEFT("LEFT", "Đã rời");

    companion object {
        fun fromValue(value: String): ClassroomMemberStatus =
            entries.find { it.value == value } ?: ACTIVE
    }
}

enum class ClassroomLessonType(val value: String, val displayName: String) {
    VIDEO("VIDEO", "Video"),
    STUDYSET("STUDYSET", "Study Set"),
    TEXT("TEXT", "Văn bản"),
    MIXED("MIXED", "Hỗn hợp");

    companion object {
        fun fromValue(value: String): ClassroomLessonType =
            entries.find { it.value == value } ?: MIXED
    }
}

enum class ClassroomMessageType(val value: String) {
    TEXT("TEXT"),
    IMAGE("IMAGE"),
    FILE("FILE");

    companion object {
        fun fromValue(value: String): ClassroomMessageType =
            entries.find { it.value == value } ?: TEXT
    }
}

enum class QuizType(val value: String) {
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    TRUE_FALSE("TRUE_FALSE"),
    SHORT_ANSWER("SHORT_ANSWER");

    companion object {
        fun fromValue(value: String): QuizType =
            entries.find { it.value == value } ?: MULTIPLE_CHOICE
    }
}
