package com.example.harsh_assignment.data.mapper

import com.example.harsh_assignment.data.local.entity.CourseEntity
import com.example.harsh_assignment.data.local.entity.CourseWithLessons
import com.example.harsh_assignment.data.local.entity.LessonEntity
import com.example.harsh_assignment.data.remote.model.CourseDto
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.model.Lesson

data class CourseEntitySnapshot(
    val courses: List<CourseEntity>,
    val lessons: List<LessonEntity>,
)

fun List<CourseDto>.toEntitySnapshot(): CourseEntitySnapshot {
    require(distinctBy(CourseDto::id).size == size) { "Course ids must be unique." }

    val allLessonIds = flatMap { it.lessons.orEmpty() }.map { it.id }
    require(allLessonIds.distinct().size == allLessonIds.size) {
        "Lesson ids must be globally unique."
    }

    val courseEntities = mapIndexed { index, dto ->
        require(dto.id > 0) { "Course id must be positive." }
        CourseEntity(
            id = dto.id,
            title = requireNotNull(dto.title).trim().also {
                require(it.isNotEmpty()) { "Course title must not be blank." }
            },
            instructor = requireNotNull(dto.instructor).trim().also {
                require(it.isNotEmpty()) { "Instructor name must not be blank." }
            },
            position = index,
        )
    }

    val lessonEntities = flatMap { course ->
        val lessons = requireNotNull(course.lessons) { "Course lessons are required." }
        require(lessons.distinctBy { it.order }.size == lessons.size) {
            "Lesson order values must be unique within a course."
        }

        lessons.map { lesson ->
            require(lesson.id > 0) { "Lesson id must be positive." }
            require(lesson.order >= 0) { "Lesson order must not be negative." }
            LessonEntity(
                id = lesson.id,
                courseId = course.id,
                title = requireNotNull(lesson.title).trim().also {
                    require(it.isNotEmpty()) { "Lesson title must not be blank." }
                },
                position = lesson.order,
                isCompleted = lesson.isCompleted,
            )
        }
    }

    return CourseEntitySnapshot(
        courses = courseEntities,
        lessons = lessonEntities,
    )
}

fun CourseWithLessons.toDomain(): Course = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons
        .sortedBy(LessonEntity::position)
        .map { lesson ->
            Lesson(
                id = lesson.id,
                courseId = lesson.courseId,
                title = lesson.title,
                order = lesson.position,
                isCompleted = lesson.isCompleted,
            )
        },
)
