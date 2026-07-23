package com.example.fitness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Insert
    suspend fun insertSession(session: WorkoutSession)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    // Body Measurements
    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurement>>

    @Insert
    suspend fun insertMeasurement(measurement: BodyMeasurement)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    // Schedule
    @Query("SELECT * FROM schedule_days")
    fun getAllScheduleDays(): Flow<List<ScheduleDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleDay(day: ScheduleDay)
}
