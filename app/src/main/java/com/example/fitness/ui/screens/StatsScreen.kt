package com.example.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.components.ChartDataPoint
import com.example.fitness.ui.components.LineChart
import com.example.fitness.ui.theme.ElectricNeonBlue
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: FitnessViewModel,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToWeightHistory: () -> Unit = {}
) {
    val history by viewModel.workoutHistory.collectAsState(initial = emptyList())
    val isBulking by viewModel.isBulking.collectAsState()
    val weightStats by viewModel.weightStats.collectAsState()

    val chartData = history.filter { it.weight > 0 }
        .sortedBy { it.startTime }
        .map { ChartDataPoint(it.date.take(6), it.weight) }

    Scaffold(
        topBar = {
            TopAppBar(title = { 
                Text("PROGRESS STATS", fontWeight = FontWeight.Black, letterSpacing = 1.sp) 
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Bulking/Cutting Toggle
            item {
                GoalToggle(
                    isBulking = isBulking,
                    onToggle = { viewModel.setBulking(it) }
                )
            }

            // Achievement Navigation
            item {
                Button(
                    onClick = onNavigateToAchievements,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("VIEW ACHIEVEMENTS", fontWeight = FontWeight.Bold)
                }
            }

            // Weight History Navigation Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWeightHistory() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weight History", fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            // Summary Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatSummaryCard("Highest Weight", formatWeight(weightStats.highestWeight), Modifier.weight(1f))
                        StatSummaryCard("Last Weight", formatWeight(weightStats.lastWeight), Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatSummaryCard("Max Gain", formatDiff(weightStats.highestGain), Modifier.weight(1f))
                        StatSummaryCard("Max Loss", formatDiff(weightStats.highestLoss), Modifier.weight(1f))
                    }
                }
            }

            // Progress Graph
            item {
                Text("Weight Progress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LineChart(
                            dataPoints = chartData,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun GoalToggle(isBulking: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            GoalToggleButton(
                text = "BULKING",
                isSelected = isBulking,
                onClick = { onToggle(true) },
                modifier = Modifier.weight(1f)
            )
            GoalToggleButton(
                text = "CUTTING",
                isSelected = !isBulking,
                onClick = { onToggle(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GoalToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) ElectricNeonBlue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.Black else Color.Gray,
            letterSpacing = 1.sp,
            fontSize = 12.sp
        )
    }
}

@Composable
fun StatSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ElectricNeonBlue)
        }
    }
}

fun formatWeight(weight: Float): String {
    if (weight == 0f) return "--"
    return String.format(Locale.getDefault(), "%.2f kg", weight)
}

fun formatDiff(diff: Float): String {
    if (diff == 0f) return "--"
    return if (diff < 1) {
        "${(diff * 1000).toInt()} g"
    } else {
        String.format(Locale.getDefault(), "%.2f kg", diff)
    }
}

fun formatHours(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    return "${hours}h ${minutes}m"
}
