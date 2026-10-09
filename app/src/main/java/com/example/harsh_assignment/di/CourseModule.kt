package com.example.harsh_assignment.di

import com.example.harsh_assignment.data.local.CourseLocalDataSource
import com.example.harsh_assignment.data.local.RoomCourseLocalDataSource
import com.example.harsh_assignment.data.remote.CourseRemoteDataSource
import com.example.harsh_assignment.data.remote.RetrofitCourseRemoteDataSource
import com.example.harsh_assignment.data.repository.OfflineFirstCourseRepository
import com.example.harsh_assignment.domain.repository.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CourseModule {

    @Binds
    @Singleton
    abstract fun bindCourseLocalDataSource(
        implementation: RoomCourseLocalDataSource,
    ): CourseLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCourseRemoteDataSource(
        implementation: RetrofitCourseRemoteDataSource,
    ): CourseRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        implementation: OfflineFirstCourseRepository,
    ): CourseRepository
}
