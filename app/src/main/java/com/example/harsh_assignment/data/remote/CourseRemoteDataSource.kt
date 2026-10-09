package com.example.harsh_assignment.data.remote

import com.example.harsh_assignment.data.remote.model.CourseDto
import javax.inject.Inject

interface CourseRemoteDataSource {
    suspend fun getCourses(): List<CourseDto>
}

class RetrofitCourseRemoteDataSource @Inject constructor(
    private val courseApi: CourseApi,
) : CourseRemoteDataSource {
    override suspend fun getCourses(): List<CourseDto> = courseApi.getCourses().value
        ?: throw IllegalArgumentException("Course response is missing its value field.")
}
