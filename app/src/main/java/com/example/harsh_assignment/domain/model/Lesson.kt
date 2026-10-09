package com.example.harsh_assignment.domain.model

/**
 * A lesson belonging to exactly one course.
 *
 * IDs and ordering are validated at the domain boundary so invalid remote or local data cannot
 * silently enter presentation logic.
 */
data class Lesson(
    val id: Long,
    val courseId: Long,
    val title: String,
    val order: Int,
    val isCompleted: Boolean,
) {
    init {
        require(id > 0) { "Lesson id must be positive." }
        require(courseId > 0) { "Course id must be positive." }
        require(title.isNotBlank()) { "Lesson title must not be blank." }
        require(order >= 0) { "Lesson order must not be negative." }
    }
}
