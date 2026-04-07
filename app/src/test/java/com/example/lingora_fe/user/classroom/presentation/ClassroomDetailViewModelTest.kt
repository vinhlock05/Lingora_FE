package com.example.lingora_fe.user.classroom.presentation

import androidx.lifecycle.SavedStateHandle
import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomLesson
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMember
import com.example.lingora_fe.user.classroom.domain.model.ClassroomMessage
import com.example.lingora_fe.user.classroom.domain.model.ClassroomQuiz
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.ClassroomLessonType
import com.example.lingora_fe.user.classroom.util.ClassroomMemberRole
import com.example.lingora_fe.user.classroom.util.ClassroomMemberStatus
import com.example.lingora_fe.user.classroom.util.ClassroomMessageType
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
import com.example.lingora_fe.user.notification.data.socket.NotificationSocketManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClassroomDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ClassroomRepository = mockk()
    private val socketManager: NotificationSocketManager = mockk()

    private val classroomId = 1
    private val savedStateHandle = SavedStateHandle(mapOf("classroomId" to classroomId.toString()))

    private val testClassroom = Classroom(
        id = classroomId,
        code = "CLS001",
        name = "English 101",
        description = "Basic English",
        coverImageUrl = null,
        maxStudents = 30,
        status = ClassroomStatus.ACTIVE,
        isPublic = true,
        settings = emptyMap(),
        teacher = ClassroomUser(id = 5, username = "teacher", avatar = null),
        totalMembers = 10,
        createdAt = null,
        updatedAt = null
    )

    private val testLesson = ClassroomLesson(
        id = 1,
        title = "Lesson 1",
        description = null,
        lessonType = ClassroomLessonType.TEXT,
        content = "Content",
        sortOrder = 1,
        isPublished = true,
        scheduledAt = null,
        createdAt = null,
        updatedAt = null
    )

    private val testQuiz = ClassroomQuiz(
        id = 1,
        title = "Quiz 1",
        description = null,
        timeLimitSeconds = 600,
        maxAttempts = 3,
        passingScore = 70.0,
        isPublished = true,
        opensAt = null,
        closesAt = null,
        createdAt = null,
        updatedAt = null
    )

    private val testMessage = ClassroomMessage(
        id = 1,
        sender = ClassroomUser(id = 5, username = "teacher", avatar = null),
        type = ClassroomMessageType.TEXT,
        content = "Hello",
        attachmentUrl = null,
        repliedTo = null,
        createdAt = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getClassroomById(classroomId) } returns Either.Right(testClassroom)
        coEvery { repository.getLessons(classroomId) } returns Either.Right(listOf(testLesson))
        coEvery { repository.getQuizzes(classroomId) } returns Either.Right(listOf(testQuiz))
        coEvery { repository.getMembers(classroomId) } returns Either.Right(emptyList())
        coEvery { repository.getChatHistory(classroomId, any()) } returns Either.Right(listOf(testMessage))
        every { socketManager.joinClassroom(any()) } just runs
        every { socketManager.leaveClassroom(any()) } just runs
        every { socketManager.sendClassroomMessage(any(), any()) } just runs
        every { socketManager.classroomMessageFlow() } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ClassroomDetailViewModel(repository, socketManager, savedStateHandle)

    @Test
    fun `loads classroom detail on init`() = runTest {
        val viewModel = createViewModel()

        assertEquals(testClassroom, viewModel.state.value.classroom)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `loads lessons and quizzes on init`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(testLesson), viewModel.state.value.lessons)
        assertEquals(listOf(testQuiz), viewModel.state.value.quizzes)
    }

    @Test
    fun `loads chat history on init`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(testMessage), viewModel.state.value.chatMessages)
        assertFalse(viewModel.state.value.isChatLoading)
    }

    @Test
    fun `shows error when getClassroomById fails`() = runTest {
        coEvery {
            repository.getClassroomById(classroomId)
        } returns Either.Left(AppFailure.NotFoundError("Not found"))

        val viewModel = createViewModel()

        assertEquals("Not found", viewModel.state.value.error)
        assertNull(viewModel.state.value.classroom)
    }

    @Test
    fun `lessons and quizzes remain empty on non-fatal load failure`() = runTest {
        coEvery { repository.getLessons(classroomId) } returns Either.Left(AppFailure.NetworkError("Fail"))
        coEvery { repository.getQuizzes(classroomId) } returns Either.Left(AppFailure.NetworkError("Fail"))

        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.lessons.isEmpty())
        assertTrue(viewModel.state.value.quizzes.isEmpty())
    }

    @Test
    fun `joins socket room on init`() = runTest {
        createViewModel()

        verify { socketManager.joinClassroom(eq(classroomId)) }
    }

    @Test
    fun `selectTab updates selectedTab`() = runTest {
        val viewModel = createViewModel()

        viewModel.selectTab(1)
        assertEquals(1, viewModel.state.value.selectedTab)

        viewModel.selectTab(2)
        assertEquals(2, viewModel.state.value.selectedTab)
    }

    @Test
    fun `onChatInputChange updates chatInput`() = runTest {
        val viewModel = createViewModel()

        viewModel.onChatInputChange("Hello world")
        assertEquals("Hello world", viewModel.state.value.chatInput)
    }

    @Test
    fun `sendMessage calls socket and clears input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onChatInputChange("Hello")

        viewModel.sendMessage()

        verify { socketManager.sendClassroomMessage(eq(classroomId), eq("Hello")) }
        assertEquals("", viewModel.state.value.chatInput)
    }

    @Test
    fun `sendMessage does nothing for blank input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onChatInputChange("   ")

        viewModel.sendMessage()

        verify(exactly = 0) {
            socketManager.sendClassroomMessage(any(), any())
        }
    }

    @Test
    fun `deleteLesson removes lesson from state on success`() = runTest {
        coEvery { repository.deleteLesson(classroomId, 1) } returns Either.Right(Unit)
        val viewModel = createViewModel()

        viewModel.deleteLesson(1)

        assertTrue(viewModel.state.value.lessons.none { it.id == 1 })
    }

    @Test
    fun `deleteLesson sets error on failure`() = runTest {
        coEvery {
            repository.deleteLesson(classroomId, 1)
        } returns Either.Left(AppFailure.ServerError("Cannot delete"))
        val viewModel = createViewModel()

        viewModel.deleteLesson(1)

        assertEquals("Cannot delete", viewModel.state.value.error)
        // lesson still present
        assertTrue(viewModel.state.value.lessons.any { it.id == 1 })
    }

    @Test
    fun `deleteQuiz removes quiz from state on success`() = runTest {
        coEvery { repository.deleteQuiz(classroomId, 1) } returns Either.Right(Unit)
        val viewModel = createViewModel()

        viewModel.deleteQuiz(1)

        assertTrue(viewModel.state.value.quizzes.none { it.id == 1 })
    }

    @Test
    fun `deleteQuiz sets error on failure`() = runTest {
        coEvery {
            repository.deleteQuiz(classroomId, 1)
        } returns Either.Left(AppFailure.ServerError("Cannot delete quiz"))
        val viewModel = createViewModel()

        viewModel.deleteQuiz(1)

        assertEquals("Cannot delete quiz", viewModel.state.value.error)
        assertTrue(viewModel.state.value.quizzes.any { it.id == 1 })
    }

    @Test
    fun `onCleared leaves socket room`() = runTest {
        val viewModel = createViewModel()
        // onCleared() is protected — invoke via reflection
        viewModel.javaClass.getDeclaredMethod("onCleared").apply {
            isAccessible = true
            invoke(viewModel)
        }

        verify { socketManager.leaveClassroom(eq(classroomId)) }
    }

    @Test
    fun `showRemoveMemberDialog updates state`() = runTest {
        val member = ClassroomMember(
            id = 5,
            user = testMessage.sender,
            role = ClassroomMemberRole.STUDENT,
            status = ClassroomMemberStatus.ACTIVE,
            joinedAt = null,
            removedAt = null
        )
        val viewModel = createViewModel()

        viewModel.showRemoveMemberDialog(member)

        assertEquals(member, viewModel.state.value.memberToRemove)
    }

    @Test
    fun `dismissRemoveMemberDialog clears state`() = runTest {
        val member = ClassroomMember(
            id = 5,
            user = testMessage.sender,
            role = ClassroomMemberRole.STUDENT,
            status = ClassroomMemberStatus.ACTIVE,
            joinedAt = null,
            removedAt = null
        )
        val viewModel = createViewModel()
        viewModel.showRemoveMemberDialog(member)
        viewModel.dismissRemoveMemberDialog()

        assertNull(viewModel.state.value.memberToRemove)
    }

    @Test
    fun `confirmRemoveMember calls repository and reloads members on success`() = runTest {
        val member = ClassroomMember(
            id = 5,
            user = testMessage.sender,
            role = ClassroomMemberRole.STUDENT,
            status = ClassroomMemberStatus.ACTIVE,
            joinedAt = null,
            removedAt = null
        )
        coEvery { repository.removeMember(classroomId, member.id) } returns Either.Right(Unit)
        val viewModel = createViewModel()

        viewModel.showRemoveMemberDialog(member)
        viewModel.confirmRemoveMember()

        coVerify {
            repository.removeMember(classroomId, member.id)
        }
        assertNull(viewModel.state.value.memberToRemove)
        assertFalse(viewModel.state.value.isRemovingMember)
    }

    @Test
    fun `confirmRemoveMember failure sets error`() = runTest {
        val member = ClassroomMember(
            id = 5,
            user = testMessage.sender,
            role = ClassroomMemberRole.STUDENT,
            status = ClassroomMemberStatus.ACTIVE,
            joinedAt = null,
            removedAt = null
        )
        coEvery {
            repository.removeMember(classroomId, member.id)
        } returns Either.Left(AppFailure.ServerError("Cannot remove member"))
        val viewModel = createViewModel()

        viewModel.showRemoveMemberDialog(member)
        viewModel.confirmRemoveMember()

        assertEquals("Cannot remove member", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isRemovingMember)
        assertNull(viewModel.state.value.memberToRemove)
    }

    @Test
    fun `confirmRemoveMember with no member selected does nothing`() = runTest {
        val viewModel = createViewModel()
        // memberToRemove is null by default
        viewModel.confirmRemoveMember()

        coVerify(exactly = 0) {
            repository.removeMember(any(), any())
        }
    }
}
