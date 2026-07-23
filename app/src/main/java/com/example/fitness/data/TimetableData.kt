package com.example.fitness.data

data class WorkoutType(
    val day: String,
    val muscleGroup: String
)

object TimetableData {
    val schedule = listOf(
        WorkoutType("Monday", "Chest & Triceps"),
        WorkoutType("Tuesday", "Back & Biceps"),
        WorkoutType("Wednesday", "Legs & Shoulder"),
        WorkoutType("Thursday", "Chest & Triceps"),
        WorkoutType("Friday", "Back & Biceps"),
        WorkoutType("Saturday", "Cardio"),
        WorkoutType("Sunday", "Rest")
    )
}
