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

data class WeightStats(
    val highestWeight: Float = 0f,
    val highestGain: Float = 0f,
    val highestLoss: Float = 0f,
    val lastWeight: Float = 0f
)

data class WeightFeedback(
    val difference: Float,
    val isPositiveProgress: Boolean,
    val message: String
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val fitnessDao = db.fitnessDao()
    private val weightPrefs = WeightPrefs(application)

    val workoutHistory = fitnessDao.getAllSessions()
    val schedule = fitnessDao.getAllScheduleDays()
    val bodyMeasurements = fitnessDao.getAllMeasurements()
    val achievements = fitnessDao.getAllAchievements()

    private val _isBulking = MutableStateFlow(weightPrefs.isBulking)
    val isBulking: StateFlow<Boolean> = _isBulking.asStateFlow()

    fun setBulking(value: Boolean) {
        weightPrefs.isBulking = value
        _isBulking.value = value
    }

    val weightStats: StateFlow<WeightStats> = workoutHistory.map { sessions ->
        calculateWeightStats(sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightStats())

    private val _weightFeedback = MutableStateFlow<WeightFeedback?>(null)
    val weightFeedback: StateFlow<WeightFeedback?> = _weightFeedback.asStateFlow()

    fun clearWeightFeedback() {
        _weightFeedback.value = null
    }

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

    private fun calculateWeightStats(sessions: List<WorkoutSession>): WeightStats {
        if (sessions.isEmpty()) return WeightStats()

        val weights = sessions.filter { it.weight > 0 }.sortedBy { it.startTime }
        if (weights.isEmpty()) return WeightStats()

        var highestWeight = 0f
        var highestGain = 0f
        var highestLoss = 0f

        weights.forEachIndexed { index, current ->
            if (current.weight > highestWeight) highestWeight = current.weight
            
            if (index > 0) {
                val prev = weights[index - 1]
                val diff = current.weight - prev.weight
                if (diff > highestGain) highestGain = diff
                if (diff < highestLoss) highestLoss = diff
            }
        }

        return WeightStats(
            highestWeight = highestWeight,
            highestGain = highestGain,
            highestLoss = kotlin.math.abs(highestLoss),
            lastWeight = weights.last().weight
        )
    }

    private fun calculateStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0
        
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val workedOutDays = sessions.map { sdf.format(Date(it.startTime)) }.distinct().sortedDescending()
        
        var currentStreak = 0
        val calendar = Calendar.getInstance()
        val today = sdf.format(calendar.time)
        
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
        if (active) {
            _weightFeedback.value = null
        }
    }

    fun saveWorkoutSession(
        muscleGroup: String,
        durationMillis: Long,
        weight: Float,
        exercises: Int,
        supplements: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val history = fitnessDao.getAllSessions().first()
            val lastSession = history.filter { it.weight > 0 }.maxByOrNull { it.startTime }
            
            if (lastSession != null && weight > 0) {
                val diff = weight - lastSession.weight
                val isBulkingMode = _isBulking.value
                val isPositive = if (isBulkingMode) diff > 0 else diff < 0
                
                val formattedDiff = if (kotlin.math.abs(diff) < 1) {
                    "${(kotlin.math.abs(diff) * 1000).toInt()} g"
                } else {
                    String.format(Locale.getDefault(), "%.2f kg", kotlin.math.abs(diff))
                }

                val msg = if (diff == 0f) {
                    "Weight remains the same."
                } else if (isBulkingMode) {
                    if (diff > 0) "🎉 You have gained $formattedDiff!" else "You have lost $formattedDiff."
                } else {
                    if (diff < 0) "🎉 You have lost $formattedDiff!" else "You have gained $formattedDiff."
                }

                _weightFeedback.value = WeightFeedback(diff, isPositive, msg)
            }

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
                date = date,
                notes = if (notes.isBlank()) null else notes
            )

            fitnessDao.insertSession(session)
            _isWorkoutActive.value = false
            if (_weightFeedback.value == null) {
                _showCompletionDialog.value = false
            }
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
