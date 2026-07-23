package com.example.fitness

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitness.service.WorkoutTimerService
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.screens.*
import com.example.fitness.ui.theme.FitnessTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FitnessViewModel by viewModels()

    private var timerService: WorkoutTimerService? = null
    private var isBound = false

    private val workoutStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WorkoutTimerService.ACTION_WORKOUT_STOPPED) {
                viewModel.setWorkoutActive(false)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WorkoutTimerService.LocalBinder
            val s = binder.getService()
            timerService = s
            isBound = true
            viewModel.setTimerService(s)
            // Sync state on connection
            viewModel.setWorkoutActive(s.isWorkoutRunning)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            viewModel.setTimerService(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        handleIntent(intent)
        setContent {
            FitnessTheme(darkTheme = true) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showCompletionDialog by viewModel.showCompletionDialog.collectAsState()

                Scaffold(
                    bottomBar = {
                        if (currentDestination?.route != "achievements" && !showCompletionDialog) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Home") },
                                    selected = currentDestination?.route == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text("Schedule") },
                                    selected = currentDestination?.route == "schedule",
                                    onClick = { navController.navigate("schedule") }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            Icons.Default.BarChart,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text("Stats") },
                                    selected = currentDestination?.route == "stats",
                                    onClick = { navController.navigate("stats") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                                    label = { Text("History") },
                                    selected = currentDestination?.route == "history",
                                    onClick = { navController.navigate("history") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onStartService = { muscleGroup, duration -> startWorkoutService(muscleGroup, duration) },
                                onStopService = { triggerWorkoutCompletion() }
                            )
                        }
                        composable("schedule") {
                            ScheduleScreen(viewModel = viewModel)
                        }
                        composable("stats") {
                            StatsScreen(
                                viewModel = viewModel,
                                onNavigateToAchievements = { navController.navigate("achievements") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(viewModel = viewModel)
                        }
                        composable("achievements") {
                            AchievementsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    if (showCompletionDialog) {
                        WorkoutCompletionDialog(
                            viewModel = viewModel,
                            onSave = { weight, exercises, supplements ->
                                viewModel.saveWorkoutSession(
                                    muscleGroup = viewModel.getPendingMuscleGroup(),
                                    durationMillis = viewModel.getPendingDuration(),
                                    weight = weight,
                                    exercises = exercises,
                                    supplements = supplements
                                )
                                stopWorkoutService()
                            },
                            onDiscard = {
                                viewModel.setShowCompletionDialog(false)
                                stopWorkoutService()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == WorkoutTimerService.ACTION_END_WORKOUT_FROM_NOTIF) {
            val muscleGroup = intent.getStringExtra(WorkoutTimerService.EXTRA_MUSCLE_GROUP) ?: "Unknown"
            val elapsedTime = intent.getLongExtra(WorkoutTimerService.EXTRA_ELAPSED_TIME, 0L)
            viewModel.prepareCompletion(muscleGroup, elapsedTime)
            viewModel.setShowCompletionDialog(true)
        }
    }

    private fun triggerWorkoutCompletion() {
        val muscleGroup = viewModel.todayWorkoutName // I need to expose this
        val elapsedTime = timerService?.elapsedTime?.value ?: 0L
        viewModel.prepareCompletion(muscleGroup, elapsedTime)
        viewModel.setShowCompletionDialog(true)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, WorkoutTimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        val filter = IntentFilter(WorkoutTimerService.ACTION_WORKOUT_STOPPED)
        ContextCompat.registerReceiver(this, workoutStoppedReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(workoutStoppedReceiver)
    }

    private fun startWorkoutService(muscleGroup: String, durationMin: Int) {
        val intent = Intent(this, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_START
            putExtra(WorkoutTimerService.EXTRA_MUSCLE_GROUP, muscleGroup)
            putExtra(WorkoutTimerService.EXTRA_DURATION_MIN, durationMin)
        }
        startForegroundService(intent)
        viewModel.setWorkoutActive(true)
    }

    private fun stopWorkoutService() {
        val intent = Intent(this, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_STOP
        }
        startService(intent)
        viewModel.setWorkoutActive(false)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
