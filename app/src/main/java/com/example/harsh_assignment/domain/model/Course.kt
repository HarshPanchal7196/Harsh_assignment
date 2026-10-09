package com.example.harsh_assignment.domain.model

/**
 * Domain representation of a course. Progress is derived from lesson state to avoid maintaining
 * two conflicting sources of truth.
 */
data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>,
) {
    init {
        require(id > 0) { "Course id must be positive." }
        require(title.isNotBlank()) { "Course title must not be blank." }
        require(instructor.isNotBlank()) { "Instructor name must not be blank." }
        require(lessons.all { it.courseId == id }) {
            "Every lesson must belong to this course."
        }
        require(lessons.distinctBy(Lesson::id).size == lessons.size) {
            "Lesson ids must be unique within a course."
        }
        require(lessons.distinctBy(Lesson::order).size == lessons.size) {
            "Lesson order values must be unique within a course."
        }
    }

    val lessonCount: Int
        get() = lessons.size

    val progress: Int
        get() = if (lessons.isEmpty()) {
            0
        } else {
            lessons.count(Lesson::isCompleted) * 100 / lessons.size
        }
}
