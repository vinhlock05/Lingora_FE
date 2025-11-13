package com.example.lingora_fe.core.domain.model

enum class ProficiencyLevel(val value: String) {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED");
    
    companion object {
        fun fromString(value: String?): ProficiencyLevel? {
            return when (value?.uppercase()) {
                "BEGINNER" -> BEGINNER
                "INTERMEDIATE" -> INTERMEDIATE
                "ADVANCED" -> ADVANCED
                else -> null
            }
        }
    }
}

