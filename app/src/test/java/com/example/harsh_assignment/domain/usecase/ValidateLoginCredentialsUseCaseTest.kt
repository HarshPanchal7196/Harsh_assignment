package com.example.harsh_assignment.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateLoginCredentialsUseCaseTest {

    private val validate = ValidateLoginCredentialsUseCase()

    @Test
    fun `valid credentials are accepted and email is normalized`() {
        val result = validate(
            email = "  learner@example.com  ",
            password = "password",
        )

        assertTrue(result.isValid)
        assertEquals("learner@example.com", result.normalizedEmail)
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }

    @Test
    fun `empty credentials report both required errors`() {
        val result = validate(email = "  ", password = "  ")

        assertFalse(result.isValid)
        assertEquals(EmailValidationError.REQUIRED, result.emailError)
        assertEquals(PasswordValidationError.REQUIRED, result.passwordError)
    }

    @Test
    fun `malformed email is rejected`() {
        val result = validate(email = "learner.example.com", password = "password")

        assertFalse(result.isValid)
        assertEquals(EmailValidationError.INVALID, result.emailError)
        assertNull(result.passwordError)
    }
}
