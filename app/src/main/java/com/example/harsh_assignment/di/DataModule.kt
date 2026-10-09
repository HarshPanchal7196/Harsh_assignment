package com.example.harsh_assignment.di

import android.content.Context
import androidx.room.Room
import com.example.harsh_assignment.data.local.CourseDao
import com.example.harsh_assignment.data.local.LearningDatabase
import com.example.harsh_assignment.data.remote.CourseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LearningDatabase = Room.databaseBuilder(
        context = context,
        klass = LearningDatabase::class.java,
        name = "learning-dashboard.db",
    ).build()

    @Provides
    fun provideCourseDao(database: LearningDatabase): CourseDao = database.courseDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://quickmock.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideCourseApi(retrofit: Retrofit): CourseApi = retrofit.create(CourseApi::class.java)
}
