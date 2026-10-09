package com.example.harsh_assignment.ui.dashboard

import com.example.harsh_assignment.domain.model.Course

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState

    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean = false,
        val isShowingCachedData: Boolean = false,
    ) : DashboardUiState

    data class Error(
        val reason: DashboardError,
    ) : DashboardUiState
}

enum class DashboardError {
    NETWORK,
    INVALID_DATA,
    UNKNOWN,
}

data class LogoutUiState(
    val isLoggingOut: Boolean = false,
    val hasError: Boolean = false,
)
