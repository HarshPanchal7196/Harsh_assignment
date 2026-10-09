package com.example.harsh_assignment.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.harsh_assignment.R
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.model.Lesson
import com.example.harsh_assignment.ui.theme.Harsh_assignmentTheme

@Composable
fun CourseDetailsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailsScreen(
        uiState = uiState,
        onBack = onBack,
        onMarkCompleted = viewModel::markLessonCompleted,
        modifier = modifier,
    )
}

@Composable
fun CourseDetailsScreen(
    uiState: CourseDetailsUiState,
    onBack: () -> Unit,
    onMarkCompleted: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .safeDrawingPadding(),
    ) {
        DetailsTopBar(onBack = onBack)

        when (uiState) {
            CourseDetailsUiState.Loading -> DetailsLoading(Modifier.weight(1f))
            is CourseDetailsUiState.Error -> DetailsError(
                error = uiState.reason,
                onBack = onBack,
                modifier = Modifier.weight(1f),
            )

            is CourseDetailsUiState.Success -> DetailsContent(
                state = uiState,
                onMarkCompleted = onMarkCompleted,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.details_back),
            )
        }
        Text(
            text = stringResource(R.string.details_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailsContent(
    state: CourseDetailsUiState.Success,
    onMarkCompleted: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "course-summary") {
                CourseSummary(course = state.course)
            }

            state.completionError?.let {
                item(key = "completion-error") {
                    CompletionErrorNotice()
                }
            }

            item(key = "lesson-heading") {
                Text(
                    text = stringResource(R.string.details_lessons_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .semantics { heading() },
                )
            }

            if (state.course.lessons.isEmpty()) {
                item(key = "empty-lessons") {
                    EmptyLessons()
                }
            } else {
                items(
                    items = state.course.lessons.sortedBy(Lesson::order),
                    key = Lesson::id,
                ) { lesson ->
                    LessonItem(
                        lesson = lesson,
                        isCompleting = lesson.id in state.completingLessonIds,
                        onMarkCompleted = { onMarkCompleted(lesson.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseSummary(course: Course) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_instructor, course.instructor),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_progress, course.progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.dashboard_lesson_count,
                        course.lessonCount,
                        course.lessonCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { course.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(9.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun LessonItem(
    lesson: Lesson,
    isCompleting: Boolean,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completed = lesson.isCompleted
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (completed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (completed) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (completed) {
                            Icons.Outlined.Check
                        } else {
                            Icons.Outlined.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (completed) {
                        stringResource(R.string.details_completed)
                    } else {
                        stringResource(R.string.details_pending)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium,
                )
            }

            if (!completed) {
                FilledTonalButton(
                    onClick = onMarkCompleted,
                    enabled = !isCompleting,
                ) {
                    if (isCompleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.details_mark_complete))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionErrorNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
            Text(stringResource(R.string.details_completion_error))
        }
    }
}

@Composable
private fun EmptyLessons() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Text(
            text = stringResource(R.string.details_empty_lessons),
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun DetailsLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.details_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailsError(
    error: CourseDetailsError,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (error) {
        CourseDetailsError.INVALID_COURSE_ID -> stringResource(R.string.course_id_invalid)
        CourseDetailsError.COURSE_NOT_FOUND -> stringResource(R.string.details_course_not_found)
        CourseDetailsError.UNKNOWN -> stringResource(R.string.details_unknown_error)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 430.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(52.dp),
            )
            Text(
                text = stringResource(R.string.course_unavailable),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onBack) {
                Text(stringResource(R.string.details_return_to_courses))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CourseDetailsPreview() {
    val lessons = List(5) { index ->
        Lesson(
            id = (index + 1).toLong(),
            courseId = 1,
            title = "Lesson ${index + 1}",
            order = index,
            isCompleted = index < 2,
        )
    }
    Harsh_assignmentTheme {
        CourseDetailsScreen(
            uiState = CourseDetailsUiState.Success(
                Course(1, "Python Programming", "John Smith", lessons),
            ),
            onBack = {},
            onMarkCompleted = {},
        )
    }
}
