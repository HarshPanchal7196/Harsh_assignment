package com.example.harsh_assignment.domain.repository

import com.example.harsh_assignment.domain.model.AuthenticationResult

interface AuthRepository {
    fun isLoggedIn(): Boolean

    suspend fun login(
        email: String,
        password: String,
    ): AuthenticationResult

    suspend fun logout()
}
