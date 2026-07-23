package com.example.fitness.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness.data.*
import com.example.fitness.service.WorkoutTimerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val fitnessDao = db.fitnessDao()

    val workoutHistory = fitnessDao.getAllSessions()
    val schedule = fitnessDao.getAllScheduleDays()
    val bodyMeasurements = fitnessDao.getAllMeasurements()
    val achievements = fitnessDao.getAllAchievements()

    val weeklyWorkouts = fitnessDao.getAllSessions().map { sessions ->
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        sessions.filter { it.startTime > oneWeekAgo }
    }

    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private var timerService: WorkoutTimerService? = null

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val elapsedTime: Flow<Long> = _isWorkoutActive.flatMapLatest { isActive ->
        if (isActive) {
            timerService?.elapsedTime ?: flowOf(0L)
        } else {
            flowOf(0L)
        }
    }

    val streak: StateFlow<Int> = workoutHistory.map { sessions ->
        calculateStreak(sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todaySchedule: Flow<ScheduleDay?> = schedule.map { days ->
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        days.find { it.day == dayOfWeek }
    }

    val isTodayCompleted: Flow<Boolean> = workoutHistory.map { sessions ->
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        sessions.any { it.date == today }
    }

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog = _showCompletionDialog.asStateFlow()

    private var pendingDuration: Long = 0
    private var pendingMuscleGroup: String = ""

    var todayWorkoutName: String = "Rest"
        private set

    fun setShowCompletionDialog(show: Boolean) {
        _showCompletionDialog.value = show
    }

    fun prepareCompletion(muscleGroup: String, duration: Long) {
        pendingMuscleGroup = muscleGroup
        pendingDuration = duration
    }

    fun getPendingMuscleGroup() = pendingMuscleGroup
    fun getPendingDuration() = pendingDuration

    fun updateTodayWorkoutName(name: String) {
        todayWorkoutName = name
    }

    private fun calculateStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val workedOutDays = sessions.map { sdf.format(Date(it.startTime)) }.distinct().sortedDescending()
        
        var currentStreak = 0
        val calendar = Calendar.getInstance()
        val today = sdf.format(calendar.time)
        
        // Check if user worked out today or yesterday to continue streak
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(calendar.time)
        
        if (workedOutDays.first() != today && workedOutDays.first() != yesterday) {
            return 0
        }
        
        val checkCalendar = Calendar.getInstance()
        if (workedOutDays.first() == yesterday) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        for (day in workedOutDays) {
            val expectedDay = sdf.format(checkCalendar.time)
            if (day == expectedDay) {
                currentStreak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return currentStreak
    }

    fun setTimerService(service: WorkoutTimerService?) {
        timerService = service
        service?.let {
            _isWorkoutActive.value = it.isWorkoutRunning
        }
    }

    fun setWorkoutActive(active: Boolean) {
        _isWorkoutActive.value = active
    }

    fun saveWorkoutSession(
        muscleGroup: String,
        durationMillis: Long,
        weight: Float,
        exercises: Int,
        supplements: List<String>
    ) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - durationMillis
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val session = WorkoutSession(
            workoutType = "Session",
            muscleGroup = muscleGroup,
            exercisesCount = exercises,
            estimatedDurationMinutes = (durationMillis / 60000).toInt(),
            weight = weight,
            supplements = supplements.joinToString(", "),
            startTime = startTime,
            endTime = endTime,
            durationMillis = durationMillis,
            date = date
        )

        viewModelScope.launch {
            fitnessDao.insertSession(session)
            _isWorkoutActive.value = false
            _showCompletionDialog.value = false
        }
    }

    fun updateScheduleDay(day: ScheduleDay) {
        viewModelScope.launch {
            fitnessDao.insertScheduleDay(day)
        }
    }

    fun addMeasurement(weight: Float, height: Float) {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val measurement = BodyMeasurement(
            date = date,
            timestamp = System.currentTimeMillis(),
            weight = weight,
            height = height
        )
        viewModelScope.launch {
            fitnessDao.insertMeasurement(measurement)
        }
    }
}
