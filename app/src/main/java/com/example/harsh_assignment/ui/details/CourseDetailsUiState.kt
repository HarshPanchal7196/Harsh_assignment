package com.example.harsh_assignment.ui.details

import com.example.harsh_assignment.domain.model.Course

sealed interface CourseDetailsUiState {
    data object Loading : CourseDetailsUiState

    data class Success(
        val course: Course,
        val completingLessonIds: Set<Long> = emptySet(),
        val completionError: CompletionError? = null,
    ) : CourseDetailsUiState

    data class Error(val reason: CourseDetailsError) : CourseDetailsUiState
}

enum class CourseDetailsError {
    INVALID_COURSE_ID,
    COURSE_NOT_FOUND,
    UNKNOWN,
}

enum class CompletionError {
    UPDATE_FAILED,
}
