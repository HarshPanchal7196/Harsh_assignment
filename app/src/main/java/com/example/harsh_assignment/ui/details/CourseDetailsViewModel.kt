package com.example.harsh_assignment.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harsh_assignment.domain.repository.CourseRepository
import com.example.harsh_assignment.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val courseId: Long? = savedStateHandle
        .get<Long>(AppDestination.CourseDetails.COURSE_ID_ARGUMENT)
        ?.takeIf { it > 0 }

    private val actionState = MutableStateFlow(CompletionActionState())

    val uiState = courseStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CourseDetailsUiState.Loading,
        )

    fun markLessonCompleted(lessonId: Long) {
        if (lessonId <= 0) return

        val currentState = uiState.value as? CourseDetailsUiState.Success ?: return
        val lesson = currentState.course.lessons.firstOrNull { it.id == lessonId } ?: return
        if (lesson.isCompleted || lessonId in currentState.completingLessonIds) return

        actionState.update { state ->
            state.copy(
                pendingLessonIds = state.pendingLessonIds + lessonId,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                courseRepository.markLessonCompleted(lessonId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                actionState.update { state ->
                    state.copy(
                        pendingLessonIds = state.pendingLessonIds - lessonId,
                        error = CompletionError.UPDATE_FAILED,
                    )
                }
            }
        }
    }

    private fun courseStateFlow(): Flow<CourseDetailsUiState> {
        val validCourseId = courseId
            ?: return flowOf(
                CourseDetailsUiState.Error(CourseDetailsError.INVALID_COURSE_ID),
            )

        return combine(
            courseRepository.observeCourse(validCourseId),
            actionState,
        ) { course, action ->
            if (course == null) {
                CourseDetailsUiState.Error(CourseDetailsError.COURSE_NOT_FOUND)
            } else {
                val incompleteLessonIds = course.lessons
                    .asSequence()
                    .filterNot { it.isCompleted }
                    .mapTo(mutableSetOf()) { it.id }

                CourseDetailsUiState.Success(
                    course = course,
                    completingLessonIds = action.pendingLessonIds intersect incompleteLessonIds,
                    completionError = action.error,
                )
            }
        }.catch {
            emit(CourseDetailsUiState.Error(CourseDetailsError.UNKNOWN))
        }
    }

    private data class CompletionActionState(
        val pendingLessonIds: Set<Long> = emptySet(),
        val error: CompletionError? = null,
    )
}
