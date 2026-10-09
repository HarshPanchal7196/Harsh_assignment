package com.example.harsh_assignment.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.harsh_assignment.ui.dashboard.DashboardRoute
import com.example.harsh_assignment.ui.details.CourseDetailsRoute
import com.example.harsh_assignment.ui.login.LoginRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppDestination.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppDestination.Dashboard.route) {
            DashboardRoute(
                onCourseSelected = { courseId ->
                    navController.navigate(AppDestination.CourseDetails.createRoute(courseId))
                },
                onLoggedOut = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = AppDestination.CourseDetails.route,
            arguments = listOf(
                navArgument(AppDestination.CourseDetails.COURSE_ID_ARGUMENT) {
                    type = NavType.LongType
                },
            ),
        ) {
            CourseDetailsRoute(
                onBack = navController::navigateUp,
            )
        }
    }
}
