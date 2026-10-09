package com.example.harsh_assignment.data.repository

import com.example.harsh_assignment.data.local.CourseLocalDataSource
import com.example.harsh_assignment.data.mapper.toDomain
import com.example.harsh_assignment.data.mapper.toEntitySnapshot
import com.example.harsh_assignment.data.remote.CourseRemoteDataSource
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.repository.CourseRefreshException
import com.example.harsh_assignment.domain.repository.CourseRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class OfflineFirstCourseRepository @Inject constructor(
    private val remoteDataSource: CourseRemoteDataSource,
    private val localDataSource: CourseLocalDataSource,
    private val authRepository: AuthRepository,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        localDataSource.observeCourses().map { courses ->
            if (authRepository.isLoggedIn()) courses.map { it.toDomain() } else emptyList()
        }

    override fun observeCourse(courseId: Long): Flow<Course?> =
        localDataSource.observeCourse(courseId).map {
            if (authRepository.isLoggedIn()) it?.toDomain() else null
        }

    override suspend fun refreshCourses() {
        ensureAuthenticated()
        try {
            val snapshot = remoteDataSource.getCourses().toEntitySnapshot()
            ensureAuthenticated()
            localDataSource.replaceCourses(snapshot.courses, snapshot.lessons)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: IOException) {
            throw CourseRefreshException.Network(network)
        } catch (http: HttpException) {
            throw CourseRefreshException.Network(http)
        } catch (invalidData: IllegalArgumentException) {
            throw CourseRefreshException.InvalidData(invalidData)
        } catch (notAuthenticated: CourseRefreshException.NotAuthenticated) {
            throw notAuthenticated
        } catch (unexpected: Throwable) {
            throw CourseRefreshException.Unknown(unexpected)
        }
    }

    override suspend fun markLessonCompleted(lessonId: Long) {
        ensureAuthenticated()
        require(lessonId > 0) { "Lesson id must be positive." }
        localDataSource.markLessonCompleted(lessonId)
    }

    override suspend fun clearCachedCourses() = localDataSource.clearCourses()

    private fun ensureAuthenticated() {
        if (!authRepository.isLoggedIn()) {
            throw CourseRefreshException.NotAuthenticated()
        }
    }
}
