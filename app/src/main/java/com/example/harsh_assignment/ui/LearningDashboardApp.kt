package com.example.harsh_assignment.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.harsh_assignment.ui.navigation.AppNavHost

/**
 * Owns app-level navigation state. rememberNavController integrates with SavedStateRegistry, so
 * the active destination and back stack are restored across configuration and process recreation.
 */
@Composable
fun LearningDashboardApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: AppSessionViewModel = hiltViewModel(),
) {
    AppNavHost(
        navController = navController,
        startDestination = sessionViewModel.startDestination,
        modifier = modifier,
    )
}
