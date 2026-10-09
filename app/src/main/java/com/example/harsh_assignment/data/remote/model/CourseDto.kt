package com.example.harsh_assignment.data.remote.model

/** Transport envelope returned by the hosted assignment mock. */
data class CoursesResponseDto(
    val value: List<CourseDto>? = null,
)

data class CourseDto(
    val id: Long = 0,
    val title: String? = null,
    val instructor: String? = null,
    val lessons: List<LessonDto>? = null,
)

data class LessonDto(
    val id: Long = 0,
    val title: String? = null,
    val order: Int = -1,
    val isCompleted: Boolean = false,
)
