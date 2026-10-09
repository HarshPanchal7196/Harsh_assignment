package com.example.harsh_assignment.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.harsh_assignment.MainDispatcherRule
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.model.Lesson
import com.example.harsh_assignment.domain.repository.CourseRepository
import com.example.harsh_assignment.ui.navigation.AppDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `invalid course id displays a specific error`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val viewModel = createViewModel(
            courseId = 0,
            repository = FakeCourseRepository(course()),
        )
        collectState(viewModel)
        runCurrent()

        assertEquals(
            CourseDetailsUiState.Error(CourseDetailsError.INVALID_COURSE_ID),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `missing cached course displays unavailable error`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val viewModel = createViewModel(
            courseId = 99,
            repository = FakeCourseRepository(null),
        )
        collectState(viewModel)
        runCurrent()

        assertEquals(
            CourseDetailsUiState.Error(CourseDetailsError.COURSE_NOT_FOUND),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `cached course is exposed with its derived progress`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val viewModel = createViewModel(repository = FakeCourseRepository(course()))
        collectState(viewModel)
        runCurrent()

        val state = viewModel.uiState.value as CourseDetailsUiState.Success
        assertEquals("Python Programming", state.course.title)
        assertEquals(50, state.course.progress)
    }

    @Test
    fun `completed and unknown lesson completion requests are ignored`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository(course())
        val viewModel = createViewModel(repository = repository)
        collectState(viewModel)
        runCurrent()

        viewModel.markLessonCompleted(101)
        viewModel.markLessonCompleted(999)
        advanceUntilIdle()

        assertTrue(repository.completionCalls.isEmpty())
    }

    @Test
    fun `duplicate taps are ignored and room emission updates progress`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val completionGate = CompletableDeferred<Unit>()
        val repository = FakeCourseRepository(course()) { lessonId ->
            completionGate.await()
            updateLesson(lessonId)
        }
        val viewModel = createViewModel(repository = repository)
        collectState(viewModel)
        runCurrent()

        viewModel.markLessonCompleted(102)
        runCurrent()
        viewModel.markLessonCompleted(102)
        runCurrent()

        assertEquals(listOf(102L), repository.completionCalls)
        val pending = viewModel.uiState.value as CourseDetailsUiState.Success
        assertEquals(setOf(102L), pending.completingLessonIds)

        completionGate.complete(Unit)
        advanceUntilIdle()

        val completed = viewModel.uiState.value as CourseDetailsUiState.Success
        assertEquals(100, completed.course.progress)
        assertTrue(completed.course.lessons.single { it.id == 102L }.isCompleted)
        assertTrue(completed.completingLessonIds.isEmpty())
    }

    @Test
    fun `completion failure remains retryable without changing progress`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = FakeCourseRepository(course()) {
            throw IllegalStateException("database unavailable")
        }
        val viewModel = createViewModel(repository = repository)
        collectState(viewModel)
        runCurrent()

        viewModel.markLessonCompleted(102)
        advanceUntilIdle()

        val failed = viewModel.uiState.value as CourseDetailsUiState.Success
        assertEquals(50, failed.course.progress)
        assertEquals(CompletionError.UPDATE_FAILED, failed.completionError)
        assertTrue(failed.completingLessonIds.isEmpty())

        viewModel.markLessonCompleted(102)
        advanceUntilIdle()
        assertEquals(listOf(102L, 102L), repository.completionCalls)
    }

    private fun kotlinx.coroutines.test.TestScope.collectState(
        viewModel: CourseDetailsViewModel,
    ) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.uiState.collect()
    }

    private fun createViewModel(
        courseId: Long = 1,
        repository: CourseRepository,
    ): CourseDetailsViewModel = CourseDetailsViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(AppDestination.CourseDetails.COURSE_ID_ARGUMENT to courseId),
        ),
        courseRepository = repository,
    )

    private class FakeCourseRepository(
        initialCourse: Course?,
        private val complete: suspend FakeCourseRepository.(Long) -> Unit = { updateLesson(it) },
    ) : CourseRepository {
        private val selectedCourse = MutableStateFlow(initialCourse)
        val completionCalls = mutableListOf<Long>()

        override fun observeCourses(): Flow<List<Course>> = MutableStateFlow(
            listOfNotNull(selectedCourse.value),
        )

        override fun observeCourse(courseId: Long): Flow<Course?> = selectedCourse

        override suspend fun refreshCourses() = Unit

        override suspend fun markLessonCompleted(lessonId: Long) {
            completionCalls += lessonId
            complete(lessonId)
        }

        override suspend fun clearCachedCourses() {
            selectedCourse.value = null
        }

        fun updateLesson(lessonId: Long) {
            selectedCourse.value = selectedCourse.value?.let { course ->
                course.copy(
                    lessons = course.lessons.map { lesson ->
                        if (lesson.id == lessonId) lesson.copy(isCompleted = true) else lesson
                    },
                )
            }
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
