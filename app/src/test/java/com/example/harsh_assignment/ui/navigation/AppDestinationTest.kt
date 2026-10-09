package com.example.harsh_assignment.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AppDestinationTest {

    @Test
    fun `course details route contains only the course id`() {
        assertEquals("course/42", AppDestination.CourseDetails.createRoute(42))
    }

    @Test
    fun `course details route rejects an invalid course id`() {
        assertThrows(IllegalArgumentException::class.java) {
            AppDestination.CourseDetails.createRoute(0)
        }
    }
}
