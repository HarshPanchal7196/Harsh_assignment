package com.example.harsh_assignment.domain.model

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult

    data class Failure(
        val reason: AuthenticationFailureReason,
    ) : AuthenticationResult
}

enum class AuthenticationFailureReason {
    SERVICE_UNAVAILABLE,
}
