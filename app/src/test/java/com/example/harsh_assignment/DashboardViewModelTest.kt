package com.example.harsh_assignment.ui.dashboard

import com.example.harsh_assignment.MainDispatcherRule
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.model.AuthenticationResult
import com.example.harsh_assignment.domain.model.Lesson
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.repository.CourseRefreshException
import com.example.harsh_assignment.domain.repository.CourseRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `first load remains loading while remote refresh is pending`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val response = CompletableDeferred<Unit>()
        val repository = FakeCourseRepository { response.await() }
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())

        runCurrent()

        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)
        assertEquals(1, repository.refreshCalls)

        viewModel.retry()
        runCurrent()
        assertEquals(1, repository.refreshCalls)

        response.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `successful refresh displays courses from repository flow`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository {
            courses.value = listOf(course())
        }
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(1, state.courses.size)
        assertFalse(state.isRefreshing)
        assertFalse(state.isShowingCachedData)
    }

    @Test
    fun `successful empty response displays empty state`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val viewModel = DashboardViewModel(FakeCourseRepository { }, FakeAuthRepository())

        advanceUntilIdle()

        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `network failure without cache displays retryable error`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository {
            throw CourseRefreshException.Network(IOException("offline"))
        }
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())

        advanceUntilIdle()

        assertEquals(
            DashboardUiState.Error(DashboardError.NETWORK),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `network failure with cache keeps courses visible`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository(
            initialCourses = listOf(course()),
        ) {
            throw CourseRefreshException.Network(IOException("offline"))
        }
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(1, state.courses.size)
        assertTrue(state.isShowingCachedData)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `retry can recover after an initial failure`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        var shouldFail = true
        val repository = FakeCourseRepository {
            if (shouldFail) {
                shouldFail = false
                throw CourseRefreshException.Network(IOException("offline"))
            }
            courses.value = listOf(course())
        }
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DashboardUiState.Error)

        viewModel.retry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DashboardUiState.Success)
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `logout cancels refresh clears cache then ends session`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val pendingRefresh = CompletableDeferred<Unit>()
        val repository = FakeCourseRepository(initialCourses = listOf(course())) {
            pendingRefresh.await()
        }
        val authRepository = FakeAuthRepository()
        val viewModel = DashboardViewModel(repository, authRepository)
        runCurrent()
        val logoutEvent = async { viewModel.logoutEvents.first() }

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(1, repository.clearCalls)
        assertTrue(repository.courses.value.isEmpty())
        assertEquals(1, authRepository.logoutCalls)
        assertFalse(authRepository.isLoggedIn())
        assertEquals(Unit, logoutEvent.await())
        assertFalse(viewModel.logoutUiState.value.isLoggingOut)
    }

    @Test
    fun `cache cleanup failure keeps session and exposes retryable logout error`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository(
            clearFailure = IllegalStateException("database unavailable"),
        ) { }
        val authRepository = FakeAuthRepository()
        val viewModel = DashboardViewModel(repository, authRepository)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(1, repository.clearCalls)
        assertEquals(0, authRepository.logoutCalls)
        assertTrue(authRepository.isLoggedIn())
        assertTrue(viewModel.logoutUiState.value.hasError)
        assertFalse(viewModel.logoutUiState.value.isLoggingOut)
    }

    private class FakeCourseRepository(
        initialCourses: List<Course> = emptyList(),
        private val clearFailure: Throwable? = null,
        private val refresh: suspend FakeCourseRepository.() -> Unit,
    ) : CourseRepository {
        val courses = MutableStateFlow(initialCourses)
        var refreshCalls: Int = 0
            private set
        var clearCalls: Int = 0
            private set

        override fun observeCourses(): Flow<List<Course>> = courses

        override fun observeCourse(courseId: Long): Flow<Course?> =
            MutableStateFlow(courses.value.firstOrNull { it.id == courseId })

        override suspend fun refreshCourses() {
            refreshCalls += 1
            refresh()
        }

        override suspend fun markLessonCompleted(lessonId: Long) = Unit

        override suspend fun clearCachedCourses() {
            clearCalls += 1
            clearFailure?.let { throw it }
            courses.value = emptyList()
        }
    }

    private class FakeAuthRepository : AuthRepository {
        private var loggedIn = true
        var logoutCalls: Int = 0
            private set

        override fun isLoggedIn(): Boolean = loggedIn

        override suspend fun login(email: String, password: String): AuthenticationResult {
            loggedIn = true
            return AuthenticationResult.Success
        }

        override suspend fun logout() {
            logoutCalls += 1
            loggedIn = false
        }
    }

    private fun course(): Course = Course(
        id = 1,
        title = "Python Programming",
        instructor = "John Smith",
        lessons = listOf(
            Lesson(101, 1, "Introduction", 0, true),
            Lesson(102, 1, "Functions", 1, false),
        ),
    )
}
