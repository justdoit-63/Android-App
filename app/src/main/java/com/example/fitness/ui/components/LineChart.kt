package com.example.fitness.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.theme.ElectricNeonBlue

data class ChartDataPoint(
    val label: String,
    val value: Float
)

@Composable
fun LineChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = ElectricNeonBlue
) {
    if (dataPoints.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No data points available", color = Color.Gray)
        }
        return
    }

    val maxWeight = dataPoints.maxOf { it.value }.coerceAtLeast(1f)
    val minWeight = dataPoints.minOf { it.value }.coerceAtMost(maxWeight - 1f)
    val range = (maxWeight - minWeight).coerceAtLeast(1f)

    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width((dataPoints.size * 60).dp.coerceAtLeast(300.dp))
                    .fillMaxHeight()
                    .padding(vertical = 20.dp, horizontal = 40.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

                val path = Path()
                val points = dataPoints.mapIndexed { index, point ->
                    val x = index * spacing
                    val y = height - ((point.value - minWeight) / range * height)
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        // Smooth line using quadratic bezier could be added here, 
                        // but let's start with simple line for stability.
                        path.lineTo(points[i].x, points[i].y)
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw dots and labels
                    points.forEachIndexed { index, offset ->
                        drawCircle(
                            color = lineColor,
                            radius = 5.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }
        }
        
        // X-Axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(60.dp)
        ) {
            dataPoints.forEach { point ->
                Text(
                    text = point.label,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}
