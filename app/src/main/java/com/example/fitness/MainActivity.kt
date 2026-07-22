package com.example.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.theme.FitnessTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Force dark theme as requested
            FitnessTheme(darkTheme = true) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClockScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ClockScreen(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var isAnalog by remember { mutableStateOf(true) }

    // Live update every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Digital", style = MaterialTheme.typography.labelLarge)
            Switch(checked = isAnalog, onCheckedChange = { isAnalog = it })
            Text("Analog", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Crossfade(targetState = isAnalog, label = "ClockType") { analog ->
            if (analog) {
                AnalogClock(currentTime)
            } else {
                DigitalClock(currentTime)
            }
        }
    }
}

@Composable
fun DigitalClock(calendar: Calendar) {
    val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)
    Text(
        text = timeString,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp
        )
    )
}

@Composable
fun AnalogClock(calendar: Calendar) {
    val seconds = calendar.get(Calendar.SECOND)
    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR)

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnSurface = MaterialTheme.colorScheme.onSurface

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Clock Face
            drawCircle(
                color = colorOnSurface,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Hour Markers
            for (i in 0 until 12) {
                val angle = i * 30 * (PI / 180)
                val start = Offset(
                    (center.x + (radius - 15.dp.toPx()) * cos(angle)).toFloat(),
                    (center.y + (radius - 15.dp.toPx()) * sin(angle)).toFloat()
                )
                val end = Offset(
                    (center.x + radius * cos(angle)).toFloat(),
                    (center.y + radius * sin(angle)).toFloat()
                )
                drawLine(color = colorOnSurface, start = start, end = end, strokeWidth = 4.dp.toPx())
            }

            // Hour Hand
            val hourAngle = (hours * 30 + minutes * 0.5 - 90) * (PI / 180)
            drawLine(
                color = colorOnSurface,
                start = center,
                end = Offset(
                    (center.x + radius * 0.5 * cos(hourAngle)).toFloat(),
                    (center.y + radius * 0.5 * sin(hourAngle)).toFloat()
                ),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minuteAngle = (minutes * 6 - 90) * (PI / 180)
            drawLine(
                color = colorOnSurface,
                start = center,
                end = Offset(
                    (center.x + radius * 0.7 * cos(minuteAngle)).toFloat(),
                    (center.y + radius * 0.7 * sin(minuteAngle)).toFloat()
                ),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Second Hand
            val secondAngle = (seconds * 6 - 90) * (PI / 180)
            drawLine(
                color = colorPrimary,
                start = center,
                end = Offset(
                    (center.x + radius * 0.9 * cos(secondAngle)).toFloat(),
                    (center.y + radius * 0.9 * sin(secondAngle)).toFloat()
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(color = colorPrimary, radius = 6.dp.toPx(), center = center)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ClockPreview() {
    FitnessTheme(darkTheme = true) {
        ClockScreen()
    }
}
