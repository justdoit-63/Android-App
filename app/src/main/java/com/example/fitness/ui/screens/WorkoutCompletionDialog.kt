package com.example.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.theme.ElectricNeonBlue

@Composable
fun WorkoutCompletionDialog(
    viewModel: FitnessViewModel,
    onSave: (weight: Float, exercises: Int, supplements: List<String>, notes: String) -> Unit,
    onDiscard: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val supplementOptions = listOf("Protein", "Creatine", "Pre-workout")
    val selectedSupplements = remember { mutableStateListOf<String>() }

    val feedback by viewModel.weightFeedback.collectAsState()

    Dialog(
        onDismissRequest = if (feedback == null) onDiscard else ({}),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Workout Summary",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = ElectricNeonBlue
                    )

                    // Stats Section
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { if (it.length <= 5) weight = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        OutlinedTextField(
                            value = exercises,
                            onValueChange = { if (it.length <= 3) exercises = it },
                            label = { Text("Exercises") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }

                    // Supplements Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Supplements Taken",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            supplementOptions.forEach { option ->
                                val isSelected = selectedSupplements.contains(option)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedSupplements.remove(option)
                                        else selectedSupplements.add(option)
                                    },
                                    label = { Text(option) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricNeonBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = ElectricNeonBlue
                                    )
                                )
                            }
                        }
                    }

                    // Notes Section
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Workout Notes (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onSave(
                                    weight.toFloatOrNull() ?: 0f,
                                    exercises.toIntOrNull() ?: 0,
                                    selectedSupplements.toList(),
                                    notes
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeonBlue, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("SAVE SESSION", fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(
                            onClick = onDiscard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Discard Session", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Feedback Overlay
            AnimatedVisibility(
                visible = feedback != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut()
            ) {
                feedback?.let { result ->
                    WeightFeedbackOverlay(
                        result = result,
                        onDismiss = { 
                            viewModel.clearWeightFeedback()
                            viewModel.setShowCompletionDialog(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeightFeedbackOverlay(result: com.example.fitness.ui.WeightFeedback, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            val emoji = if (result.isPositiveProgress) "😊" else "😔"
            val title = if (result.isPositiveProgress) "Great Job!" else "Keep Going!"
            
            Text(
                text = emoji,
                fontSize = 80.sp
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = result.message,
                style = MaterialTheme.typography.titleLarge,
                color = ElectricNeonBlue,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("CONTINUE", fontWeight = FontWeight.Black)
            }
        }
    }
}
