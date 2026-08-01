package com.example.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.FitnessViewModel
import com.example.fitness.ui.theme.ElectricNeonBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(viewModel: FitnessViewModel, onBack: () -> Unit) {
    val history by viewModel.workoutHistory.collectAsState(initial = emptyList())
    val weightHistory = history.filter { it.weight > 0 }.sortedByDescending { it.startTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WEIGHT HISTORY", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (weightHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No weight records found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weightHistory) { session ->
                    WeightRecordCard(
                        weight = session.weight,
                        date = session.date,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.startTime))
                    )
                }
            }
        }
    }
}

@Composable
fun WeightRecordCard(weight: Float, date: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(date, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.7f))
            }
            Text(
                text = String.format("%.2f kg", weight),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = ElectricNeonBlue
            )
        }
    }
}
