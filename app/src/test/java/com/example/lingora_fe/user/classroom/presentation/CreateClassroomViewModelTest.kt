package com.example.lingora_fe.user.classroom.presentation

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomUser
import com.example.lingora_fe.user.classroom.domain.repository.ClassroomRepository
import com.example.lingora_fe.user.classroom.util.ClassroomStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateClassroomViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ClassroomRepository = mockk()

    private val createdClassroom = Classroom(
        id = 42,
        code = "XYZ789",
        name = "New Classroom",
        description = "A test description",
        coverImageUrl = null,
        maxStudents = 20,
        status = ClassroomStatus.ACTIVE,
        isPublic = true,
        settings = emptyMap(),
        teacher = ClassroomUser(id = 1, username = "teacher", avatar = null),
        totalMembers = 0,
        createdAt = null,
        updatedAt = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct defaults`() {
        val viewModel = CreateClassroomViewModel(repository)
        val state = viewModel.state.value

        assertEquals("", state.name)
        assertEquals("", state.description)
        assertTrue(state.isPublic)
        assertNull(state.maxStudents)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.createdClassroomId)
    }

    @Test
    fun `onNameChange updates name`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("My Classroom")

        assertEquals("My Classroom", viewModel.state.value.name)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onDescriptionChange("A description")

        assertEquals("A description", viewModel.state.value.description)
    }

    @Test
    fun `onIsPublicChange updates isPublic`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onIsPublicChange(false)

        assertFalse(viewModel.state.value.isPublic)
    }

    @Test
    fun `onMaxStudentsChange parses valid integer`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onMaxStudentsChange("25")

        assertEquals(25, viewModel.state.value.maxStudents)
    }

    @Test
    fun `onMaxStudentsChange sets null for non-numeric input`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onMaxStudentsChange("abc")

        assertNull(viewModel.state.value.maxStudents)
    }

    @Test
    fun `onMaxStudentsChange sets null for empty input`() {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onMaxStudentsChange("25")
        viewModel.onMaxStudentsChange("")

        assertNull(viewModel.state.value.maxStudents)
    }

    @Test
    fun `submit with blank name sets error and does not call repository`() = runTest {
        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("   ")

        viewModel.submit()

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
        coVerify(exactly = 0) {
            repository.createClassroom(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `submit with empty name sets error`() = runTest {
        val viewModel = CreateClassroomViewModel(repository)
        // name is "" by default
        viewModel.submit()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `submit successfully creates classroom and sets success state`() = runTest {
        coEvery {
            repository.createClassroom(any(), any(), any(), any(), any())
        } returns Either.Right(createdClassroom)

        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("New Classroom")
        viewModel.onDescriptionChange("A test description")
        viewModel.onMaxStudentsChange("20")

        viewModel.submit()

        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(42, viewModel.state.value.createdClassroomId)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `submit trims name before sending`() = runTest {
        coEvery {
            repository.createClassroom(any(), any(), any(), any(), any())
        } returns Either.Right(createdClassroom)

        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("  Trimmed Name  ")
        viewModel.submit()

        coVerify {
            repository.createClassroom(eq("Trimmed Name"), any(), any(), any(), any())
        }
    }

    @Test
    fun `submit sends null description for empty description`() = runTest {
        coEvery {
            repository.createClassroom(any(), any(), any(), any(), any())
        } returns Either.Right(createdClassroom)

        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("My Class")
        // description is "" by default
        viewModel.submit()

        coVerify {
            repository.createClassroom(any(), null, any(), any(), any())
        }
    }

    @Test
    fun `submit on failure sets error message`() = runTest {
        coEvery {
            repository.createClassroom(any(), any(), any(), any(), any())
        } returns Either.Left(AppFailure.ServerError("Server error"))

        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("My Class")
        viewModel.submit()

        assertEquals("Server error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `resetError clears error`() = runTest {
        coEvery {
            repository.createClassroom(any(), any(), any(), any(), any())
        } returns Either.Left(AppFailure.ServerError("Error"))

        val viewModel = CreateClassroomViewModel(repository)
        viewModel.onNameChange("My Class")
        viewModel.submit()
        assertNotNull(viewModel.state.value.error)

        viewModel.resetError()

        assertNull(viewModel.state.value.error)
    }
}
