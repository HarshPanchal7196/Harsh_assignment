package com.example.harsh_assignment.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harsh_assignment.domain.model.Course
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.repository.CourseRefreshException
import com.example.harsh_assignment.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _logoutUiState = MutableStateFlow(LogoutUiState())
    val logoutUiState: StateFlow<LogoutUiState> = _logoutUiState.asStateFlow()

    private val logoutEventChannel = Channel<Unit>(capacity = Channel.BUFFERED)
    val logoutEvents = logoutEventChannel.receiveAsFlow()

    private var latestCourses: List<Course> = emptyList()
    private var isRefreshing: Boolean = true
    private var refreshError: DashboardError? = null
    private var refreshJob: Job? = null
    private var logoutJob: Job? = null

    init {
        observeCourses()
        refreshCourses()
    }

    fun retry() {
        if (_logoutUiState.value.isLoggingOut) return
        refreshCourses()
    }

    fun logout() {
        if (logoutJob?.isActive == true) return

        _logoutUiState.value = LogoutUiState(isLoggingOut = true)
        logoutJob = viewModelScope.launch {
            try {
                refreshJob?.cancelAndJoin()
                courseRepository.clearCachedCourses()
                authRepository.logout()
                logoutEventChannel.send(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _logoutUiState.value = LogoutUiState(hasError = true)
            } finally {
                if (!_logoutUiState.value.hasError) {
                    _logoutUiState.value = LogoutUiState()
                }
            }
        }
    }

    private fun observeCourses() {
        viewModelScope.launch {
            courseRepository.observeCourses()
                .catch {
                    refreshError = DashboardError.UNKNOWN
                    isRefreshing = false
                    publishState()
                }
                .collect { courses ->
                    latestCourses = courses
                    publishState()
                }
        }
    }

    private fun refreshCourses() {
        if (refreshJob?.isActive == true) return

        isRefreshing = true
        refreshError = null
        publishState()

        refreshJob = viewModelScope.launch {
            try {
                courseRepository.refreshCourses()
                refreshError = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: CourseRefreshException) {
                refreshError = error.toDashboardError()
            } catch (_: Throwable) {
                refreshError = DashboardError.UNKNOWN
            } finally {
                isRefreshing = false
                publishState()
            }
        }
    }

    private fun publishState() {
        _uiState.value = when {
            latestCourses.isNotEmpty() -> DashboardUiState.Success(
                courses = latestCourses,
                isRefreshing = isRefreshing,
                isShowingCachedData = refreshError != null,
            )

            isRefreshing -> DashboardUiState.Loading
            refreshError != null -> DashboardUiState.Error(requireNotNull(refreshError))
            else -> DashboardUiState.Empty
        }
    }

    private fun CourseRefreshException.toDashboardError(): DashboardError = when (this) {
        is CourseRefreshException.Network -> DashboardError.NETWORK
        is CourseRefreshException.InvalidData -> DashboardError.INVALID_DATA
        is CourseRefreshException.NotAuthenticated -> DashboardError.UNKNOWN
        is CourseRefreshException.Unknown -> DashboardError.UNKNOWN
    }
}
