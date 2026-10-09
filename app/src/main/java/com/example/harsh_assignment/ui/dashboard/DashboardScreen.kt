package com.example.harsh_assignment.ui.dashboard

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun DashboardRoute(
    onCourseSelected: (Long) -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logoutUiState by viewModel.logoutUiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.logoutEvents.collect { onLoggedOut() }
    }

    DashboardScreen(
        uiState = uiState,
        logoutUiState = logoutUiState,
        onCourseSelected = onCourseSelected,
        onRetry = viewModel::retry,
        onLogout = viewModel::logout,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    logoutUiState: LogoutUiState,
    onCourseSelected: (Long) -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
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
        DashboardHeader(
            isLoggingOut = logoutUiState.isLoggingOut,
            onLogout = onLogout,
        )

        if (logoutUiState.hasError) {
            LogoutErrorNotice(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }

        when (uiState) {
            DashboardUiState.Loading -> DashboardLoading(
                modifier = Modifier.weight(1f),
            )

            DashboardUiState.Empty -> DashboardEmpty(
                modifier = Modifier.weight(1f),
            )

            is DashboardUiState.Error -> DashboardFailure(
                error = uiState.reason,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )

            is DashboardUiState.Success -> DashboardCourseGrid(
                state = uiState,
                onCourseSelected = onCourseSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 1200.dp)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_eyebrow),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        IconButton(
            onClick = onLogout,
            enabled = !isLoggingOut,
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = stringResource(R.string.dashboard_logout),
                )
            }
        }
    }
}

@Composable
private fun LogoutErrorNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
            Text(stringResource(R.string.dashboard_logout_error))
        }
    }
}

@Composable
private fun DashboardCourseGrid(
    state: DashboardUiState.Success,
    onCourseSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (state.isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (state.isShowingCachedData) {
            CachedDataNotice(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = state.courses,
                key = Course::id,
            ) { course ->
                CourseCard(
                    course = course,
                    onContinue = { onCourseSelected(course.id) },
                )
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = stringResource(R.string.dashboard_course_icon),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_instructor, course.instructor),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_progress, course.progress),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.dashboard_lesson_count,
                            course.lessonCount,
                            course.lessonCount,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { course.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(stringResource(R.string.dashboard_continue))
            }
        }
    }
}

@Composable
private fun CachedDataNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.dashboard_cached_notice),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DashboardLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.dashboard_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DashboardEmpty(modifier: Modifier = Modifier) {
    DashboardMessage(
        icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
        title = stringResource(R.string.dashboard_empty_title),
        message = stringResource(R.string.dashboard_empty_message),
        modifier = modifier,
    )
}

@Composable
private fun DashboardFailure(
    error: DashboardError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when (error) {
        DashboardError.NETWORK -> stringResource(R.string.dashboard_network_error)
        DashboardError.INVALID_DATA -> stringResource(R.string.dashboard_data_error)
        DashboardError.UNKNOWN -> stringResource(R.string.dashboard_unknown_error)
    }
    DashboardMessage(
        icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null) },
        title = stringResource(R.string.dashboard_error_title),
        message = message,
        action = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.dashboard_retry))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun DashboardMessage(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) { icon() }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            action?.invoke()
        }
    }
}

private fun previewCourse(
    id: Long,
    title: String,
    instructor: String,
    completed: Int,
): Course = Course(
    id = id,
    title = title,
    instructor = instructor,
    lessons = List(10) { index ->
        Lesson(
            id = id * 100 + index,
            courseId = id,
            title = "Lesson ${index + 1}",
            order = index,
            isCompleted = index < completed,
        )
    },
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DashboardPreview() {
    Harsh_assignmentTheme {
        DashboardScreen(
            uiState = DashboardUiState.Success(
                courses = listOf(
                    previewCourse(1, "Python Programming", "John Smith", 6),
                    previewCourse(2, "Generative AI", "Sarah Williams", 4),
                ),
            ),
            logoutUiState = LogoutUiState(),
            onCourseSelected = {},
            onRetry = {},
            onLogout = {},
        )
    }
}
