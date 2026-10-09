package com.example.harsh_assignment.domain.usecase

import javax.inject.Inject

class ValidateLoginCredentialsUseCase @Inject constructor() {

    operator fun invoke(
        email: String,
        password: String,
    ): LoginValidationResult {
        val normalizedEmail = email.trim()

        val emailError = when {
            normalizedEmail.isEmpty() -> EmailValidationError.REQUIRED
            !EMAIL_PATTERN.matches(normalizedEmail) -> EmailValidationError.INVALID
            else -> null
        }

        val passwordError = if (password.isBlank()) {
            PasswordValidationError.REQUIRED
        } else {
            null
        }

        return LoginValidationResult(
            normalizedEmail = normalizedEmail,
            emailError = emailError,
            passwordError = passwordError,
        )
    }

    private companion object {
        val EMAIL_PATTERN = Regex(
            pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            option = RegexOption.IGNORE_CASE,
        )
    }
}

data class LoginValidationResult(
    val normalizedEmail: String,
    val emailError: EmailValidationError?,
    val passwordError: PasswordValidationError?,
) {
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}

enum class EmailValidationError {
    REQUIRED,
    INVALID,
}

enum class PasswordValidationError {
    REQUIRED,
}
