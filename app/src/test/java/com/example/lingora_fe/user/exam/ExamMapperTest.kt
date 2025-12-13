package com.example.lingora_fe.user.exam

import com.example.lingora_fe.user.exam.data.remote.dto.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamMapperTest {
    @Test
    fun mapExamDtoToDomain() {
        val dto = ExamDto(
            id = 1,
            examType = "IELTS",
            code = "IELTS-MOCK-001",
            title = "Mock 1",
            level = "Intermediate",
            isPublished = true,
            metadata = mapOf("readingVariant" to "ACADEMIC"),
            sections = listOf(
                ExamSectionDto(
                    id = 10,
                    sectionType = "LISTENING",
                    title = "Listening",
                    durationSeconds = 2400,
                    instructions = null,
                    audioUrl = "https://cdn/audio.mp3",
                    groups = listOf(
                        ExamSectionGroupDto(
                            id = 100,
                            groupType = "LISTENING_PART",
                            title = "Part 1",
                            description = "Questions 1-10",
                            content = null,
                            resourceUrl = null,
                            questions = listOf(
                                ExamQuestionDto(
                                    id = 1000,
                                    questionType = "SHORT_ANSWER",
                                    prompt = "Write...",
                                    options = null,
                                    correctAnswer = "Cambridge",
                                    explanation = null
                                )
                            )
                        )
                    )
                )
            )
        )
        val domain = dto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("IELTS", domain.examType.value)
        assertEquals(1, domain.sections.size)
        assertEquals(1, domain.sections.first().groups.first().questions.size)
    }
}

