package com.example.lingora_fe.user.classroom.presentation

import arrow.core.Either
import com.example.lingora_fe.core.error.AppFailure
import com.example.lingora_fe.user.classroom.domain.model.Classroom
import com.example.lingora_fe.user.classroom.domain.model.ClassroomListResult
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
class ClassroomListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ClassroomRepository = mockk()

    private val testClassroom = Classroom(
        id = 1,
        code = "ABC123",
        name = "Test Classroom",
        description = null,
        coverImageUrl = null,
        maxStudents = 30,
        status = ClassroomStatus.ACTIVE,
        isPublic = true,
        settings = emptyMap(),
        teacher = ClassroomUser(id = 10, username = "teacher", avatar = null),
        totalMembers = 5,
        createdAt = null,
        updatedAt = null
    )

    private val testResult = ClassroomListResult(
        currentPage = 1,
        totalPages = 1,
        total = 1,
        classrooms = listOf(testClassroom)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery {
            repository.getAllClassrooms(any(), any(), any(), any(), any())
        } returns Either.Right(testResult)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads classrooms on init`() = runTest {
        val viewModel = ClassroomListViewModel(repository)

        assertEquals(listOf(testClassroom), viewModel.state.value.classrooms)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `shows error when load fails`() = runTest {
        coEvery {
            repository.getAllClassrooms(any(), any(), any(), any(), any())
        } returns Either.Left(AppFailure.NetworkError("Network error"))

        val viewModel = ClassroomListViewModel(repository)

        assertEquals("Network error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.classrooms.isEmpty())
    }

    @Test
    fun `initial tab 0 loads with isPublic true`() = runTest {
        ClassroomListViewModel(repository)

        coVerify {
            repository.getAllClassrooms(any(), any(), any(), eq(true), any())
        }
    }

    @Test
    fun `selectTab to tab 1 reloads with isPublic null`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.selectTab(1)

        coVerify {
            repository.getAllClassrooms(any(), any(), any(), null, any())
        }
        assertEquals(1, viewModel.state.value.selectedTab)
    }

    @Test
    fun `selectTab to same tab does not reload`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.selectTab(0) // already on tab 0 — should be a no-op

        // Only 1 call: from init
        coVerify(exactly = 1) {
            repository.getAllClassrooms(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `onSearchQueryChange updates state without triggering load`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.onSearchQueryChange("english")

        assertEquals("english", viewModel.state.value.searchQuery)
        // Still only 1 call from init
        coVerify(exactly = 1) {
            repository.getAllClassrooms(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `applySearch triggers reload with search query`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.onSearchQueryChange("english")
        viewModel.applySearch()

        coVerify {
            repository.getAllClassrooms(eq(1), any(), eq("english"), any(), any())
        }
    }

    @Test
    fun `applySearch with empty query sends null search`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.applySearch()

        coVerify {
            repository.getAllClassrooms(any(), any(), null, any(), any())
        }
    }

    @Test
    fun `deleteClassroom removes it from state on success`() = runTest {
        coEvery { repository.deleteClassroom(1) } returns Either.Right(Unit)
        val viewModel = ClassroomListViewModel(repository)

        viewModel.deleteClassroom(1)

        assertTrue(viewModel.state.value.classrooms.none { it.id == 1 })
    }

    @Test
    fun `deleteClassroom sets error on failure`() = runTest {
        coEvery { repository.deleteClassroom(1) } returns Either.Left(AppFailure.ServerError("Delete failed"))
        val viewModel = ClassroomListViewModel(repository)

        viewModel.deleteClassroom(1)

        assertEquals("Delete failed", viewModel.state.value.error)
        // Classroom still in list
        assertTrue(viewModel.state.value.classrooms.any { it.id == 1 })
    }

    @Test
    fun `refresh reloads classrooms`() = runTest {
        val viewModel = ClassroomListViewModel(repository)
        viewModel.refresh()

        coVerify(atLeast = 2) {
            repository.getAllClassrooms(any(), any(), any(), any(), any())
        }
    }
}
