package com.example.harsh_assignment.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CourseWithLessons(
    @Embedded val course: CourseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId",
    )
    val lessons: List<LessonEntity>,
)
