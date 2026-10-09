package com.example.harsh_assignment.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CourseTest {

    @Test
    fun `progress is zero when course has no lessons`() {
        val course = createCourse(lessons = emptyList())

        assertEquals(0, course.progress)
    }

    @Test
    fun `progress reflects completed lesson ratio`() {
        val course = createCourse(
            lessons = listOf(
                createLesson(id = 1, order = 0, isCompleted = true),
                createLesson(id = 2, order = 1, isCompleted = false),
                createLesson(id = 3, order = 2, isCompleted = true),
                createLesson(id = 4, order = 3, isCompleted = false),
            ),
        )

        assertEquals(50, course.progress)
    }

    @Test
    fun `progress is one hundred when every lesson is completed`() {
        val course = createCourse(
            lessons = listOf(
                createLesson(id = 1, order = 0, isCompleted = true),
                createLesson(id = 2, order = 1, isCompleted = true),
            ),
        )

        assertEquals(100, course.progress)
    }

    @Test
    fun `course rejects a lesson owned by another course`() {
        val foreignLesson = Lesson(
            id = 1,
            courseId = 99,
            title = "Foreign lesson",
            order = 0,
            isCompleted = false,
        )

        assertThrows(IllegalArgumentException::class.java) {
            createCourse(lessons = listOf(foreignLesson))
        }
    }

    private fun createCourse(lessons: List<Lesson>): Course = Course(
        id = COURSE_ID,
        title = "Test course",
        instructor = "Test instructor",
        lessons = lessons,
    )

    private fun createLesson(
        id: Long,
        order: Int,
        isCompleted: Boolean,
    ): Lesson = Lesson(
        id = id,
        courseId = COURSE_ID,
        title = "Lesson $id",
        order = order,
        isCompleted = isCompleted,
    )

    private companion object {
        const val COURSE_ID = 1L
    }
}
