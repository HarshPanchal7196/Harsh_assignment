package com.example.harsh_assignment.ui.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harsh_assignment.domain.model.AuthenticationFailureReason
import com.example.harsh_assignment.domain.model.AuthenticationResult
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.domain.usecase.ValidateLoginCredentialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateCredentials: ValidateLoginCredentialsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(email = savedStateHandle.get<String>(EMAIL_KEY).orEmpty()),
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        if (_uiState.value.isLoading) return

        savedStateHandle[EMAIL_KEY] = email
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                error = null,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                error = null,
            )
        }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoggedIn) return

        val validation = validateCredentials(
            email = currentState.email,
            password = currentState.password,
        )

        savedStateHandle[EMAIL_KEY] = validation.normalizedEmail
        _uiState.update {
            it.copy(
                email = validation.normalizedEmail,
                emailError = validation.emailError,
                passwordError = validation.passwordError,
                error = null,
            )
        }

        if (!validation.isValid) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                when (
                    val result = authRepository.login(
                        email = validation.normalizedEmail,
                        password = currentState.password,
                    )
                ) {
                    AuthenticationResult.Success -> {
                        _uiState.update {
                            it.copy(
                                password = "",
                                isLoading = false,
                                isLoggedIn = true,
                            )
                        }
                    }

                    is AuthenticationResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.reason.toLoginError(),
                            )
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = LoginError.UNEXPECTED,
                    )
                }
            }
        }
    }

    fun onLoginNavigationHandled() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    private fun AuthenticationFailureReason.toLoginError(): LoginError = when (this) {
        AuthenticationFailureReason.SERVICE_UNAVAILABLE -> LoginError.SERVICE_UNAVAILABLE
    }

    private companion object {
        const val EMAIL_KEY = "login_email"
    }
}
