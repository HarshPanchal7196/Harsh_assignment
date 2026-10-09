package com.example.harsh_assignment.data.local

import com.example.harsh_assignment.data.local.entity.CourseEntity
import com.example.harsh_assignment.data.local.entity.CourseWithLessons
import com.example.harsh_assignment.data.local.entity.LessonEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface CourseLocalDataSource {
    fun observeCourses(): Flow<List<CourseWithLessons>>
    fun observeCourse(courseId: Long): Flow<CourseWithLessons?>
    suspend fun replaceCourses(courses: List<CourseEntity>, lessons: List<LessonEntity>)
    suspend fun markLessonCompleted(lessonId: Long): Boolean
    suspend fun clearCourses()
}

class RoomCourseLocalDataSource @Inject constructor(
    private val courseDao: CourseDao,
) : CourseLocalDataSource {
    override fun observeCourses(): Flow<List<CourseWithLessons>> = courseDao.observeCourses()

    override fun observeCourse(courseId: Long): Flow<CourseWithLessons?> =
        courseDao.observeCourse(courseId)

    override suspend fun replaceCourses(
        courses: List<CourseEntity>,
        lessons: List<LessonEntity>,
    ) = courseDao.replaceCourses(courses, lessons)

    override suspend fun markLessonCompleted(lessonId: Long): Boolean =
        courseDao.markLessonCompleted(lessonId) > 0

    override suspend fun clearCourses() = courseDao.clearCourses()
}
