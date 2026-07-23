package com.example.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.theme.ElectricNeonBlue
import com.example.fitness.ui.theme.ElectricNeonBlueDeep
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FitnessViewModel,
    onStartService: (String, Int) -> Unit,
    onStopService: () -> Unit
) {
    val isActive by viewModel.isWorkoutActive.collectAsState()
    val weeklyWorkouts by viewModel.weeklyWorkouts.collectAsState(initial = emptyList())
    val elapsedTime by viewModel.elapsedTime.collectAsState(initial = 0L)
    val streak by viewModel.streak.collectAsState()
    val todayScheduleFlow by viewModel.todaySchedule.collectAsState(initial = null)
    val isTodayCompleted by viewModel.isTodayCompleted.collectAsState(initial = false)

    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
    val todayWorkout = if (todayScheduleFlow != null) {
        if (todayScheduleFlow!!.muscle2.isNotEmpty()) "${todayScheduleFlow!!.muscle1} + ${todayScheduleFlow!!.muscle2}" else todayScheduleFlow!!.muscle1
    } else "Rest"
    
    LaunchedEffect(todayWorkout) {
        viewModel.updateTodayWorkoutName(todayWorkout)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CAFFEINE FITNESS",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = ElectricNeonBlue
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val brush = Brush.linearGradient(listOf(ElectricNeonBlue, ElectricNeonBlueDeep))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = {
                                if (isActive) {
                                    onStopService()
                                } else {
                                    onStartService(todayWorkout, todayScheduleFlow?.durationMinutes ?: 0)
                                }
                            },
                            modifier = Modifier
                                .size(100.dp)
                                .border(2.dp, brush, CircleShape)
                                .padding(4.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) Color.Red.copy(alpha = 0.2f) else ElectricNeonBlue.copy(alpha = 0.1f),
                                contentColor = if (isActive) Color.Red else ElectricNeonBlue
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        if (isActive) {
                            Text(
                                text = formatTimer(elapsedTime),
                                color = ElectricNeonBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Today's Workout Card
            item {
                Text("Today's Workout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dayOfWeek ?: "Today", style = MaterialTheme.typography.labelLarge, color = ElectricNeonBlue)
                            if (isTodayCompleted) {
                                Surface(
                                    color = Color.Green.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "✅ Completed",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                        Text(todayWorkout, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            WorkoutDetailItem("Exercises", "8")
                            WorkoutDetailItem("Duration", "${todayScheduleFlow?.durationMinutes ?: 0} min")
                        }
                    }
                }
            }

            // Progress Card
            if (isActive && todayScheduleFlow != null && todayScheduleFlow!!.durationMinutes > 0) {
                item {
                    Text("Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val targetMillis = todayScheduleFlow!!.durationMinutes * 60000f
                            val progress = (elapsedTime / targetMillis).coerceIn(0f, 1f)
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = ElectricNeonBlue,
                                trackColor = ElectricNeonBlue.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricNeonBlue
                            )
                        }
                    }
                }
            }

            // Weekly Progress
            item {
                Text("Weekly Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProgressStatCard("Workouts", "${weeklyWorkouts.size}", Modifier.weight(1f))
                    val totalMillis = weeklyWorkouts.sumOf { it.durationMillis }
                    val hours = totalMillis / (1000 * 60 * 60)
                    ProgressStatCard("Hours", "${hours}h", Modifier.weight(1f))
                    ProgressStatCard("Streak", "$streak Days", Modifier.weight(1f))
                }
            }

            // Motivational Quote
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElectricNeonBlue.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = ElectricNeonBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "The only bad workout is the one that didn't happen.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ElectricNeonBlue)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

fun formatTimer(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
