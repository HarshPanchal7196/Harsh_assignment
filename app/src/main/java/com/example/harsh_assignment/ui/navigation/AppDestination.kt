package com.example.harsh_assignment.ui.navigation

sealed interface AppDestination {
    val route: String

    data object Login : AppDestination {
        override val route: String = "login"
    }

    data object Dashboard : AppDestination {
        override val route: String = "dashboard"
    }

    data object CourseDetails : AppDestination {
        const val COURSE_ID_ARGUMENT: String = "courseId"

        override val route: String = "course/{$COURSE_ID_ARGUMENT}"

        fun createRoute(courseId: Long): String {
            require(courseId > 0) { "Course id must be positive." }
            return "course/$courseId"
        }
    }
}
