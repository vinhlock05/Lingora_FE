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

enum class ClassroomMemberRole(val value: String) {
    STUDENT("STUDENT"),
    ASSISTANT("ASSISTANT"),
    OBSERVER("OBSERVER");

    companion object {
        fun fromValue(value: String): ClassroomMemberRole =
            entries.find { it.value == value } ?: STUDENT
    }
}

enum class ClassroomMemberStatus(val value: String) {
    ACTIVE("ACTIVE"),
    REMOVED("REMOVED"),
    LEFT("LEFT");

    companion object {
        fun fromValue(value: String): ClassroomMemberStatus =
            entries.find { it.value == value } ?: ACTIVE
    }
}

enum class ClassroomLessonType(val value: String) {
    VIDEO("VIDEO"),
    STUDYSET("STUDYSET"),
    TEXT("TEXT"),
    MIXED("MIXED");

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
