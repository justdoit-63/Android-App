package com.example.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            TopAppBar(title = { Text("Workout Schedule") })
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.day, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (item.muscle2.isNotEmpty()) "${item.muscle1} + ${item.muscle2}" else item.muscle1,
                                    color = ElectricNeonBlue
                                )
                                Text("${item.durationMinutes} min", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { editingDay = item }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
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
