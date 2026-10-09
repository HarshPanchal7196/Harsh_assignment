package com.example.harsh_assignment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val instructor: String,
    val position: Int,
)
