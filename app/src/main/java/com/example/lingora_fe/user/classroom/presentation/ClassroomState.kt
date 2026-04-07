package com.example.lingora_fe.user.classroom.presentation

import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomFlashcard
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLessonDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizDetail
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuizQuestion
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.QuizType

data class ClassroomListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val classrooms: List<Classroom> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val searchQuery: String = "",
    val selectedTab: Int = 0,  // 0: Khám phá (public), 1: Của tôi (all)
    val showJoinDialog: Boolean = false,
    val joinCode: String = "",
    val isJoining: Boolean = false,
    val joinError: String? = null,
    val currentUserId: Int? = null,
    val selectedStatusFilter: Int = 0 // 0: Tất cả, 1: Công khai (Active), 2: Lưu trữ, 3: Nháp
)

data class ClassroomDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val classroom: Classroom? = null,
    val lessons: List<ClassroomLesson> = emptyList(),
    val quizzes: List<ClassroomQuiz> = emptyList(),
    val members: List<ClassroomMember> = emptyList(),
    val chatMessages: List<ClassroomMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val chatInput: String = "",
    val isSendingMessage: Boolean = false,
    val isMembersLoading: Boolean = false,
    val selectedTab: Int = 0,  // 0: Bài học, 1: Bài kiểm tra, 2: Thảo luận, 3: Thành viên
    val currentUserRole: String? = null,
    val currentUserId: Int? = null,
    val memberToRemove: ClassroomMember? = null,
    val isRemovingMember: Boolean = false,
    val isDeleted: Boolean = false
)

data class CreateClassroomState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val maxStudents: Int? = null,
    val status: com.example.lingora_fe.user.classroom.util.ClassroomStatus = com.example.lingora_fe.user.classroom.util.ClassroomStatus.ACTIVE,
    val coverImageUri: String? = null,
    val isSuccess: Boolean = false,
    val createdClassroomId: Int? = null,
    val isEditMode: Boolean = false,
    val classroomId: Int? = null
)

data class CreateLessonState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val description: String = "",
    val lessonType: ClassroomLessonType = ClassroomLessonType.TEXT,
    val content: String = "",
    val sortOrder: Int = 0,
    val isPublished: Boolean = false,
    val isSuccess: Boolean = false
)

data class LessonDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lesson: ClassroomLessonDetail? = null,
    val showAddFlashcardDialog: Boolean = false,
    val editingFlashcard: ClassroomFlashcard? = null,
    val flashcardFront: String = "",
    val flashcardBack: String = "",
    val flashcardExample: String = "",
    val isSavingFlashcard: Boolean = false,
    // Import from StudySet
    val showImportStudySetDialog: Boolean = false,
    val studySetOptions: List<StudySetOption> = emptyList(),
    val isLoadingStudySets: Boolean = false,
    val isImporting: Boolean = false,
    val selectedStudySetId: Int? = null
)

data class StudySetOption(
    val id: Int,
    val title: String
)

data class CreateQuizState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val description: String = "",
    val timeLimitSeconds: Int? = null,
    val maxAttempts: Int = 1,
    val passingScore: Double = 70.0,
    val isPublished: Boolean = false,
    val isSuccess: Boolean = false
)

data class QuizDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val quiz: ClassroomQuizDetail? = null,
    val showAddQuestionDialog: Boolean = false,
    val editingQuestion: ClassroomQuizQuestion? = null,
    val questionType: QuizType = QuizType.MULTIPLE_CHOICE,
    val questionText: String = "",
    val questionOptions: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val isSavingQuestion: Boolean = false,
    // Import from StudySet
    val showImportStudySetDialog: Boolean = false,
    val studySetOptions: List<StudySetOption> = emptyList(),
    val isLoadingStudySets: Boolean = false,
    val isImporting: Boolean = false,
    val selectedStudySetId: Int? = null
)
