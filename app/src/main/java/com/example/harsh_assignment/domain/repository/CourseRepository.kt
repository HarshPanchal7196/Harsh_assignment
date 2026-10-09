package com.example.harsh_assignment.domain.repository

import com.example.harsh_assignment.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    fun observeCourse(courseId: Long): Flow<Course?>
    suspend fun refreshCourses()
    suspend fun markLessonCompleted(lessonId: Long)
    suspend fun clearCachedCourses()
}

sealed class CourseRefreshException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Network(cause: Throwable) : CourseRefreshException("Unable to reach course service.", cause)
    class InvalidData(cause: Throwable) : CourseRefreshException("Course data is invalid.", cause)
    class NotAuthenticated : CourseRefreshException("A logged-in session is required.")
    class Unknown(cause: Throwable) : CourseRefreshException("Unable to refresh courses.", cause)
}
