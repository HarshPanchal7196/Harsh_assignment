package com.example.harsh_assignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.harsh_assignment.data.local.entity.CourseEntity
import com.example.harsh_assignment.data.local.entity.LessonEntity

@Database(
    entities = [CourseEntity::class, LessonEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LearningDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}
