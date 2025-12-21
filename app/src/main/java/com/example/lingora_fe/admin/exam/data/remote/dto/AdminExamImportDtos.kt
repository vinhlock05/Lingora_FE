package com.example.lingora_fe.admin.exam.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// ==================== Enums ====================

@Keep
enum class ExamType {
    @SerializedName("IELTS") IELTS,
    @SerializedName("TOEIC") TOEIC,
    @SerializedName("TOEFL") TOEFL,
    @SerializedName("CUSTOM") CUSTOM
}

@Keep
enum class ExamSectionType {
    @SerializedName("LISTENING") LISTENING,
    @SerializedName("READING") READING,
    @SerializedName("WRITING") WRITING,
    @SerializedName("SPEAKING") SPEAKING,
    @SerializedName("GENERAL") GENERAL
}

@Keep
enum class ExamGroupType {
    @SerializedName("LISTENING_PART") LISTENING_PART,
    @SerializedName("PASSAGE") PASSAGE,
    @SerializedName("WRITING_TASK") WRITING_TASK,
    @SerializedName("SPEAKING_PART") SPEAKING_PART,
    @SerializedName("GENERAL") GENERAL
}

@Keep
enum class ExamQuestionType {
    @SerializedName("MULTIPLE_CHOICE") MULTIPLE_CHOICE,
    @SerializedName("SHORT_ANSWER") SHORT_ANSWER,
    @SerializedName("MATCHING") MATCHING,
    @SerializedName("ESSAY") ESSAY,
    @SerializedName("SPEAKING_PROMPT") SPEAKING_PROMPT,
    @SerializedName("TRUE_FALSE_NOT_GIVEN") TRUE_FALSE_NOT_GIVEN,
    @SerializedName("FILL_IN_THE_BLANK") FILL_IN_THE_BLANK,
    @SerializedName("ORDERING") ORDERING,
    @SerializedName("YES_NO_NOT_GIVEN") YES_NO_NOT_GIVEN,
    @SerializedName("DIAGRAM_LABELING") DIAGRAM_LABELING,
    @SerializedName("TABLE_COMPLETION") TABLE_COMPLETION,
    @SerializedName("FLOWCHART_COMPLETION") FLOWCHART_COMPLETION,
    @SerializedName("NOTE_COMPLETION") NOTE_COMPLETION
}

// ==================== Import Requests ====================

@Keep
data class ImportExamQuestionReq(
    @SerializedName("questionType") val questionType: ExamQuestionType,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("options") val options: List<Any>? = null, // Can be String or Map
    @SerializedName("correctAnswer") val correctAnswer: Any? = null,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("scoreWeight") val scoreWeight: Double? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)

@Keep
data class ImportExamQuestionGroupReq(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("resourceUrl") val resourceUrl: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("questions") val questions: List<ImportExamQuestionReq>
)

@Keep
data class ImportExamSectionGroupReq(
    @SerializedName("groupType") val groupType: ExamGroupType,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("resourceUrl") val resourceUrl: String? = null,
    @SerializedName("displayOrder") val displayOrder: Int? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("questionGroups") val questionGroups: List<ImportExamQuestionGroupReq>
)

@Keep
data class ImportExamSectionReq(
    @SerializedName("sectionType") val sectionType: ExamSectionType,
    @SerializedName("title") val title: String,
    @SerializedName("displayOrder") val displayOrder: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("instructions") val instructions: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("groups") val groups: List<ImportExamSectionGroupReq>
)

@Keep
data class ImportExamBodyReq(
    @SerializedName("examType") val examType: ExamType,
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("totalDurationSeconds") val totalDurationSeconds: Int? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    @SerializedName("isPublished") val isPublished: Boolean? = null,
    @SerializedName("sections") val sections: List<ImportExamSectionReq>
)
