package com.example.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fitness.ui.FitnessViewModel

@Composable
fun WorkoutCompletionDialog(
    viewModel: FitnessViewModel,
    onSave: (weight: Float, exercises: Int, supplements: List<String>) -> Unit,
    onDiscard: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf("") }
    
    val supplementOptions = listOf("Protein", "Creatine", "Pre-workout")
    val selectedSupplements = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDiscard) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Workout Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Current Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = exercises,
                    onValueChange = { exercises = it },
                    label = { Text("Exercises Completed") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Supplements Taken",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                supplementOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSupplements.contains(option),
                            onCheckedChange = { checked ->
                                if (checked) selectedSupplements.add(option)
                                else selectedSupplements.remove(option)
                            }
                        )
                        Text(text = option, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDiscard) {
                        Text("Discard Session", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            weight.toFloatOrNull() ?: 0f,
                            exercises.toIntOrNull() ?: 0,
                            selectedSupplements.toList()
                        )
                    }) {
                        Text("Save Session")
                    }
                }
            }
        }
    }
}
