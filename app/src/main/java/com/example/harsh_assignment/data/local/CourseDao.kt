package com.example.harsh_assignment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.harsh_assignment.data.local.entity.CourseEntity
import com.example.harsh_assignment.data.local.entity.CourseWithLessons
import com.example.harsh_assignment.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {

    @Transaction
    @Query("SELECT * FROM courses ORDER BY position ASC")
    abstract fun observeCourses(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    abstract fun observeCourse(courseId: Long): Flow<CourseWithLessons?>

    @Query("SELECT id FROM lessons WHERE isCompleted = 1")
    protected abstract suspend fun completedLessonIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons")
    protected abstract suspend fun deleteLessons()

    @Query("DELETE FROM courses")
    protected abstract suspend fun deleteCourses()

    @Transaction
    open suspend fun clearCourses() {
        deleteLessons()
        deleteCourses()
    }

    @Query("UPDATE lessons SET isCompleted = 1 WHERE id = :lessonId AND isCompleted = 0")
    abstract suspend fun markLessonCompleted(lessonId: Long): Int

    /**
     * Replaces one complete remote snapshot atomically. Existing local completion wins over a
     * stale remote value, so refreshing cannot undo progress made by the learner.
     */
    @Transaction
    open suspend fun replaceCourses(
        courses: List<CourseEntity>,
        lessons: List<LessonEntity>,
    ) {
        val completedIds = completedLessonIds().toHashSet()
        val mergedLessons = lessons.map { lesson ->
            if (lesson.id in completedIds) lesson.copy(isCompleted = true) else lesson
        }

        clearCourses()
        if (courses.isNotEmpty()) insertCourses(courses)
        if (mergedLessons.isNotEmpty()) insertLessons(mergedLessons)
    }
}
