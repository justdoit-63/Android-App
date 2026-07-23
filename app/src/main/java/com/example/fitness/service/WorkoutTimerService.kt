package com.example.fitness.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.fitness.MainActivity
import com.example.fitness.data.AppDatabase
import com.example.fitness.data.WorkoutSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class WorkoutTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private var startTime = 0L
    private var targetDurationMillis = 0L
    private var muscleGroup = "Unknown"
    private var alarmTriggered = false
    
    private var _isWorkoutRunning = false
    val isWorkoutRunning: Boolean get() = _isWorkoutRunning

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutTimerService = this@WorkoutTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                muscleGroup = intent.getStringExtra(EXTRA_MUSCLE_GROUP) ?: "Unknown"
                val durationMin = intent.getIntExtra(EXTRA_DURATION_MIN, 0)
                targetDurationMillis = durationMin * 60000L
                startWorkout()
            }
            ACTION_STOP -> stopWorkout()
        }
        return START_STICKY
    }

    private fun startWorkout() {
        startTime = System.currentTimeMillis()
        _isWorkoutRunning = true
        alarmTriggered = false
        startForeground(NOTIFICATION_ID, createNotification("Workout Active", "Duration: 00:00:00"))

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                _elapsedTime.value = elapsed
                updateNotification("Workout Active", "Duration: ${formatTime(elapsed)}")
                
                if (targetDurationMillis > 0 && elapsed >= targetDurationMillis && !alarmTriggered) {
                    onDurationReached()
                    alarmTriggered = true
                }
                
                delay(1000)
            }
        }
    }

    private fun stopWorkout() {
        if (!_isWorkoutRunning) return
        _isWorkoutRunning = false
        
        timerJob?.cancel()
        sendBroadcast(Intent(ACTION_WORKOUT_STOPPED))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onDurationReached() {
        vibrate()
        updateNotification("Duration Reached!", "Scheduled workout time is up!")
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun updateNotification(title: String, text: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, text))
    }

    private fun createNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Target MainActivity for "End Workout"
        val stopIntent = Intent(this, MainActivity::class.java).apply { 
            action = ACTION_END_WORKOUT_FROM_NOTIF 
            putExtra(EXTRA_MUSCLE_GROUP, muscleGroup)
            putExtra(EXTRA_ELAPSED_TIME, _elapsedTime.value)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val stopPendingIntent = PendingIntent.getActivity(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Workout", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Timer Channel",
            NotificationManager.IMPORTANCE_HIGH // High for sound/heads-up
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_END_WORKOUT_FROM_NOTIF = "com.example.fitness.END_WORKOUT_FROM_NOTIF"
        const val ACTION_WORKOUT_STOPPED = "com.example.fitness.WORKOUT_STOPPED"
        const val EXTRA_MUSCLE_GROUP = "EXTRA_MUSCLE_GROUP"
        const val EXTRA_DURATION_MIN = "EXTRA_DURATION_MIN"
        const val EXTRA_ELAPSED_TIME = "EXTRA_ELAPSED_TIME"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "workout_timer_channel"
    }
}
