package com.example.harsh_assignment.data.repository

import com.example.harsh_assignment.data.local.CourseLocalDataSource
import com.example.harsh_assignment.data.local.entity.CourseEntity
import com.example.harsh_assignment.data.local.entity.CourseWithLessons
import com.example.harsh_assignment.data.local.entity.LessonEntity
import com.example.harsh_assignment.data.remote.CourseRemoteDataSource
import com.example.harsh_assignment.data.remote.model.CourseDto
import com.example.harsh_assignment.data.remote.model.LessonDto
import com.example.harsh_assignment.domain.repository.CourseRefreshException
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.model.AuthenticationResult
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstCourseRepositoryTest {

    @Test
    fun `successful refresh saves validated remote data and exposes local domain data`() = runTest {
        val local = FakeLocalDataSource()
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource { listOf(validCourseDto()) },
            localDataSource = local,
            authRepository = FakeAuthRepository(),
        )

        repository.refreshCourses()

        assertEquals(1, local.replaceCalls)
        val courses = repository.observeCourses().first()
        assertEquals(1, courses.size)
        assertEquals("Python Programming", courses.single().title)
        assertEquals(50, courses.single().progress)
    }

    @Test
    fun `network failure does not replace cached data`() = runTest {
        val local = FakeLocalDataSource(listOf(validCourseWithLessons()))
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource { throw IOException("offline") },
            localDataSource = local,
            authRepository = FakeAuthRepository(),
        )

        val failure = runCatching {
            repository.refreshCourses()
        }.exceptionOrNull()

        assertTrue(failure is CourseRefreshException.Network)

        assertEquals(0, local.replaceCalls)
        assertEquals("Python Programming", repository.observeCourses().first().single().title)
    }

    @Test
    fun `invalid remote payload is rejected before cache is changed`() = runTest {
        val local = FakeLocalDataSource(listOf(validCourseWithLessons()))
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource {
                listOf(validCourseDto().copy(title = " "))
            },
            localDataSource = local,
            authRepository = FakeAuthRepository(),
        )

        val failure = runCatching {
            repository.refreshCourses()
        }.exceptionOrNull()

        assertTrue(failure is CourseRefreshException.InvalidData)

        assertEquals(0, local.replaceCalls)
        assertEquals(1, repository.observeCourses().first().size)
    }

    @Test
    fun `logged out session cannot observe or refresh cached courses`() = runTest {
        var remoteCalls = 0
        val local = FakeLocalDataSource(listOf(validCourseWithLessons()))
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource {
                remoteCalls += 1
                listOf(validCourseDto())
            },
            localDataSource = local,
            authRepository = FakeAuthRepository(loggedIn = false),
        )

        assertTrue(repository.observeCourses().first().isEmpty())
        val failure = runCatching { repository.refreshCourses() }.exceptionOrNull()

        assertTrue(failure is CourseRefreshException.NotAuthenticated)
        assertEquals(0, remoteCalls)
        assertEquals(0, local.replaceCalls)
    }

    @Test
    fun `session ending during fetch prevents response from being persisted`() = runTest {
        val auth = FakeAuthRepository()
        val local = FakeLocalDataSource()
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource {
                auth.logout()
                listOf(validCourseDto())
            },
            localDataSource = local,
            authRepository = auth,
        )

        val failure = runCatching { repository.refreshCourses() }.exceptionOrNull()

        assertTrue(failure is CourseRefreshException.NotAuthenticated)
        assertEquals(0, local.replaceCalls)
    }

    @Test
    fun `clear cached courses removes all locally observed data`() = runTest {
        val local = FakeLocalDataSource(listOf(validCourseWithLessons()))
        val repository = OfflineFirstCourseRepository(
            remoteDataSource = FakeRemoteDataSource { emptyList() },
            localDataSource = local,
            authRepository = FakeAuthRepository(),
        )

        repository.clearCachedCourses()

        assertTrue(repository.observeCourses().first().isEmpty())
    }

    private class FakeRemoteDataSource(
        private val response: suspend () -> List<CourseDto>,
    ) : CourseRemoteDataSource {
        override suspend fun getCourses(): List<CourseDto> = response()
    }

    private class FakeLocalDataSource(
        initialCourses: List<CourseWithLessons> = emptyList(),
    ) : CourseLocalDataSource {
        private val courses = MutableStateFlow(initialCourses)
        var replaceCalls: Int = 0
            private set

        override fun observeCourses(): Flow<List<CourseWithLessons>> = courses

        override fun observeCourse(courseId: Long): Flow<CourseWithLessons?> =
            MutableStateFlow(courses.value.firstOrNull { it.course.id == courseId })

        override suspend fun replaceCourses(
            courses: List<CourseEntity>,
            lessons: List<LessonEntity>,
        ) {
            replaceCalls += 1
            this.courses.value = courses.map { course ->
                CourseWithLessons(
                    course = course,
                    lessons = lessons.filter { it.courseId == course.id },
                )
            }
        }

        override suspend fun markLessonCompleted(lessonId: Long): Boolean = false

        override suspend fun clearCourses() {
            courses.value = emptyList()
        }
    }

    private class FakeAuthRepository(
        private var loggedIn: Boolean = true,
    ) : AuthRepository {
        override fun isLoggedIn(): Boolean = loggedIn

        override suspend fun login(email: String, password: String): AuthenticationResult {
            loggedIn = true
            return AuthenticationResult.Success
        }

        override suspend fun logout() {
            loggedIn = false
        }
    }

    private fun validCourseDto(): CourseDto = CourseDto(
        id = 1,
        title = "Python Programming",
        instructor = "John Smith",
        lessons = listOf(
            LessonDto(101, "Introduction", 0, true),
            LessonDto(102, "Functions", 1, false),
        ),
    )

    private fun validCourseWithLessons(): CourseWithLessons = CourseWithLessons(
        course = CourseEntity(1, "Python Programming", "John Smith", 0),
        lessons = listOf(
            LessonEntity(101, 1, "Introduction", 0, true),
            LessonEntity(102, 1, "Functions", 1, false),
        ),
    )
}
