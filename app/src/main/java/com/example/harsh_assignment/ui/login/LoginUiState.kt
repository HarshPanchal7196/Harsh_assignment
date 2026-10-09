package com.example.harsh_assignment.ui.login

import com.example.harsh_assignment.domain.usecase.EmailValidationError
import com.example.harsh_assignment.domain.usecase.PasswordValidationError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailValidationError? = null,
    val passwordError: PasswordValidationError? = null,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val isLoggedIn: Boolean = false,
)

enum class LoginError {
    SERVICE_UNAVAILABLE,
    UNEXPECTED,
}
