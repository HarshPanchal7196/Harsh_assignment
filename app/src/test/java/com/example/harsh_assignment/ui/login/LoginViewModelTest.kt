package com.example.harsh_assignment.ui.login

import androidx.lifecycle.SavedStateHandle
import com.example.harsh_assignment.MainDispatcherRule
import com.example.harsh_assignment.domain.model.AuthenticationFailureReason
import com.example.harsh_assignment.domain.model.AuthenticationResult
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.usecase.EmailValidationError
import com.example.harsh_assignment.domain.usecase.PasswordValidationError
import com.example.harsh_assignment.domain.usecase.ValidateLoginCredentialsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `invalid inputs expose field errors without calling repository`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = TestAuthRepository { AuthenticationResult.Success }
        val viewModel = createViewModel(repository)

        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged(" ")
        viewModel.login()

        assertEquals(EmailValidationError.INVALID, viewModel.uiState.value.emailError)
        assertEquals(PasswordValidationError.REQUIRED, viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `valid login exposes loading ignores duplicate submit and then succeeds`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val response = CompletableDeferred<AuthenticationResult>()
        val repository = TestAuthRepository { response.await() }
        val viewModel = createViewModel(repository)
        viewModel.onEmailChanged(" learner@example.com ")
        viewModel.onPasswordChanged("password")

        viewModel.login()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("learner@example.com", viewModel.uiState.value.email)
        assertEquals(1, repository.loginCalls)

        viewModel.login()
        runCurrent()
        assertEquals(1, repository.loginCalls)

        response.complete(AuthenticationResult.Success)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `repository failure becomes recoverable login error`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = TestAuthRepository {
            AuthenticationResult.Failure(AuthenticationFailureReason.SERVICE_UNAVAILABLE)
        }
        val viewModel = createViewModel(repository)
        viewModel.onEmailChanged("learner@example.com")
        viewModel.onPasswordChanged("password")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LoginError.SERVICE_UNAVAILABLE, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `unexpected repository exception does not leave screen loading`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val repository = TestAuthRepository { error("Unexpected failure") }
        val viewModel = createViewModel(repository)
        viewModel.onEmailChanged("learner@example.com")
        viewModel.onPasswordChanged("password")

        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LoginError.UNEXPECTED, viewModel.uiState.value.error)
    }

    @Test
    fun `saved email is restored but password is not persisted`() {
        val viewModel = createViewModel(
            repository = TestAuthRepository { AuthenticationResult.Success },
            savedStateHandle = SavedStateHandle(mapOf("login_email" to "saved@example.com")),
        )

        assertEquals("saved@example.com", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun `navigation acknowledgement consumes login success`() = runTest(
        mainDispatcherRule.testDispatcher,
    ) {
        val viewModel = createViewModel(
            repository = TestAuthRepository { AuthenticationResult.Success },
        )
        viewModel.onEmailChanged("learner@example.com")
        viewModel.onPasswordChanged("password")
        viewModel.login()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoggedIn)

        viewModel.onLoginNavigationHandled()

        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    private fun createViewModel(
        repository: AuthRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): LoginViewModel = LoginViewModel(
        authRepository = repository,
        validateCredentials = ValidateLoginCredentialsUseCase(),
        savedStateHandle = savedStateHandle,
    )

    private class TestAuthRepository(
        private val response: suspend () -> AuthenticationResult,
    ) : AuthRepository {
        var loginCalls: Int = 0
            private set

        override fun isLoggedIn(): Boolean = false

        override suspend fun login(
            email: String,
            password: String,
        ): AuthenticationResult {
            loginCalls += 1
            return response()
        }

        override suspend fun logout() = Unit
    }
}
