package com.example.fitness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_days")
data class ScheduleDay(
    @PrimaryKey val day: String, // e.g., "Monday"
    val muscle1: String,
    val muscle2: String,
    val durationMinutes: Int
)
