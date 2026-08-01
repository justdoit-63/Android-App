package com.example.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.data.ScheduleDay
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.theme.ElectricNeonBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: FitnessViewModel) {
    val schedule by viewModel.schedule.collectAsState(initial = emptyList())
    var editingDay by remember { mutableStateOf<ScheduleDay?>(null) }

    val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val sortedSchedule = schedule.sortedBy { daysOrder.indexOf(it.day) }

    Scaffold(
        topBar = {
            TopAppBar(title = { 
                Text("WORKOUT SCHEDULE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) 
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedSchedule) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.day.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricNeonBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (item.muscle2.isNotEmpty()) "${item.muscle1} + ${item.muscle2}" else item.muscle1,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.durationMinutes} min", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            IconButton(
                                onClick = { editingDay = item },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = ElectricNeonBlue.copy(alpha = 0.1f),
                                    contentColor = ElectricNeonBlue
                                )
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        if (editingDay != null) {
            EditScheduleDialog(
                day = editingDay!!,
                onDismiss = { editingDay = null },
                onSave = { updatedDay ->
                    viewModel.updateScheduleDay(updatedDay)
                    editingDay = null
                }
            )
        }
    }
}

@Composable
fun EditScheduleDialog(
    day: ScheduleDay,
    onDismiss: () -> Unit,
    onSave: (ScheduleDay) -> Unit
) {
    var muscle1 by remember { mutableStateOf(day.muscle1) }
    var muscle2 by remember { mutableStateOf(day.muscle2) }
    var duration by remember { mutableStateOf(day.durationMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${day.day}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = muscle1, onValueChange = { muscle1 = it }, label = { Text("Muscle Group 1") })
                TextField(value = muscle2, onValueChange = { muscle2 = it }, label = { Text("Muscle Group 2") })
                TextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(day.copy(muscle1 = muscle1, muscle2 = muscle2, durationMinutes = duration.toIntOrNull() ?: 0))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
