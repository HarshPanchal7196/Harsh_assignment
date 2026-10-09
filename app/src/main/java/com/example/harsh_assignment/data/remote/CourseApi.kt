package com.example.harsh_assignment.data.remote

import com.example.harsh_assignment.data.remote.model.CoursesResponseDto
import retrofit2.http.GET

interface CourseApi {
    @GET("m/GPS6SAvUn7WF")
    suspend fun getCourses(): CoursesResponseDto
}
