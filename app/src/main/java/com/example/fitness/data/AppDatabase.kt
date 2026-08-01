package com.example.fitness.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [WorkoutSession::class, BodyMeasurement::class, Achievement::class, ScheduleDay::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .addCallback(DatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDefaultSchedule(database.fitnessDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        val dao = database.fitnessDao()
                        // Ensure we have data even if migrations happened or it's a fresh install
                        if (dao.getAllScheduleDays().first().isEmpty()) {
                            populateDefaultSchedule(dao)
                        }
                    }
                }
            }

            suspend fun populateDefaultSchedule(dao: FitnessDao) {
                val days = listOf(
                    ScheduleDay("Monday", "Chest", "Triceps", 45),
                    ScheduleDay("Tuesday", "Back", "Biceps", 45),
                    ScheduleDay("Wednesday", "Legs", "Shoulders", 60),
                    ScheduleDay("Thursday", "Chest", "Triceps", 45),
                    ScheduleDay("Friday", "Back", "Biceps", 45),
                    ScheduleDay("Saturday", "Cardio", "Abs", 30),
                    ScheduleDay("Sunday", "Rest", "", 0)
                )
                days.forEach { dao.insertScheduleDay(it) }
            }
        }
    }
}
