package com.example.fitness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutType: String,
    val muscleGroup: String,
    val exercisesCount: Int,
    val estimatedDurationMinutes: Int,
    val weight: Float,
    val supplements: String,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val date: String,
    val notes: String? = null
)
