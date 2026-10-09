package com.example.harsh_assignment.data.repository

import android.content.Context
import com.example.harsh_assignment.domain.model.AuthenticationResult
import com.example.harsh_assignment.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay

/** Mock authentication required by the assignment. Validated credentials always succeed. */
class FakeAuthRepository @Inject constructor(
    @ApplicationContext context: Context,
) : AuthRepository {

    private val preferences = context.getSharedPreferences(
        SESSION_PREFERENCES,
        Context.MODE_PRIVATE,
    )

    override fun isLoggedIn(): Boolean = preferences.getBoolean(LOGGED_IN_KEY, false)

    override suspend fun login(
        email: String,
        password: String,
    ): AuthenticationResult {
        delay(SIMULATED_NETWORK_DELAY_MILLIS)
        preferences.edit().putBoolean(LOGGED_IN_KEY, true).apply()
        return AuthenticationResult.Success
    }

    override suspend fun logout() {
        preferences.edit().remove(LOGGED_IN_KEY).apply()
    }

    private companion object {
        const val SIMULATED_NETWORK_DELAY_MILLIS = 750L
        const val SESSION_PREFERENCES = "authentication_session"
        const val LOGGED_IN_KEY = "is_logged_in"
    }
}
