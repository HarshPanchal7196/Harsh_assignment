package com.example.harsh_assignment.ui

import androidx.lifecycle.ViewModel
import com.example.harsh_assignment.domain.repository.AuthRepository
import com.example.harsh_assignment.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val startDestination: String = if (authRepository.isLoggedIn()) {
        AppDestination.Dashboard.route
    } else {
        AppDestination.Login.route
    }
}
