package com.example.harsh_assignment.ui

import com.example.harsh_assignment.domain.model.AuthenticationResult
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.ui.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSessionViewModelTest {

    @Test
    fun `logged in session starts at dashboard`() {
        val viewModel = AppSessionViewModel(FakeAuthRepository(loggedIn = true))

        assertEquals(AppDestination.Dashboard.route, viewModel.startDestination)
    }

    @Test
    fun `logged out session starts at login`() {
        val viewModel = AppSessionViewModel(FakeAuthRepository(loggedIn = false))

        assertEquals(AppDestination.Login.route, viewModel.startDestination)
    }

    private class FakeAuthRepository(
        private var loggedIn: Boolean,
    ) : AuthRepository {
        override fun isLoggedIn(): Boolean = loggedIn

        override suspend fun login(email: String, password: String): AuthenticationResult {
            loggedIn = true
            return AuthenticationResult.Success
        }

        override suspend fun logout() {
            loggedIn = false
        }
    }
}
